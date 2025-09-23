package com.example.chat.dto.req;

import com.example.chat.enums.VoteType;
import lombok.Data;

@Data
public class VoteArticleRequest {
    private VoteType type;
}
