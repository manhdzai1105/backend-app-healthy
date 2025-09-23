package com.example.chat.integration.minio.dto;

import lombok.Data;

@Data
public class PresignPutRequest {
    private String fileName;
    private String contentType;
}
