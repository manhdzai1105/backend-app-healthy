package com.example.chat.integration.zalopay;

import com.example.chat.dto.res.PaymentInfoDto;
import com.example.chat.dto.res.TransactionResponse;
import com.example.chat.entity.Appointment;
import com.example.chat.entity.Transaction;
import com.example.chat.enums.AppointmentStatus;
import com.example.chat.enums.PaymentStatus;
import com.example.chat.enums.RefundStatus;
import com.example.chat.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ZaloPayConfig zaloPayConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final TransactionRepository transactionRepository;

    /** Tạo đơn thanh toán mới */
    public Map<String, Object> createZaloPayOrder(Appointment appointment) {
            long appTime = System.currentTimeMillis();
            String appTransId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + appointment.getId();

            String embedData = "{\"redirecturl\":\"myapp://orders\"}";
            String item = "[{\"itemid\":\"sp1\",\"itemname\":\"Lich kham\",\"itemprice\":" + appointment.getFee() + "}]";

            Map<String, String> order = new LinkedHashMap<>();
            order.put("app_id", zaloPayConfig.getAppId());
            order.put("app_trans_id", appTransId);
            order.put("app_time", String.valueOf(appTime));
            order.put("app_user", "user_" + appointment.getUser().getId());
            order.put("amount", String.valueOf(appointment.getFee()));
            order.put("description", "Lich hen #" + appointment.getId());
            order.put("bank_code", "zalopayapp");
            order.put("callback_url", zaloPayConfig.getCallbackUrl());
            order.put("embed_data", embedData);
            order.put("item", item);

            String data = zaloPayConfig.getAppId() + "|" + appTransId + "|" +
                    "user_" + appointment.getUser().getId() + "|" + appointment.getFee() + "|" +
                    appTime + "|" + embedData + "|" + item;

            String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
            order.put("mac", mac);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            order.forEach(params::add);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    zaloPayConfig.getEndpoint(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            log.info("ZaloPay order result: {}", result);

            Transaction tx = Transaction.builder()
                    .appointment(appointment)
                    .amount(appointment.getFee())
                    .appTransId(appTransId)
                    .paymentStatus(PaymentStatus.PENDING)
                    .refundStatus(RefundStatus.NONE)
                    .build();
            transactionRepository.save(tx);

            if (result != null) {
                result.put("app_trans_id", appTransId);
            }

            return result;
    }

    /** Xử lý callback từ ZaloPay */
    public Map<String, Object> handleCallback(Map<String, Object> body) {
        try {
            String data = (String) body.get("data");
            String reqMac = (String) body.get("mac");

            String myMac = hmacSHA256(zaloPayConfig.getKey2(), data);
            if (!myMac.equalsIgnoreCase(reqMac)) {
                return Map.of("return_code", -1, "return_message", "mac not equal");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> dataMap = mapper.readValue(data, Map.class);
            log.info("ZaloPay callback data: {}", dataMap);

            String appTransId = (String) dataMap.get("app_trans_id");
            Map<String, Object> queryResult = queryZaloPayOrder(appTransId);

            return Map.of("return_code", 1, "return_message", "success", "zalopay_result", queryResult);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý callback từ ZaloPay: " + e.getMessage(), e);
        }
    }

    /** Kiểm tra trạng thái thanh toán */
    public Map<String, Object> queryZaloPayOrder(String appTransId) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_id", zaloPayConfig.getAppId());
            params.put("app_trans_id", appTransId);

            String data = zaloPayConfig.getAppId() + "|" + appTransId + "|" + zaloPayConfig.getKey1();
            String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
            params.put("mac", mac);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            params.forEach(body::add);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://sb-openapi.zalopay.vn/v2/query",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            log.info("Query result: {}", result);

            if (result == null) {
                throw new IllegalStateException("ZaloPay không trả về dữ liệu hợp lệ");
            }

            String zpTransId = String.valueOf(result.get("zp_trans_id"));
            int returnCode = ((Number) result.get("return_code")).intValue();

            transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
                if (returnCode == 1) {
                    tx.setZpTransId(zpTransId);
                    tx.setPaymentStatus(PaymentStatus.SUCCESS);

                    Object serverTimeObj = result.get("server_time");
                    if (serverTimeObj != null) {
                        long serverTimeMillis = Long.parseLong(serverTimeObj.toString());
                        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
                        LocalDateTime paymentDateVN = Instant.ofEpochMilli(serverTimeMillis)
                                .atZone(vietnamZone)
                                .toLocalDateTime();
                        tx.setPaymentDate(paymentDateVN);
                    }
                } else if (returnCode == 2) {
                    tx.setPaymentStatus(PaymentStatus.FAILED);
                }
                transactionRepository.save(tx);
            });

            return result;
    }

    public TransactionResponse queryZaloPayTransaction(String appTransId) {
        queryZaloPayOrder(appTransId);

        Transaction tx = transactionRepository.findByAppTransId(appTransId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch trong DB"));

        return TransactionResponse.builder()
                .paymentInfo(
                        PaymentInfoDto.builder()
                                .paymentStatus(tx.getPaymentStatus())
                                .appTransId(tx.getAppTransId())
                                .zpTransId(tx.getZpTransId())
                                .zpRefundId(tx.getZpRefundId())
                                .refundId(tx.getRefundId())
                                .paymentDate(tx.getPaymentDate())
                                .refundStatus(tx.getRefundStatus())
                                .build()
                )
                .build();
    }

    /** Hoàn tiền giao dịch */
    public Map<String, Object> refundOrder(String appTransId) {
            Transaction tx = transactionRepository.findByAppTransId(appTransId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch trong DB"));

            if (tx.getPaymentStatus() != PaymentStatus.SUCCESS) {
                throw new IllegalStateException("Không thể hoàn tiền vì giao dịch chưa thành công");
            }

            String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .iterator().next().getAuthority().replace("ROLE_", "");

            switch (role) {
                case "USER" -> {
                    Appointment appointment = tx.getAppointment();
                    if (appointment == null) throw new IllegalStateException("Không tìm thấy lịch hẹn liên quan giao dịch");
                    if (appointment.getStatus() != AppointmentStatus.CANCELLED)
                        throw new IllegalStateException("Lịch hẹn chưa bị hủy, không thể hoàn tiền");
                }
                case "ADMIN" -> {}
                case "DOCTOR" -> {
                    // dc refund
                }
            }

            switch (tx.getRefundStatus()) {
                case PROCESSING -> throw new IllegalStateException("Refund đang được xử lý");
                case COMPLETED -> throw new IllegalStateException("Refund đã hoàn tất");
            }

            long amount = tx.getAmount();
            String zpTransId = tx.getZpTransId();
            long timestamp = System.currentTimeMillis();
            String uid = timestamp + "" + (111 + new Random().nextInt(888));
            String refundId = new SimpleDateFormat("yyMMdd").format(new Date())
                    + "_" + zaloPayConfig.getAppId() + "_" + uid;

            String refundDescription = "Hoan tien giao dich";
            String data = zaloPayConfig.getAppId() + "|" + zpTransId + "|" + amount + "|" + refundDescription + "|" + timestamp;
            String mac = hmacSHA256(zaloPayConfig.getKey1(), data);

            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_id", zaloPayConfig.getAppId());
            params.put("zp_trans_id", zpTransId);
            params.put("m_refund_id", refundId);
            params.put("amount", String.valueOf(amount));
            params.put("description", refundDescription);
            params.put("timestamp", String.valueOf(timestamp));
            params.put("mac", mac);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            params.forEach(body::add);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://sb-openapi.zalopay.vn/v2/refund",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            log.info("Refund result: {}", result);

            if (result == null) throw new IllegalStateException("ZaloPay không phản hồi refund hợp lệ");

            int refundCode = ((Number) result.get("return_code")).intValue();
            String zpRefundId = String.valueOf(result.get("refund_id"));

            switch (refundCode) {
                case 1 -> {
                    tx.setRefundStatus(RefundStatus.COMPLETED);
                    tx.setRefundId(refundId);
                    tx.setZpRefundId(zpRefundId);
                    result.put("refund_status", RefundStatus.COMPLETED.name());
                }
                case 2 -> {
                    tx.setRefundStatus(RefundStatus.FAILED);
                    result.put("refund_status", RefundStatus.FAILED.name());
                }
                case 3 -> {
                    tx.setRefundStatus(RefundStatus.PROCESSING);
                    tx.setRefundId(refundId);
                    tx.setZpRefundId(zpRefundId);
                    result.put("refund_status", RefundStatus.PROCESSING.name());
                }
            }

            transactionRepository.save(tx);
            return result;
    }

    /** Kiểm tra trạng thái hoàn tiền */
    public Map<String, Object> queryRefundOrder(String refundId) {
            long timestamp = System.currentTimeMillis();
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_id", zaloPayConfig.getAppId());
            params.put("m_refund_id", refundId);
            params.put("timestamp", String.valueOf(timestamp));

            String data = zaloPayConfig.getAppId() + "|" + refundId + "|" + timestamp;
            String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
            params.put("mac", mac);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            params.forEach(body::add);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://sb-openapi.zalopay.vn/v2/query_refund",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            log.info("Refund query result: {}", result);

            if (result == null) throw new IllegalStateException("Không nhận được dữ liệu hợp lệ từ ZaloPay");

            int returnCode = ((Number) result.get("return_code")).intValue();
            transactionRepository.findByRefundId(refundId).ifPresent(tx -> {
                switch (returnCode) {
                    case 1 -> tx.setRefundStatus(RefundStatus.COMPLETED);
                    case 2 -> tx.setRefundStatus(RefundStatus.FAILED);
                    case 3 -> tx.setRefundStatus(RefundStatus.PROCESSING);
                }
                transactionRepository.save(tx);
                result.put("refund_status", tx.getRefundStatus().name());
            });

            return result;
    }

    /** Utility HMAC-SHA256 */
    private String hmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi mã hóa HMAC-SHA256", e);
        }
    }
}
