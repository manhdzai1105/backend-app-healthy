package com.example.chat.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrescriptionResponse {
    private Long id;
    private String medicineName;
    private Integer quantity;
    private String unit;
    private String dose;
}
