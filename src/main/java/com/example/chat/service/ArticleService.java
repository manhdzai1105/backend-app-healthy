package com.example.chat.service;

import com.example.chat.dto.PagingResponse;
import com.example.chat.dto.req.ArticleMediaRequest;
import com.example.chat.dto.req.CreateArticleRequest;
import com.example.chat.dto.res.ArticleResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.Article;
import com.example.chat.entity.ArticleMedia;
import com.example.chat.entity.ArticleVote;
import com.example.chat.enums.ArticleStatus;
import com.example.chat.enums.VoteType;
import com.example.chat.mapper.ArticleMapper;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.ArticleMediaRepository;
import com.example.chat.repository.ArticleRepository;
import com.example.chat.repository.ArticleVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleMediaRepository articleMediaRepository;
    private final AccountRepository accountRepository;
    private final ArticleVoteRepository articleVoteRepository;
    private final ArticleMapper articleMapper;

    @Transactional
    public ArticleResponse createArticle(CreateArticleRequest request) {
        // Lay thong tin User
        Account user = getCurrentUser();

        // Tạo article
        Article article = Article.builder()
                .content(request.getContent())
                .status(request.getStatus() != null ? request.getStatus() : ArticleStatus.PUBLIC)
                .user(user)
                .build();

        article = articleRepository.save(article);

        // Lưu danh sách media nếu có
        List<ArticleMedia> medias = new ArrayList<>();
        if (request.getMedias() != null && !request.getMedias().isEmpty()) {
            for (ArticleMediaRequest mediaReq : request.getMedias()) {
                ArticleMedia media = ArticleMedia.builder()
                        .article(article)
                        .fileType(mediaReq.getFileType())
                        .objectKey(mediaReq.getObjectKey())
                        .orderIndex(mediaReq.getOrderIndex())
                        .build();

                articleMediaRepository.save(media);
                medias.add(media);
            }
        }
        article.setMedias(medias);

        // Map sang ArticleResponse và trả về
        return articleMapper.toArticleResponse(article, user.getId());
    }

    public PagingResponse<ArticleResponse> listPublicArticles(Pageable pageable) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Article> page = articleRepository.findByStatus(ArticleStatus.PUBLIC, pageable);

        return PagingResponse.<ArticleResponse>builder()
                .code(200)
                .message("Success")
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .data(
                        page.getContent()
                                .stream()
                                .map(article -> articleMapper.toArticleResponse(article, accountId)) // ✅ dùng bean
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Transactional
    public ArticleResponse voteArticle(Long articleId, VoteType reactionType) {
        Account user = getCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        var existingVote = articleVoteRepository.findByArticleIdAndUserId(articleId, user.getId());

        if (existingVote.isPresent()) {
            ArticleVote vote = existingVote.get();
            if (vote.getReactionType() == reactionType) {
                // Bấm lại cùng reaction => bỏ vote
                articleVoteRepository.delete(vote);
            } else {
                // Đổi reaction
                vote.setReactionType(reactionType);
                articleVoteRepository.save(vote);
            }
        } else {
            // Vote mới
            ArticleVote newVote = ArticleVote.builder()
                    .article(article)
                    .user(user)
                    .reactionType(reactionType)
                    .build();
            articleVoteRepository.save(newVote);
        }

        // Cập nhật voteCount trong Article
        int voteCount = articleVoteRepository.countByArticleId(articleId);
        article.setVoteCount(voteCount);

        articleRepository.save(article);

        // ✅ dùng bean ArticleMapper thay vì static
        return articleMapper.toArticleResponse(article, user.getId());
    }


    @Transactional
    public void deleteArticle(Long articleId) {
        Account user = getCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        // 🔒 kiểm tra quyền (chủ bài viết)
        if (!article.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền xóa bài viết này");
        }

        articleRepository.delete(article);
    }


    private Account getCurrentUser() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }
}