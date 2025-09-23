package com.example.chat.repository;

import com.example.chat.entity.ArticleCommentVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleCommentVoteRepository extends JpaRepository<ArticleCommentVote, Long> {
    Optional<ArticleCommentVote> findByCommentIdAndUserId(Long commentId, Long userId);
    int countByCommentId(Long commentId);
}
