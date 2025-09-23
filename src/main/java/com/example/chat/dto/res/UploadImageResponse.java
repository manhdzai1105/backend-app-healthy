package com.example.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadImageResponse {
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
}
