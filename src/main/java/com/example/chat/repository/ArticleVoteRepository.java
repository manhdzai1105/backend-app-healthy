package com.example.chat.repository;

import com.example.chat.entity.ArticleVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleVoteRepository extends JpaRepository<ArticleVote, Long> {
    Optional<ArticleVote> findByArticleIdAndUserId(Long articleId, Long userId);
    int countByArticleId(Long articleId);
}
