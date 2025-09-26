package com.example.chat.integration.zalopay;

import com.example.chat.entity.Appointment;
import com.example.chat.entity.Transaction;
import com.example.chat.enums.PaymentStatus;
import com.example.chat.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ZaloPayConfig zaloPayConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final TransactionRepository transactionRepository;

    public Map<String, Object> createZaloPayOrder(Appointment appointment) throws Exception {
        long appTime = System.currentTimeMillis();
        String appTransId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + appointment.getId();

        // ✅ embed_data và item phải là JSON string
        String embedData = "{\"redirecturl\":\"http://localhost:3000/orders\"}";
        String item = "[{\"itemid\":\"sp1\",\"itemname\":\"Lich kham\",\"itemprice\":" + appointment.getFee() + "}]";

        // build params
        Map<String, String> order = new LinkedHashMap<>();
        order.put("app_id", zaloPayConfig.getAppId());
        order.put("app_trans_id", appTransId);
        order.put("app_time", String.valueOf(appTime));
        order.put("app_user", "user_" + appointment.getUser().getId());
        order.put("amount", String.valueOf(appointment.getFee()));
        order.put("description", "Lich hen #" + appointment.getId()); // không dấu, ngắn gọn
        order.put("bank_code", "zalopayapp");
        order.put("callback_url", zaloPayConfig.getCallbackUrl());
        order.put("embed_data", embedData);
        order.put("item", item);

        // ✅ tính MAC
        String data = zaloPayConfig.getAppId() + "|" + appTransId + "|" +
                "user_" + appointment.getUser().getId() + "|" + appointment.getFee() + "|" +
                appTime + "|" + embedData + "|" + item;

        String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
        order.put("mac", mac);

        // ✅ gửi dạng application/x-www-form-urlencoded
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

        System.out.println("Request sent: " + order);
        System.out.println("ZaloPay response: " + result);

        // ✅ lưu transaction
        Transaction tx = Transaction.builder()
                .appointment(appointment)
                .amount(appointment.getFee())
                .appTransId(appTransId)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        transactionRepository.save(tx);

        return result;
    }

    public Map<String, Object> handleCallback(Map<String, Object> body) throws Exception {
        String data = (String) body.get("data");
        String reqMac = (String) body.get("mac");

        // ✅ Verify MAC
        String myMac = hmacSHA256(zaloPayConfig.getKey2(), data);
        if (!myMac.equalsIgnoreCase(reqMac)) {
            return Map.of("return_code", -1, "return_message", "mac not equal");
        }

        // ✅ Parse data JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> dataMap = mapper.readValue(data, Map.class);

        System.out.println("ZaloPay response callback: " + dataMap);

        String appTransId = (String) dataMap.get("app_trans_id");
        String zpTransId = String.valueOf(dataMap.get("zp_trans_id"));

        // ✅ Update DB
        transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {

            tx.setPaymentStatus(PaymentStatus.SUCCESS);
            tx.setZpTransId(zpTransId);
            tx.setPaymentDate(LocalDateTime.now());
            transactionRepository.save(tx);
        });

        return Map.of("return_code", 1, "return_message", "success");
    }

    // ==========  Kiểm tra trạng thái giao dịch ==========
    public Map<String, Object> queryZaloPayOrder(String appTransId) throws Exception {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayConfig.getAppId());
        params.put("app_trans_id", appTransId);

        // mac = app_id|app_trans_id|key1
        String data = zaloPayConfig.getAppId() + "|" + appTransId + "|" + zaloPayConfig.getKey1();
        String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
        params.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/query",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> result = response.getBody();
        System.out.println("Query result: " + result);

        // update nếu DB vẫn đang PENDING
        transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            int returnCode = (int) result.get("return_code");
            if (tx.getPaymentStatus() == PaymentStatus.PENDING) {
                if (returnCode == 1) {
                    tx.setPaymentStatus(PaymentStatus.SUCCESS);
                } else if (returnCode == 2) {
                    tx.setPaymentStatus(PaymentStatus.FAILED);
                }
                tx.setPaymentDate(LocalDateTime.now());
                transactionRepository.save(tx);
            }
        });

        return result;
    }

    // ========== Hoàn tiền ==========
    public Map<String, Object> refundOrder(String appTransId) throws Exception {
        // 1. Truy vấn trạng thái giao dịch trước khi refund
        Map<String, Object> queryResult = queryZaloPayOrder(appTransId);
        int returnCode = ((Number) queryResult.get("return_code")).intValue();

        if (returnCode != 1) {
            throw new IllegalStateException("Không thể hoàn tiền vì giao dịch chưa thành công");
        }

        // 2. Lấy amount và zp_trans_id từ query
        long amount = ((Number) queryResult.get("amount")).longValue();
        String zpTransId = String.valueOf(queryResult.get("zp_trans_id"));

        // 3. Sinh m_refund_id theo format yyMMdd_appid_uid
        long timestamp = System.currentTimeMillis();
        String uid = timestamp + "" + (111 + new Random().nextInt(888)); // unique id
        String refundId = new SimpleDateFormat("yyMMdd").format(new Date())
                + "_" + zaloPayConfig.getAppId()
                + "_" + uid;

        System.out.println("RefundId: " + refundId);

        // 4. Description mặc định
        String refundDescription = "Hoan tien giao dich";

        // 5. Tạo MAC = appid|zptransid|amount|description|timestamp
        String data = zaloPayConfig.getAppId() + "|"
                + zpTransId + "|"
                + amount + "|"
                + refundDescription + "|"
                + timestamp;

        String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
        System.out.println("Refund data for MAC: " + data);

        // 6. Build request
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
        System.out.println("Refund result: " + result);


        transactionRepository.findByAppTransId(appTransId).ifPresent(tx -> {
            tx.setRefundId(refundId);
            tx.setPaymentStatus(PaymentStatus.REFUND_PROCESSING);
            transactionRepository.save(tx);
        });

        return result;
    }

    // ========== Kiểm tra trạng thái hoàn tiền ==========
    public Map<String, Object> queryRefundOrder(String refundId) throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("app_id", zaloPayConfig.getAppId());
        params.put("m_refund_id", refundId);
        params.put("timestamp", String.valueOf(timestamp));

        // mac = app_id|m_refund_id|timestamp
        String data = zaloPayConfig.getAppId() + "|" + refundId + "|" + timestamp;
        String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
        params.put("mac", mac);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://sb-openapi.zalopay.vn/v2/query_refund",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> result = response.getBody();
        System.out.println("Refund query result: " + result);

        if (result != null) {
            int returnCode = ((Number) result.get("return_code")).intValue();

            transactionRepository.findByRefundId(refundId).ifPresent(tx -> {
                if (returnCode == 1) {
                    // Refund thành công
                    tx.setPaymentStatus(PaymentStatus.REFUNDED);
                    transactionRepository.save(tx);
                }
            });
        }

        return result;
    }

    private String hmacSHA256(String key, String data) throws Exception {
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
    }
}
