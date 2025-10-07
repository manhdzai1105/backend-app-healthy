package com.example.chat.dto.req;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionRequest {
    private String medicineName;
    private Integer quantity;
    private String unit;
    private String dose;
}