package com.example.chat.dto.res;

import com.example.chat.dto.ArticleMediaDto;
import com.example.chat.dto.UserDto;
import com.example.chat.enums.VoteType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ArticleResponse {
    private Long id;
    private String content;
    private String status;
    private UserDto user;
    private Integer voteCount;
    private Integer commentCount;
    private Integer shareCount;
    private LocalDateTime createdAt;
    private List<ArticleMediaDto> medias;
    private boolean userVoted;
    private VoteType userTypeVote;
}
