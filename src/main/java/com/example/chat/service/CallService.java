package com.example.chat.service;

import com.example.chat.entity.Account;
import com.example.chat.entity.CallSession;
import com.example.chat.entity.Conversation;
import com.example.chat.enums.CallStatus;
import com.example.chat.enums.CallType;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.CallSessionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CallService {
    private final CallSessionRepository callSessionRepository;
    private final ConversationService conversationService;
    private final AccountRepository accountRepository;

    public CallSession createCall(Long accountId, Long receiverId, CallType callType){
        Account caller = getUserById(accountId);
        Conversation conversation = conversationService.getOrCreateConversation(caller.getId(), receiverId);

        CallSession callSession = CallSession.builder()
                .conversation(conversation)
                .account(caller)
                .callType(callType)
                .status(CallStatus.ONGOING)
                .build();

        return callSessionRepository.save(callSession);
    }

    public CallSession rejectCall(Long callSessionId) {
        CallSession callSession = callSessionRepository.findById(callSessionId)
                .orElseThrow(() -> new RuntimeException("Cuộc gọi không tồn tại"));

        // Cập nhật trạng thái cuộc gọi
        callSession.setStatus(CallStatus.REJECTED);

        callSessionRepository.save(callSession);
        return callSession;
    }

    public CallSession missCall(Long callSessionId) {
        CallSession callSession = callSessionRepository.findById(callSessionId)
                .orElseThrow(() -> new RuntimeException("Cuộc gọi không tồn tại"));

        // Cập nhật trạng thái cuộc gọi
        callSession.setStatus(CallStatus.MISSED);

        callSessionRepository.save(callSession);
        return callSession;
    }

    public CallSession cancelCall(Long callSessionId) {
        CallSession callSession = callSessionRepository.findById(callSessionId)
                .orElseThrow(() -> new RuntimeException("Cuộc gọi không tồn tại"));

        // Cập nhật trạng thái cuộc gọi
        callSession.setStatus(CallStatus.CANCELLED);

        callSessionRepository.save(callSession);
        return callSession;
    }

    public CallSession acceptCall(Long callSessionId, LocalDateTime startedAt) {
        CallSession callSession = callSessionRepository.findById(callSessionId)
                .orElseThrow(() -> new RuntimeException("Cuộc gọi không tồn tại"));

        callSession.setStartedAt(startedAt);
        callSessionRepository.save(callSession);

        return callSession;
    }

    public CallSession endCall(Long callSessionId, LocalDateTime endedAt) {
        CallSession callSession = callSessionRepository.findById(callSessionId)
                .orElseThrow(() -> new RuntimeException("Cuộc gọi không tồn tại"));

        callSession.setEndedAt(endedAt);

        callSessionRepository.save(callSession);
        return callSession;
    }

    public Conversation getConversationByCallSessionId(Long callSessionId) {
        CallSession callSession = callSessionRepository.findById(callSessionId)
                .orElseThrow(() -> new RuntimeException("Cuộc gọi không tồn tại"));

        return callSession.getConversation();
    }


    private Account getUserById(Long userId) {
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserId not found"));
    }

}
