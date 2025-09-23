package com.example.chat.repository;

import com.example.chat.entity.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Long> {
    List<ArticleComment> findByArticleIdAndParentCommentIsNullOrderByCreatedAtDesc(Long articleId);
}
