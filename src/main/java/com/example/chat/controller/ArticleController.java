package com.example.chat.controller;

import com.example.chat.dto.ApiResponse;
import com.example.chat.dto.PagingResponse;
import com.example.chat.dto.req.CommentRequest;
import com.example.chat.dto.req.CreateArticleRequest;
import com.example.chat.dto.req.VoteArticleRequest;
import com.example.chat.dto.req.VoteCommentRequest;
import com.example.chat.dto.res.ArticleCommentResponse;
import com.example.chat.dto.res.ArticleResponse;
import com.example.chat.service.ArticleCommentService;
import com.example.chat.service.ArticleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;
    private final ArticleCommentService articleCommentService;

    @PostMapping("/create")
    public ResponseEntity<ArticleResponse> createArticle(
            @RequestBody CreateArticleRequest request
    ) {
        ArticleResponse articleResponse = articleService.createArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(articleResponse);
    }



    @PostMapping("/{id}/create-comment")
    public ResponseEntity<ArticleCommentResponse> createComment(
            @PathVariable("id") Long articleId,
            @ModelAttribute CommentRequest form
    ) {
        ArticleCommentResponse response = articleCommentService.createComment(
                articleId,
                form.getContent(),
                form.getParentCommentId(),
                form.getFileType(),
                form.getFile()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @DeleteMapping("/comment/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        articleCommentService.deleteComment(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Xóa comment thành công")
                        .data(null)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Xóa bài viết thành công")
                        .data(null)
                        .build()
        );
    }

    @GetMapping("/public")
    public PagingResponse<ArticleResponse> getPublicArticles(
            @PageableDefault(size = 5, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return articleService.listPublicArticles(pageable);
    }

    @GetMapping("/{articleId}/comments")
    public List<ArticleCommentResponse> getCommentsByArticle(@PathVariable Long articleId) {
        return articleCommentService.getCommentsByArticle(articleId);
    }

    @PostMapping("/comment/{id}/vote")
    public ResponseEntity<ArticleCommentResponse> voteComment(
            @PathVariable Long id,
            @RequestBody VoteCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(articleCommentService.voteComment(id, request.getType()));
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<ArticleResponse> voteArticle(
            @PathVariable Long id,
            @RequestBody VoteArticleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(articleService.voteArticle(id, request.getType()));
    }


}
