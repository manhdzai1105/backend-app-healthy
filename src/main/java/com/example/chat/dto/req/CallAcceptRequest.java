package com.example.chat.dto.req;


import com.example.chat.enums.CallType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CallAcceptRequest {
    private Long callSessionId;
    private LocalDateTime startedAt;
    private CallType callType;
}
