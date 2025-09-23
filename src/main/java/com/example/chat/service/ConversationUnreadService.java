package com.example.chat.service;

import com.example.chat.entity.Account;
import com.example.chat.entity.Conversation;
import com.example.chat.entity.ConversationUnread;
import com.example.chat.entity.ConversationMemberId;
import com.example.chat.repository.ConversationUnreadRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationUnreadService {

    private final ConversationUnreadRepository conversationUnreadRepository;

    @Transactional
    public ConversationUnread incrementUnreadCount(Conversation conversation, Account receiver) {
        ConversationMemberId id = new ConversationMemberId(conversation.getId(), receiver.getId());

        // Tìm ConversationUnread theo ID
        ConversationUnread unread = conversationUnreadRepository.findById(id)
                .orElseGet(() -> {
                    ConversationUnread newUnread = new ConversationUnread();
                    newUnread.setId(id);
                    newUnread.setUnreadCount(0);
                    newUnread.setConversation(conversation);
                    newUnread.setAccount(receiver);
                    return newUnread;
                });

        // Tăng số lượng chưa đọc
        unread.setUnreadCount(unread.getUnreadCount() + 1);

        return conversationUnreadRepository.save(unread);
    }

    public void resetUnreadCount(Long conversationId, Long userId) {
        ConversationMemberId id = new ConversationMemberId(conversationId, userId);

        conversationUnreadRepository.findById(id)
                .ifPresent(unread -> {
                    unread.setUnreadCount(0);
                    conversationUnreadRepository.save(unread);
                });
    }

    public ConversationUnread getOrCreateUnread(Conversation conversation, Account account) {
        ConversationMemberId id = new ConversationMemberId(conversation.getId(), account.getId());

        return conversationUnreadRepository.findById(id)
                .orElseGet(() -> {
                    ConversationUnread newUnread = new ConversationUnread();
                    newUnread.setId(id);
                    newUnread.setUnreadCount(0);
                    newUnread.setConversation(conversation);
                    newUnread.setAccount(account);
                    return newUnread;
                });
    }
}
