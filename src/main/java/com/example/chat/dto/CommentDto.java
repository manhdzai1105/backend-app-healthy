package com.example.chat.dto;

import com.example.chat.enums.FileType;
import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String comment;
    private FileType fileType;
    private String fileUrl;
    private Long parentCommentId;
    private Integer voteCount;
}
