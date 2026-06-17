package com.echo.follow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Follow 도메인 불변식 단위 테스트.
 */
class FollowTest {

    @Test
    @DisplayName("정상 생성 시 follower/followee와 createdAt이 설정된다")
    void create_success() {
        Follow follow = new Follow(1L, 2L);

        assertEquals(1L, follow.getFollowerId());
        assertEquals(2L, follow.getFolloweeId());
        assertNotNull(follow.getCreatedAt());
    }

    @Test
    @DisplayName("자기 자신은 팔로우할 수 없다")
    void create_self_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Follow(1L, 1L));
    }

    @Test
    @DisplayName("followerId/followeeId가 null이면 예외")
    void create_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Follow(null, 2L));
        assertThrows(IllegalArgumentException.class, () -> new Follow(1L, null));
    }
}
