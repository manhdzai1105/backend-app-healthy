package com.example.chat.dto.req;

import com.example.chat.enums.FileType;
import lombok.Data;

@Data
public class ArticleMediaRequest {
    private String objectKey;   // key trong MinIO
    private FileType fileType;  // IMAGE, VIDEO, FILE
    private Integer orderIndex; // vị trí
}
