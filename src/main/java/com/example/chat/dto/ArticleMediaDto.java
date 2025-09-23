package com.example.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArticleMediaDto {
    private Long id;
    private String fileType;
    private String fileUrl;
    private Integer orderIndex;
}
