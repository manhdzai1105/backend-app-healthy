package com.example.chat.repository;

import com.example.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
    SELECT c FROM Conversation c
    JOIN c.members m1
    JOIN c.members m2
    WHERE m1.account.id IN (:userId1, :userId2)
      AND m2.account.id IN (:userId1, :userId2)
      AND m1.account.id <> m2.account.id
      AND SIZE(c.members) = 2
""")
    List<Conversation> findDirectConversationBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);


    @Query("""
    SELECT DISTINCT c FROM Conversation c
    JOIN c.members cm
    WHERE cm.account.id = :userId
    """)
    List<Conversation> findAllByMemberId(@Param("userId") Long userId);

    @Query("""
       SELECT DISTINCT c FROM Conversation c
       LEFT JOIN FETCH c.members m
       LEFT JOIN FETCH m.account
       WHERE c.id = :id
       """)
    Optional<Conversation> findByIdWithMembers(@Param("id") Long id);

}
