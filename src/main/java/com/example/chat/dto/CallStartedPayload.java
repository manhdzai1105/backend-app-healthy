package com.example.chat.dto;

import com.example.chat.enums.CallType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallStartedPayload {
    private Long callSessionId;
    private UserDto receiver;
    private CallType callType;
}
