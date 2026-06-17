package com.echo.api.engagement;

/**
 * 게시물 좋아요 수 응답 DTO.
 */
public record LikeCountResponse(Long postId, long count) {

    public static LikeCountResponse of(Long postId, long count) {
        return new LikeCountResponse(postId, count);
    }
}
