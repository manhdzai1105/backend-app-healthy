package com.example.chat.repository;

import com.example.chat.entity.ConversationMemberId;
import com.example.chat.entity.ConversationUnread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationUnreadRepository extends JpaRepository<ConversationUnread, ConversationMemberId> {
    Optional<ConversationUnread> findById_ConversationIdAndId_AccountId(Long conversationId, Long accountId);
}
