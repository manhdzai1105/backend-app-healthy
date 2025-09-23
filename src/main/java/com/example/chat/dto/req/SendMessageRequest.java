package com.example.chat.dto.req;

import com.example.chat.enums.MessageType;
import lombok.Data;

@Data
public class SendMessageRequest {
    private Long receiverId;
    private String content;
    private MessageType messageType;
}
