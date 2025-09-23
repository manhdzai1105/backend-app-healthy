package com.example.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatItem {
    private UserDto sender;
    private MessageDto message;
}
