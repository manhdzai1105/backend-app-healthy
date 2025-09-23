package com.example.chat.dto.res;

import com.example.chat.dto.MessageDto;
import com.example.chat.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversationResponse {
    private Long conversation_id;
    private UserDto partner;
    private MessageDto message;
    private Long unread;
}
