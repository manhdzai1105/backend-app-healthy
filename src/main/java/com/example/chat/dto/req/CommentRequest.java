package com.example.chat.dto.req;

import com.example.chat.enums.FileType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CommentRequest {
    private String content;           // Nội dung comment
    private Long parentCommentId;     // Nếu reply, gán comment cha, null nếu comment gốc
    private FileType fileType;          // IMAGE, VIDEO, FILE
    private MultipartFile file;       // File upload kèm comment
}
