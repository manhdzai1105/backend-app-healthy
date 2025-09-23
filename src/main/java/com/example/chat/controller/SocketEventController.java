package com.example.chat.controller;

import com.example.chat.dto.*;
import com.example.chat.dto.req.*;
import com.example.chat.dto.res.CallAcceptResponse;
import com.example.chat.dto.res.SendMessageResponse;
import com.example.chat.dto.res.SignalReponse;
import com.example.chat.entity.*;
import com.example.chat.enums.MessageType;
import com.example.chat.listener.OnlineUserTracker;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.CallSessionRepository;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.CallService;
import com.example.chat.service.ConversationService;
import com.example.chat.service.ConversationUnreadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SocketEventController {
    private final ConversationUnreadService conversationUnreadService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CallService callService;
    private final AccountRepository accountRepository;
    private final MessageRepository messageRepository;
    private final OnlineUserTracker onlineUserTracker;
    private final ConversationService conversationService;
    private final CallSessionRepository callSessionRepository;

    @MessageMapping("/request-online-users")
    public void getOnlineUsers(Principal principal) {
        if (principal != null) {
            String accountId = principal.getName();

            // Gửi danh sách người dùng online về cho chính user đó (FE đang dùng /user/queue/online-users)
            messagingTemplate.convertAndSendToUser(
                    accountId,
                    "/queue/online-users",
                    onlineUserTracker.getOnlineUsers()
            );
        }
    }

    @MessageMapping("/reset-unread")
    public void resetUnread(@Payload ResetUnreadPayload payload, Principal principal) {
        // Lấy ID người dùng từ Principal (dạng String), sau đó parse sang Long
        Long accountId = Long.parseLong(principal.getName());

        // Gọi service để reset số lượng unread
        conversationUnreadService.resetUnreadCount(payload.getConversationId(), accountId);

        // Gửi thông báo về lại cho người dùng
        messagingTemplate.convertAndSendToUser(
                accountId.toString(),
                "/queue/reset-unread",
                payload
        );
    }

    @MessageMapping("/call/start")
    public void handleStartCall(@Payload CallStartRequest request, Principal principal) {
        Long accountId = Long.parseLong(principal.getName());
        CallSession callSession = callService.createCall(
                accountId,
                request.getReceiverId(),
                request.getCallType()
        );


        Account caller = getUserById(accountId);
        Account receiver = getUserById(request.getReceiverId());

        // Chuẩn bị thông tin người gọi
        UserDto callerDto = UserDto.builder()
                .id(caller.getId())
                .username(caller.getUsername())
                .avatarUrl(
                        caller.getUserDetail() != null
                                ? caller.getUserDetail().getAvatar_url()
                                : null
                )
                .build();

        // Tạo payload gửi cho người nhận
        IncomingCallPayload payload = IncomingCallPayload.builder()
                .callSessionId(callSession.getId())
                .caller(callerDto)
                .callType(request.getCallType())
                .build();

        // Chuẩn bị thông tin người nhận
        UserDto receiverDto = UserDto.builder()
                .id(receiver.getId())
                .username(receiver.getUsername())
                .avatarUrl(
                        receiver.getUserDetail() != null
                                ? receiver.getUserDetail().getAvatar_url()
                                : null
                )
                .build();

        // Tạo payload cho người gọi
        CallStartedPayload payload1 = CallStartedPayload.builder()
                .callSessionId(callSession.getId())
                .receiver(receiverDto)
                .callType(request.getCallType())
                .build();

        // Gửi socket đến người nhận (FE nên subscribe: /user/queue/incoming-call)
        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/incoming-call",
                payload
        );

        // Gửi socket đến người gọi
        messagingTemplate.convertAndSendToUser(
                caller.getId().toString(),
                "/queue/call-started",
                payload1
        );
    }

    @MessageMapping("/call/reject")
    public void handleRejectCall(@Payload CallRejectRequest request, Principal principal) {
        Long accountId = Long.parseLong(principal.getName());
        Account currentUser = getUserById(accountId);

        // 1. Gọi service xử lý từ chối
        CallSession callSession = callService.rejectCall(request.getCallSessionId());

        // 2. Lấy cuộc hội thoại
        Conversation conversation = callService.getConversationByCallSessionId(callSession.getId());

        handleCallMessage(conversation, callSession, currentUser, "REJECTED");

        Long callerId = callSession.getAccount().getId(); // người gọi
        messagingTemplate.convertAndSendToUser(
                callerId.toString(),
                "/queue/call-rejected",
                request.getCallSessionId()
        );
    }

    @MessageMapping("/call/cancel")
    public void handleCancelCall(@Payload CallCancelRequest request, Principal principal) {
        Long accountId = Long.parseLong(principal.getName());
        Account currentUser = getUserById(accountId);

        // 1. Gọi service xử lý hủy bỏ
        CallSession callSession = callService.cancelCall(request.getCallSessionId());

        // 2. Lấy cuộc hội thoại
        Conversation conversation = callService.getConversationByCallSessionId(callSession.getId());

        handleCallMessage(conversation, callSession, currentUser, "CANCELLED");

        Long receiverId = request.getReceiverId();
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/call-cancel",
                request.getCallSessionId()
        );
    }

    @MessageMapping("/call/miss")
    public void handleCallMiss(@Payload CallMissRequest request,  Principal principal) {
        Long accountId = Long.parseLong(principal.getName());
        Account currentUser = getUserById(accountId);

        // 1. Gọi service xử lý bỏ lỡ
        CallSession callSession = callService.cancelCall(request.getCallSessionId());

        // 2. Lấy cuộc hội thoại
        Conversation conversation = callService.getConversationByCallSessionId(callSession.getId());

        handleCallMessage(conversation, callSession, currentUser, "MISSED");

        Long receiverId = request.getReceiverId();
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/call-miss",
                request.getCallSessionId()
        );
    }

    @MessageMapping("/call/accept")
    public void handleAcceptCall(@Payload CallAcceptRequest request, Principal principal) {
        CallSession callSession = callService.acceptCall(
                request.getCallSessionId(),
                request.getStartedAt()
        );

        Long accountId = Long.parseLong(principal.getName());
        Account receiver = getUserById(accountId);

        UserDto receiverDto = UserDto.builder()
                .id(receiver.getId())
                .username(receiver.getUsername())
                .avatarUrl(
                        receiver.getUserDetail() != null
                                ? receiver.getUserDetail().getAvatar_url()
                                : null
                )
                .build();

        // Tạo response
        CallAcceptResponse response = new CallAcceptResponse();
        response.setCallSessionId(callSession.getId());
        response.setStartedAt(callSession.getStartedAt());
        response.setReceiver(receiverDto);
        response.setCallType(request.getCallType());

        // Gửi về người gọi
        Long callerId = callSession.getAccount().getId();
        messagingTemplate.convertAndSendToUser(
                callerId.toString(),
                "/queue/call-accept",
                response
        );
    }

    @MessageMapping("/call/end")
    public void handleEndCall(@Payload CallEndRequest request, Principal principal) {
        Long accountId = Long.parseLong(principal.getName());
        Account currentUser = getUserById(accountId);

        CallSession callSession = callService.endCall(request.getCallSessionId(), request.getEndedAt());

        LocalDateTime startedAt = callSession.getStartedAt();
        LocalDateTime endedAt = callSession.getEndedAt();

        Duration duration;
        if (startedAt != null && endedAt != null) {
            duration = Duration.between(startedAt, endedAt);
        } else {
            duration = Duration.ZERO; // tương đương với 00:00:00
        }

        Conversation conversation = callService.getConversationByCallSessionId(callSession.getId());

        handleCallMessage(conversation, callSession, currentUser, formatDuration(duration));

        Conversation conversationWithMembers = conversationService.getConversationWithMembers(conversation.getId());

        for (ConversationMember member : conversationWithMembers.getMembers()) {
            if (!member.getAccount().getId().equals(currentUser.getId())) {
                conversationUnreadService.incrementUnreadCount(conversation, member.getAccount());
                messagingTemplate.convertAndSendToUser(
                        member.getAccount().getId().toString(),
                        "/queue/call-end",
                        request.getCallSessionId()
                );
            }
        }
    }

    private void handleCallMessage(
            Conversation conversation,
            CallSession callSession,
            Account currentUser,
            String messageContent
    ) {
        Message callMessage = new Message();
        if("CANCELLED".equals(messageContent) || "REJECTED".equals(messageContent) || "MISSED".equals(messageContent)) {
            callMessage.setConversation(conversation);
            callMessage.setAccount(currentUser);
            callMessage.setMessageType(MessageType.CALL);
            callMessage.setMessageContent(messageContent);
        }else {
            callMessage.setConversation(conversation);
            callMessage.setAccount(callSession.getAccount());
            callMessage.setMessageType(MessageType.CALL);
            callMessage.setMessageContent(messageContent);
        }

        messageRepository.save(callMessage);

        Conversation conversationWithMembers = conversationService.getConversationWithMembers(conversation.getId());

        // Tăng unread cho người nhận nếu cuộc gọi bị hủy
        if ("CANCELLED".equals(messageContent)) {
            for (ConversationMember member : conversationWithMembers.getMembers()) {
                if (!member.getAccount().getId().equals(currentUser.getId())) {
                    conversationUnreadService.incrementUnreadCount(conversation, member.getAccount());
                }
            }
        }
        // Tăng unread cho người từ chối cuộc gọi
        else if ("REJECTED".equals(messageContent)) {
            for (ConversationMember member : conversationWithMembers.getMembers()) {
                if (member.getAccount().getId().equals(currentUser.getId())) {
                    conversationUnreadService.incrementUnreadCount(conversation, member.getAccount());
                }
            }
        }
        // Tăng unread cho người nhận bỏ lỡ cuộc gọi
        else if ("MISSED".equals(messageContent)) {
            for (ConversationMember member : conversationWithMembers.getMembers()) {
                if (!member.getAccount().getId().equals(currentUser.getId())) {
                    conversationUnreadService.incrementUnreadCount(conversation, member.getAccount());
                }
            }
        }

        // Map lưu unreadCount của tất cả thành viên
        Map<Long, Long> unreadCounts = new HashMap<>();

        for (ConversationMember member : conversationWithMembers.getMembers()) {
            Account memberAccount = member.getAccount();
            ConversationUnread unread = conversationUnreadService.getOrCreateUnread(conversation, memberAccount);
            unreadCounts.put(memberAccount.getId(), unread.getUnreadCount().longValue());
        }

        MessageDto messageDto = MessageDto.from(callMessage);
        UserDto senderDto = UserDto.from(currentUser);

        for (ConversationMember member : conversationWithMembers.getMembers()) {
            Long participantId = member.getAccount().getId();
            Long unread = unreadCounts.getOrDefault(participantId, 0L);

            SendMessageResponse response = new SendMessageResponse(
                    conversation.getId(),
                    senderDto,
                    messageDto,
                    unread
            );

            messagingTemplate.convertAndSendToUser(
                    participantId.toString(),
                    "/queue/messages",
                    response
            );
        }
    }

    @MessageMapping("/call/session/{callSessionId}/signal")
    public void handleSignal(
            @DestinationVariable String callSessionId,
            @Payload SignalPayload signal,
            Principal principal
    ) {
        Long accountId = Long.parseLong(principal.getName());

        SignalReponse signalReponse = new SignalReponse();
        signalReponse.setType(signal.getType());
        signalReponse.setData(signal.getData());
        signalReponse.setSenderId(accountId);

        messagingTemplate.convertAndSend(
                "/topic/call-session/" + callSessionId,
                signalReponse
        );
    }

    private Account getUserById(Long userId) {
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserId not found"));
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
