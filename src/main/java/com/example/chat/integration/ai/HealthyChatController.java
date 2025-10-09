package com.example.chat.integration.ai;

import com.example.chat.dto.ApiResponse;
import com.example.chat.integration.ai.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/healthy")
@RequiredArgsConstructor
@Slf4j
public class HealthyChatController {

    private final HealthyAiService healthyAiService;

    /**
     * Gửi tin nhắn tới HealthyBot (có thể kèm file)
     */
    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED) // ✅ Trả về HTTP 201
    public ApiResponse<ChatDto> chat(
            @RequestPart("message") String message,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        // ✅ Lấy userId từ context (nếu bạn đã gắn vào Principal)
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ChatDto response = healthyAiService.chat(userId, message, files);

        return ApiResponse.<ChatDto>builder()
                .code(HttpServletResponse.SC_CREATED) // ✅ HTTP 201
                .message("Chat created successfully")
                .data(response)
                .build();
    }

    /**
     * Lấy lịch sử hội thoại của người dùng
     */
    @GetMapping("/history")
    public ApiResponse<List<ChatDto>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ChatDto> history = healthyAiService.getHistory(userId, page, size);
        return ApiResponse.<List<ChatDto>>builder()
                .code(HttpServletResponse.SC_OK)
                .message("Chat history")
                .data(history)
                .build();
    }

    /**
     * Xóa toàn bộ lịch sử chat của userId
     */
    @DeleteMapping("/history")
    public ApiResponse<Void> clearHistory() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        healthyAiService.clearHistory(userId);
        return ApiResponse.<Void>builder()
                .code(HttpServletResponse.SC_OK)
                .message("Chat history cleared")
                .data(null)
                .build();
    }
}
