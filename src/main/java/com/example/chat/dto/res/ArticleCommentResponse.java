package com.example.chat.dto.res;

import com.example.chat.dto.UserDto;
import com.example.chat.enums.FileType;
import com.example.chat.enums.VoteType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleCommentResponse {
    private Long id;
    private Long articleId;
    private Integer articleCommentCount;
    private UserDto user;
    private String comment;
    private FileType fileType;
    private String fileUrl;
    private Long parentCommentId;
    private Integer voteCount;
    private LocalDateTime createdAt;

    // replies để hiển thị comment con (reply)
    private List<ArticleCommentResponse> replies;
    private boolean userVoted;
    private VoteType userTypeVote;
}

