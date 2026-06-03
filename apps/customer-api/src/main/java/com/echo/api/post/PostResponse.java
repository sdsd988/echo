package com.echo.api.post;

import com.echo.post.domain.Post;
import java.time.LocalDateTime;

/**
 * POST /posts 응답 DTO.
 */
public record PostResponse(Long id, Long authorId, String content, LocalDateTime createdAt) {

    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getAuthorId(),
            post.getContent(),
            post.getCreatedAt()
        );
    }
}
