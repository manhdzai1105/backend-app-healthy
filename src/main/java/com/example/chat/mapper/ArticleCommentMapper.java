package com.example.chat.mapper;

import com.example.chat.dto.UserDto;
import com.example.chat.dto.res.ArticleCommentResponse;
import com.example.chat.entity.ArticleComment;

import java.util.List;
import java.util.stream.Collectors;

public class ArticleCommentMapper {

    // Dùng cho 1 comment đơn lẻ (khi create)
    public static ArticleCommentResponse toResponse(ArticleComment comment, Long currentUserId) {
        return toResponseWithReplies(comment, false, currentUserId);
    }

    // Dùng cho get list (bao gồm replies đệ quy)
    public static ArticleCommentResponse toResponseWithReplies(ArticleComment comment, boolean includeReplies, Long currentUserId) {
        ArticleCommentResponse res = new ArticleCommentResponse();
        res.setId(comment.getId());
        res.setArticleId(comment.getArticle().getId());
        res.setArticleCommentCount(comment.getArticle().getCommentCount());
        res.setUser(UserDto.from(comment.getUser()));
        res.setComment(comment.getContent());
        res.setFileType(comment.getFileType());
        res.setFileUrl(comment.getFileUrl());
        res.setParentCommentId(
                comment.getParentComment() != null ? comment.getParentComment().getId() : null
        );
        res.setVoteCount(comment.getVoteCount() != null ? comment.getVoteCount() : 0);
        res.setCreatedAt(comment.getCreatedAt());

        // 🔥 check trạng thái vote của currentUser
        if (currentUserId != null && comment.getVotes() != null) {
            comment.getVotes().stream()
                    .filter(v -> v.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .ifPresent(vote -> {
                        res.setUserVoted(true);
                        res.setUserTypeVote(vote.getReactionType());
                    });
        } else {
            res.setUserVoted(false);
            res.setUserTypeVote(null);
        }

        // replies đệ quy
        if (includeReplies && comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<ArticleCommentResponse> replyResponses = comment.getReplies().stream()
                    .map(reply -> toResponseWithReplies(reply, true, currentUserId))
                    .collect(Collectors.toList());
            res.setReplies(replyResponses);
        }

        return res;
    }
}
