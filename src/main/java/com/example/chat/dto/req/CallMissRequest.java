package com.example.chat.dto.req;

import lombok.Data;

@Data
public class CallMissRequest {
    private Long callSessionId;
    private Long receiverId;
}
