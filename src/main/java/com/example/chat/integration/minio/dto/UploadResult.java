package com.example.chat.integration.minio.dto;

import com.example.chat.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResult {
    private String objectKey;
    private String uploadUrl;
    private FileType fileType;
}
