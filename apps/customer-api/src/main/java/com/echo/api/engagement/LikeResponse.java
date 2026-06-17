package com.echo.api.engagement;

import com.echo.engagement.domain.Like;
import java.time.LocalDateTime;

/**
 * 좋아요 응답 DTO.
 */
public record LikeResponse(Long id, Long postId, Long userId, LocalDateTime createdAt) {

    public static LikeResponse from(Like like) {
        return new LikeResponse(like.getId(), like.getPostId(), like.getUserId(), like.getCreatedAt());
    }
}
