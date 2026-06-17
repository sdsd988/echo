package com.echo.api.follow;

import java.util.List;

/**
 * 팔로워 조회 응답 DTO. userId를 팔로우하는 유저들의 id 목록과 그 수.
 */
public record FollowersResponse(Long userId, long count, List<Long> followerIds) {

    public static FollowersResponse of(Long userId, List<Long> followerIds) {
        return new FollowersResponse(userId, followerIds.size(), followerIds);
    }
}
