package com.example.chat.repository;

import com.example.chat.entity.Article;
import com.example.chat.enums.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article,Long> {
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
}
