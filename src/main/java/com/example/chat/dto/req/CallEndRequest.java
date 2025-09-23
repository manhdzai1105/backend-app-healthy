package com.example.chat.dto.req;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CallEndRequest {
    private Long callSessionId;
    private LocalDateTime endedAt;
}
