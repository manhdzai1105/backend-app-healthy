package com.example.chat.dto;

import com.example.chat.enums.CallType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomingCallPayload {
    private Long callSessionId;
    private UserDto caller;
    private CallType callType;
}
