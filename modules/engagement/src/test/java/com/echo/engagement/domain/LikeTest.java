package com.echo.engagement.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Like 도메인 불변식 테스트(생성자 검증).
 */
class LikeTest {

    @Test
    @DisplayName("정상 값으로 생성된다")
    void create_ok() {
        Like like = new Like(1L, 2L);
        assertEquals(1L, like.getPostId());
        assertEquals(2L, like.getUserId());
        assertNotNull(like.getCreatedAt());
    }

    @Test
    @DisplayName("postId가 null이면 예외")
    void create_nullPost_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Like(null, 2L));
    }

    @Test
    @DisplayName("userId가 null이면 예외")
    void create_nullUser_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Like(1L, null));
    }
}
