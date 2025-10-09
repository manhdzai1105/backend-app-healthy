package com.example.chat.integration.ai;

import com.example.chat.integration.ai.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthyAiService {

    private final ChatClient chatClient;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    private static final long MAX_FILE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_MIME_PREFIX = List.of("image/", "text/", "application/pdf");

    /**
     * Chat với HealthyBot — trợ lý sức khỏe thông minh
     */
    public ChatDto chat(Long userId, String message, List<MultipartFile> files) {

        // Chuẩn bị danh sách media
        List<Media> medias = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                if (file.getSize() > MAX_FILE_BYTES) {
                    throw new IllegalArgumentException("File quá lớn: " + file.getOriginalFilename());
                }

                String mime = file.getContentType() != null
                        ? file.getContentType()
                        : MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;

                if (ALLOWED_MIME_PREFIX.stream().noneMatch(mime::startsWith)) {
                    throw new IllegalArgumentException("Không hỗ trợ định dạng: " + mime);
                }

                medias.add(Media.builder()
                        .mimeType(MimeTypeUtils.parseMimeType(mime))
                        .data(file.getResource())
                        .build());
            }
        }

        // Cấu hình sinh nội dung
        ChatOptions chatOptions = ChatOptions.builder().temperature(0.5).build();

        String systemPrompt = """
                Bạn là **HealthyBot**, trợ lý sức khỏe thông minh của ứng dụng **Healthy**.

                Nhiệm vụ:
                - Tư vấn sức khỏe, giấc ngủ, dinh dưỡng, tâm lý, và thói quen sống lành mạnh.
                - Không được kê đơn thuốc hay chẩn đoán bệnh nghiêm trọng.
                - Khi gặp triệu chứng nguy hiểm, hãy khuyên người dùng đi khám.

                Phong cách trả lời:
                - Giọng điệu thân thiện, nhẹ nhàng, khuyến khích.
                - Trình bày ngắn gọn, dễ hiểu, dùng gạch đầu dòng khi cần.
                - Có thể hỏi lại 1 câu nếu cần thêm thông tin để hỗ trợ tốt hơn.
                """;

        String answer = chatClient.prompt()
                .options(chatOptions)
                .system(systemPrompt)
                .user(u -> {
                    medias.forEach(u::media);
                    u.text(message);
                })
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId.toString()))
                .call()
                .content();

        // Trả về kết quả
        return new ChatDto("ASSISTANT", answer);
    }

    /**
     * Lấy lịch sử hội thoại của userId
     */
    public List<ChatDto> getHistory(Long userId, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        List<Message> all = jdbcChatMemoryRepository.findByConversationId(userId.toString());
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);

        return all.subList(from, to)
                .stream()
                .map(this::toChatDto)
                .collect(Collectors.toList());
    }

    private ChatDto toChatDto(Message m) {
        String role = m.getMessageType().name(); // USER | ASSISTANT
        String text = m.getText();
        return new ChatDto(role, text);
    }

    /**
     * Xóa toàn bộ lịch sử chat của người dùng
     */
    public void clearHistory(Long userId) {
        jdbcChatMemoryRepository.deleteByConversationId(userId.toString());
    }
}
