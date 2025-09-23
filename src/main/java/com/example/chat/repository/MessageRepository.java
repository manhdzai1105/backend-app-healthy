package com.example.chat.repository;

import com.example.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Lấy 10 tin nhắn mới nhất (sắp xếp giảm dần theo ID)
    @Query("""
    SELECT m FROM Message m
    WHERE m.conversation.id = :conversationId
    ORDER BY m.id DESC
""")
    List<Message> findTop10LatestMessages(@Param("conversationId") Long conversationId, Pageable pageable);

    // Lấy các tin nhắn cũ hơn một message cụ thể
    @Query("""
    SELECT m FROM Message m
    WHERE m.conversation.id = :conversationId AND m.id < :lastMessageId
    ORDER BY m.id DESC
""")
    List<Message> findOlderMessages(@Param("conversationId") Long conversationId,
                                    @Param("lastMessageId") Long lastMessageId,
                                    Pageable pageable);

    // Đếm tổng số tin nhắn
    long countByConversationId(Long conversationId);

    // Kiểm tra còn tin nhắn cũ hơn không
    boolean existsByConversationIdAndIdLessThan(Long conversationId, Long messageId);

}
