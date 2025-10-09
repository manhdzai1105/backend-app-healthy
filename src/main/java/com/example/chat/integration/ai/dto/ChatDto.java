package com.example.chat.integration.ai.dto;

/** Bản ghi hội thoại giữa người dùng và bot */
public record ChatDto(
        String role,  // "USER" | "ASSISTANT"
        String text   // Nội dung tin nhắn
) {}
