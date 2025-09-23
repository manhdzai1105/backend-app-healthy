package com.example.chat.mapper;

import com.example.chat.dto.MessageDto;
import com.example.chat.dto.UserDto;
import com.example.chat.dto.res.ConversationResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.Conversation;
import com.example.chat.entity.ConversationMember;
import com.example.chat.entity.ConversationUnread;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    public ConversationResponse toDTO(Conversation conversation, Long currentUserId) {
        // Lấy người còn lại (partner)
        Account partner = conversation.getMembers().stream()
                .map(ConversationMember::getAccount)
                .filter(account -> !account.getId().equals(currentUserId))
                .findFirst()
                .orElse(null);

        UserDto partnerDTO = null;
        if (partner != null) {
            String avatarUrl = partner.getUserDetail() != null ? partner.getUserDetail().getAvatar_url() : null;
            partnerDTO = new UserDto(partner.getId(), partner.getUsername(), avatarUrl);
        }

        // Lấy tin nhắn cuối
        MessageDto lastMessageDto = extractLastMessage(conversation);

        // Lấy số lượng tin chưa đọc (unread)
        Integer unread = conversation.getUnreads().stream()
                .filter(unreadEntry -> unreadEntry.getAccount().getId().equals(currentUserId))
                .map(ConversationUnread::getUnreadCount)
                .findFirst()
                .orElse(0); // mặc định 0 nếu không có

        return new ConversationResponse(
                conversation.getId(),
                partnerDTO,
                lastMessageDto,
                unread.longValue()
        );
    }

    private MessageDto extractLastMessage(Conversation conversation) {
        if (conversation.getMessages().isEmpty()) {
            return null;
        }

        var lastMessage = conversation.getMessages().getLast();
        return new MessageDto(
                lastMessage.getId(),
                lastMessage.getMessageType(),
                lastMessage.getMessageContent(),
                lastMessage.getFileName(),
                lastMessage.getFileUrl(),
                lastMessage.getFileSize(),
                lastMessage.getFileType(),
                lastMessage.getCreatedAt()
        );
    }

}

