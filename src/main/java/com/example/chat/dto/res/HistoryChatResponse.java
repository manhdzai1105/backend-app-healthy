package com.example.chat.dto.res;

import com.example.chat.dto.ChatItem;
import com.example.chat.dto.MessageDto;
import com.example.chat.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HistoryChatResponse {
    private Long conversation_id;
    private long total_count_message;
    private boolean hasMore;
    private List<ChatItem> data;
}
