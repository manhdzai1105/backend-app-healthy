package com.example.chat.controller;

import com.example.chat.dto.req.SendMessageRequest;
import com.example.chat.dto.res.HistoryChatResponse;
import com.example.chat.dto.res.SendMessageResponse;

import com.example.chat.enums.MessageType;
import com.example.chat.service.ConversationService;
import com.example.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    @PostMapping("/send")
    public ResponseEntity<SendMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        SendMessageResponse response = messageService.sendMessage(
                request.getReceiverId(),
                request.getContent(),
                request.getMessageType()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/send-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SendMessageResponse> sendImageMessage(
            @RequestParam Long receiverId,
            @RequestPart("file") MultipartFile file
    ) {
        SendMessageResponse response = messageService.sendImage(receiverId, file, MessageType.IMAGE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    public ResponseEntity<HistoryChatResponse> getLatestMessages(
            @RequestParam("receiverId") Long receiverId,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        HistoryChatResponse response = conversationService.getLatestMessages(receiverId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/older")
    public ResponseEntity<HistoryChatResponse> getOlderMessages(
            @RequestParam Long receiverId,
            @RequestParam Long lastMessageId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        HistoryChatResponse response = conversationService.getOlderMessages(receiverId, lastMessageId, limit);
        return ResponseEntity.ok(response);
    }


}
