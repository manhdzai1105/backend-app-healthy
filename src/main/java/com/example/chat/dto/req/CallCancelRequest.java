package com.example.chat.dto.req;

import lombok.Data;

@Data
public class CallCancelRequest {
    private Long callSessionId;
    private Long receiverId;
}
