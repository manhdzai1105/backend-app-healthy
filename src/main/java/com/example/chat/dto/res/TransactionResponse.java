package com.example.chat.dto.res;


import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class TransactionResponse {
    private PaymentInfoDto paymentInfo;
}
