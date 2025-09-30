package com.example.chat.dto.res;

import com.example.chat.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentInfoDto {
    private PaymentStatus paymentStatus;
    private String appTransId;
    private String zpTransId;
    private String zpRefundId;
    private String refundId;
    private LocalDateTime paymentDate;
}
