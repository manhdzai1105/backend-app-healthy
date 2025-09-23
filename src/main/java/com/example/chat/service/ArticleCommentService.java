package com.example.chat.service;

import com.example.chat.dto.req.CommentRequest;
import com.example.chat.dto.res.ArticleCommentResponse;
import com.example.chat.entity.Account;
import com.example.chat.entity.Article;
import com.example.chat.entity.ArticleComment;
import com.example.chat.entity.ArticleCommentVote;
import com.example.chat.enums.FileType;
import com.example.chat.enums.VoteType;
import com.example.chat.mapper.ArticleCommentMapper;
import com.example.chat.repository.AccountRepository;
import com.example.chat.repository.ArticleCommentRepository;
import com.example.chat.repository.ArticleCommentVoteRepository;
import com.example.chat.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleCommentService {
    private final ArticleCommentRepository articleCommentRepository;
    private final AccountRepository accountRepository;
    private final ArticleRepository articleRepository;
    private final CloudinaryService cloudinaryService;
    private final ArticleCommentVoteRepository articleCommentVoteRepository;

    @Transactional
    public ArticleCommentResponse createComment(
            Long articleId,
            String content,
            Long parentCommentId,
            FileType fileType,
            MultipartFile file
    ) {
        // 1. Lấy thông tin user hiện tại
        Account user = getCurrentUser();

        // 2. Tìm bài viết
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        // 3. Tạo comment
        ArticleComment comment = new ArticleComment();
        comment.setUser(user);
        comment.setArticle(article);
        comment.setContent(content);

        // 4. Nếu có parentCommentId thì set comment cha
        if (parentCommentId != null) {
            ArticleComment parent = articleCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Comment cha không tồn tại"));
            comment.setParentComment(parent); // reply
        } else {
            comment.setParentComment(null); // comment gốc
        }

        // 5. Xử lý file upload (nếu có)
        if (file != null && !file.isEmpty()) {
            var upload = cloudinaryService.uploadFile(file);
            comment.setFileUrl(upload.getFileUrl());
            comment.setFileType(fileType);
        }

        // 6. Lưu comment
        ArticleComment savedComment = articleCommentRepository.save(comment);

        // 7. Cập nhật số lượng comment cho bài viết
        article.setCommentCount(article.getCommentCount() + 1);
        articleRepository.save(article);

        // 8. Trả response
        return ArticleCommentMapper.toResponse(savedComment, user.getId());
    }


    @Transactional
    public void deleteComment(Long commentId) {
        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Article article = comment.getArticle();

        // đếm tất cả comment sẽ bị xóa (bao gồm replies)
        int deletedCount = countCommentsRecursively(comment);

        // xóa comment
        articleCommentRepository.delete(comment);

        // cập nhật commentCount trong article
        article.setCommentCount(article.getCommentCount() - deletedCount);
        articleRepository.save(article);
    }

    private int countCommentsRecursively(ArticleComment comment) {
        int count = 1; // chính nó
        if (comment.getReplies() != null) {
            for (ArticleComment reply : comment.getReplies()) {
                count += countCommentsRecursively(reply);
            }
        }
        return count;
    }

    //get all comment by article id
    @Transactional(readOnly = true)
    public List<ArticleCommentResponse> getCommentsByArticle(Long articleId) {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        List<ArticleComment> rootComments = articleCommentRepository
                .findByArticleIdAndParentCommentIsNullOrderByCreatedAtDesc(articleId);

        return rootComments.stream()
                .map(c -> ArticleCommentMapper.toResponseWithReplies(c, true, accountId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ArticleCommentResponse voteComment(Long commentId, VoteType reactionType) {
        Account user = getCurrentUser();
        ArticleComment comment = articleCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // tìm xem user đã vote chưa
        var existingVote = articleCommentVoteRepository.findByCommentIdAndUserId(commentId, user.getId());

        if (existingVote.isPresent()) {
            ArticleCommentVote vote = existingVote.get();
            if (vote.getReactionType() == reactionType) {
                // Bấm lại cùng reaction => bỏ vote
                articleCommentVoteRepository.delete(vote);
            } else {
                // Đổi reaction
                vote.setReactionType(reactionType);
                articleCommentVoteRepository.save(vote);
            }
        } else {
            // Vote mới
            ArticleCommentVote newVote = ArticleCommentVote.builder()
                    .comment(comment)
                    .user(user)
                    .reactionType(reactionType)
                    .build();
            articleCommentVoteRepository.save(newVote);
        }

        // cập nhật voteCount trong comment
        int voteCount = articleCommentVoteRepository.countByCommentId(commentId);
        comment.setVoteCount(voteCount);
        articleCommentRepository.save(comment);

        return ArticleCommentMapper.toResponse(comment, user.getId());
    }

    private Account getCurrentUser() {
        Long accountId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }
}
