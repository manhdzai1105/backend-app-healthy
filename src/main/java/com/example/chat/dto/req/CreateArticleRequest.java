package com.example.chat.dto.req;

import com.example.chat.enums.ArticleStatus;
import lombok.Data;


import java.util.List;

@Data
public class CreateArticleRequest {
    private String content;
    private ArticleStatus status;
    private List<ArticleMediaRequest> medias;
}