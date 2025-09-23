package com.example.chat.dto.req;

import com.example.chat.enums.CallType;
import lombok.Data;

@Data
public class CallStartRequest {
    private Long receiverId;
    private CallType callType;
}
