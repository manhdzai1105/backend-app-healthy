package com.example.chat.service;

import com.example.chat.dto.ChatItem;
import com.example.chat.dto.MessageDto;
import com.example.chat.dto.UserDto;
import com.example.chat.dto.res.ConversationResponse;
import com.example.chat.dto.res.HistoryChatResponse;
import com.example.chat.entity.*;
import com.example.chat.mapper.ConversationMapper;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.ConversationRepository;
import com.example.chat.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final AccountRepository accountRepository;
    private final ConversationMapper conversationMapper;
    private final MessageRepository messageRepository;

    @Transactional
    public Conversation getOrCreateConversation(Long senderId, Long receiverId) {
        List<Conversation> conversations = conversationRepository.findDirectConversationBetween(senderId, receiverId);
        if (!conversations.isEmpty()) {
            return conversations.getFirst();
        }

        Conversation conversation = Conversation.builder()
                .build();
        conversation = conversationRepository.save(conversation);

        Account sender = getAccountOrThrow(senderId, "Sender");
        Account receiver = getAccountOrThrow(receiverId, "Receiver");

        // Tạo thành viên cuộc trò chuyện
        ConversationMember member1 = createMember(conversation, sender);

        ConversationMember member2 = createMember(conversation, receiver);

        // Gán các thành viên vào cuộc trò chuyện
        Set<ConversationMember> members = new HashSet<>();
        members.add(member1);
        members.add(member2);
        conversation.setMembers(members);

        // Lưu lại conversation với members
        return conversationRepository.save(conversation);
    }

    public List<ConversationResponse> getUserConversations(Long userId) {
        List<Conversation> conversations = conversationRepository.findAllByMemberId(userId);
        return conversations.stream()
                .map(conversation -> conversationMapper.toDTO(conversation, userId))
                .collect(Collectors.toList());
    }

    public HistoryChatResponse getLatestMessages(Long receiverId, int limit) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long conversationId = getConversationIdOrNull(userId, receiverId);
        if (conversationId == null) return new HistoryChatResponse(null, 0, false, List.of());

        Pageable pageable = PageRequest.of(0, limit);
        List<Message> messages = messageRepository.findTop10LatestMessages(conversationId, pageable);

        long total = messageRepository.countByConversationId(conversationId);

        boolean hasMore = !messages.isEmpty() &&
                messageRepository.existsByConversationIdAndIdLessThan(conversationId, messages.getLast().getId());

        Collections.reverse(messages);

        return new HistoryChatResponse(conversationId, total, hasMore, buildChatItems(messages));
    }

    public HistoryChatResponse getOlderMessages(Long receiverId, Long lastMessageId, int limit) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long conversationId = getConversationIdOrNull(userId, receiverId);
        if (conversationId == null) return new HistoryChatResponse(null, 0, false, List.of());

        Pageable pageable = PageRequest.of(0, limit);
        List<Message> messages = messageRepository.findOlderMessages(conversationId, lastMessageId, pageable);

        long total = messageRepository.countByConversationId(conversationId);

        boolean hasMore = !messages.isEmpty() &&
                messageRepository.existsByConversationIdAndIdLessThan(conversationId, messages.getLast().getId());

        Collections.reverse(messages);

        return new HistoryChatResponse(conversationId, total, hasMore, buildChatItems(messages));
    }

    // ==== Private Helper Methods ====

    private Long getConversationIdOrNull(Long userId, Long receiverId) {
        return conversationRepository.findDirectConversationBetween(userId, receiverId)
                .stream().findFirst()
                .map(Conversation::getId)
                .orElse(null);
    }

    private List<ChatItem> buildChatItems(List<Message> messages) {
        return messages.stream().map(message -> {
            Account acc = message.getAccount();
            return new ChatItem(
                    UserDto.builder()
                            .id(acc != null ? acc.getId() : null)
                            .username(acc != null ? acc.getUsername() : null)
                            .avatarUrl(acc != null && acc.getUserDetail() != null ? acc.getUserDetail().getAvatar_url() : null)
                            .build(),
                    new MessageDto(
                            message.getId(), message.getMessageType(), message.getMessageContent(),
                            message.getFileName(), message.getFileUrl(),
                            message.getFileSize(), message.getFileType(), message.getCreatedAt()
                    )
            );
        }).toList();
    }

    private Account getAccountOrThrow(Long accountId, String role) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(role + "Id not found"));
    }

    private ConversationMember createMember(Conversation conversation, Account account) {
        return ConversationMember.builder()
                .id(new ConversationMemberId(conversation.getId(), account.getId()))
                .conversation(conversation)
                .account(account)
                .build();
    }

    public Conversation getConversationWithMembers(Long id) {
        return conversationRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

}
