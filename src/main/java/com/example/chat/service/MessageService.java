package com.example.chat.service;

import com.example.chat.dto.MessageDto;
import com.example.chat.dto.UserDto;
import com.example.chat.dto.res.SendMessageResponse;
import com.example.chat.dto.res.UploadImageResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.Conversation;
import com.example.chat.entity.ConversationUnread;
import com.example.chat.entity.Message;
import com.example.chat.enums.MessageType;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationService conversationService;
    private final AccountRepository accountRepository;
    private final ConversationUnreadService conversationUnreadService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public SendMessageResponse sendMessage(Long receiverId, String content, MessageType messageType) {
        Account sender = getCurrentUser();
        Account receiver = getUserById(receiverId);

        validateNotSameUser(sender.getId(), receiverId);

        Conversation conversation = conversationService.getOrCreateConversation(sender.getId(), receiverId);

        Message message = Message.builder()
                .conversation(conversation)
                .account(sender)
                .messageType(messageType)
                .messageContent(content)
                .build();

        return saveAndNotify(message, conversation, sender, receiver);
    }

    @Transactional
    public SendMessageResponse sendImage(Long receiverId, MultipartFile file, MessageType messageType) {
        Account sender = getCurrentUser();
        Account receiver = getUserById(receiverId);

        validateNotSameUser(sender.getId(), receiverId);

        Conversation conversation = conversationService.getOrCreateConversation(sender.getId(), receiverId);

        UploadImageResponse imageInfo = cloudinaryService.uploadFile(file);

        Message message = Message.builder()
                .conversation(conversation)
                .account(sender)
                .fileUrl(imageInfo.getFileUrl())
                .fileName(imageInfo.getFileName())
                .fileSize(imageInfo.getFileSize())
                .fileType(imageInfo.getFileType())
                .messageType(messageType)
                .build();

        return saveAndNotify(message, conversation, sender, receiver);
    }

    // ================= PRIVATE HELPERS =================

    private SendMessageResponse saveAndNotify(Message message, Conversation conversation, Account sender, Account receiver) {
        Message savedMessage = messageRepository.save(message);

        ConversationUnread unread = conversationUnreadService.incrementUnreadCount(conversation, receiver);

        SendMessageResponse response = new SendMessageResponse(
                conversation.getId(),
                new UserDto(sender.getId(), sender.getUsername(),
                        sender.getUserDetail() != null ? sender.getUserDetail().getAvatar_url() : null),
                new MessageDto(
                        savedMessage.getId(),
                        savedMessage.getMessageType(),
                        savedMessage.getMessageContent(),
                        savedMessage.getFileName(),
                        savedMessage.getFileUrl(),
                        savedMessage.getFileSize(),
                        savedMessage.getFileType(),
                        savedMessage.getCreatedAt()
                ),
                unread.getUnreadCount().longValue()
        );

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiver.getId()),
                "/queue/messages",
                response
        );

        ConversationUnread unread_sender = conversationUnreadService.getOrCreateUnread(conversation, sender);
        return new SendMessageResponse(
                conversation.getId(),
                new UserDto(sender.getId(), sender.getUsername(),
                        sender.getUserDetail() != null ? sender.getUserDetail().getAvatar_url() : null),
                new MessageDto(
                        savedMessage.getId(),
                        savedMessage.getMessageType(),
                        savedMessage.getMessageContent(),
                        savedMessage.getFileName(),
                        savedMessage.getFileUrl(),
                        savedMessage.getFileSize(),
                        savedMessage.getFileType(),
                        savedMessage.getCreatedAt()
                ),
                unread_sender.getUnreadCount().longValue()
        );
    }

    private Account getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("SenderId not found"));
    }

    private Account getUserById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ReceiverId not found"));
    }

    private void validateNotSameUser(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Người gửi và người nhận không được trùng nhau");
        }
    }
}
