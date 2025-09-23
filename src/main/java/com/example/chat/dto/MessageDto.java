package com.example.chat.dto;

import com.example.chat.entity.Message;
import com.example.chat.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private MessageType message_type;
    private String message_content;
    private String file_name;
    private String file_url;
    private Long file_size;
    private String file_type;
    private LocalDateTime timestamp;

    public static MessageDto from(Message message) {
        return new MessageDto(
                message.getId(),
                message.getMessageType(),
                message.getMessageContent(),
                message.getFileName(),
                message.getFileUrl(),
                message.getFileSize(),
                message.getFileType(),
                message.getCreatedAt()
        );
    }
}
