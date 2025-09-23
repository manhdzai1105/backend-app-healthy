package com.example.chat.mapper;

import com.example.chat.dto.ArticleMediaDto;
import com.example.chat.dto.UserDto;
import com.example.chat.dto.res.ArticleResponse;
import com.example.chat.entity.Article;
import com.example.chat.entity.ArticleVote;
import com.example.chat.integration.minio.MinioChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArticleMapper {

    private final MinioChannel minioChannel;

    // ❌ bỏ static
    public ArticleResponse toArticleResponse(Article article, Long currentUserId) {
        if (article == null) return null;

        ArticleResponse response = ArticleResponse.builder()
                .id(article.getId())
                .content(article.getContent())
                .status(article.getStatus().name())
                .user(UserDto.from(article.getUser()))
                .voteCount(article.getVoteCount())
                .commentCount(article.getCommentCount())
                .shareCount(article.getShareCount())
                .createdAt(article.getCreatedAt())
                .medias(article.getMedias() != null
                        ? article.getMedias().stream()
                        .map(m -> {
                            String presignedUrl;
                            try {
                                presignedUrl = minioChannel.presignedGetUrl(
                                        m.getObjectKey(),
                                        86400
                                );
                            } catch (Exception e) {
                                presignedUrl = null;
                            }

                            return ArticleMediaDto.builder()
                                    .id(m.getId())
                                    .fileType(m.getFileType().name())
                                    .fileUrl(presignedUrl) // ✅ trả presigned GET URL
                                    .orderIndex(m.getOrderIndex())
                                    .build();
                        })
                        .collect(Collectors.toList())
                        : null)
                .build();

        // ✅ Xử lý trạng thái vote của user
        if (currentUserId != null && article.getVotes() != null) {
            article.getVotes().stream()
                    .filter(v -> v.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .ifPresent((ArticleVote vote) -> {
                        response.setUserVoted(true);
                        response.setUserTypeVote(vote.getReactionType());
                    });
        } else {
            response.setUserVoted(false);
            response.setUserTypeVote(null);
        }

        return response;
    }
}
