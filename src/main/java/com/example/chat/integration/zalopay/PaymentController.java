package com.example.chat.integration.zalopay;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> result = paymentService.handleCallback(body);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("return_code", 0, "return_message", e.getMessage()));
        }
    }

    // Kiểm tra trạng thái thanh toán
    @GetMapping("/status/{appTransId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String appTransId) {
        try {
            return ResponseEntity.ok(paymentService.queryZaloPayOrder(appTransId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Hoàn tiền giao dịch
    @PostMapping("/refund/{appTransId}")
    public ResponseEntity<Map<String, Object>> refund(
            @PathVariable String appTransId
    ) {
        try {
            return ResponseEntity.ok(paymentService.refundOrder(appTransId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Kiểm tra trạng thái hoàn tiền
    @GetMapping("/refund-status/{refundId}")
    public ResponseEntity<Map<String, Object>> refundStatus(@PathVariable String refundId) {
        try {
            return ResponseEntity.ok(paymentService.queryRefundOrder(refundId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
