package com.example.chat.integration.zalopay;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.res.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** Nhận callback từ ZaloPay */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestBody Map<String, Object> body) throws Exception {
        Map<String, Object> result = paymentService.handleCallback(body);
        return ResponseEntity.ok(result);
    }

    /** Kiểm tra trạng thái thanh toán */
    @GetMapping("/status/{appTransId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String appTransId) throws Exception {
        return ResponseEntity.ok(paymentService.queryZaloPayOrder(appTransId));
    }

    @GetMapping("/{appTransId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getPaymentStatus(@PathVariable String appTransId) {
        TransactionResponse response = paymentService.queryZaloPayTransaction(appTransId);
        return ResponseEntity.ok(
                ApiResponse.<TransactionResponse>builder()
                        .code(200)
                        .message("Lấy thông tin giao dịch thành công")
                        .data(response)
                        .build()
        );
    }

    /** Hoàn tiền giao dịch */
    @PostMapping("/refund/{appTransId}")
    public ResponseEntity<Map<String, Object>> refund(@PathVariable String appTransId) throws Exception {
        return ResponseEntity.ok(paymentService.refundOrder(appTransId));
    }

    /** Kiểm tra trạng thái hoàn tiền */
    @GetMapping("/refund-status/{refundId}")
    public ResponseEntity<Map<String, Object>> refundStatus(@PathVariable String refundId) throws Exception {
        return ResponseEntity.ok(paymentService.queryRefundOrder(refundId));
    }
}
