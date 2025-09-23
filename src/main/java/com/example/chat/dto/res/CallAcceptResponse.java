package com.example.chat.dto.res;

import com.example.chat.dto.UserDto;
import com.example.chat.enums.CallType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallAcceptResponse {
    private Long callSessionId;
    private LocalDateTime startedAt;
    private UserDto receiver;
    private CallType callType;
}