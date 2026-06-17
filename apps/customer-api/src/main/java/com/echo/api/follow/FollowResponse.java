package com.echo.api.follow;

import com.echo.follow.domain.Follow;
import java.time.LocalDateTime;

/**
 * 팔로우 응답 DTO.
 */
public record FollowResponse(Long id, Long followerId, Long followeeId, LocalDateTime createdAt) {

    public static FollowResponse from(Follow follow) {
        return new FollowResponse(
            follow.getId(),
            follow.getFollowerId(),
            follow.getFolloweeId(),
            follow.getCreatedAt()
        );
    }
}
