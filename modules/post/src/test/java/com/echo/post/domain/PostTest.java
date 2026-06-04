package com.echo.post.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Post 도메인 불변식 단위 테스트 — Spring 컨텍스트 없이 순수 POJO로 검증.
 */
class PostTest {

    @Test
    @DisplayName("content가 280자를 초과하면 IllegalArgumentException")
    void content_exceedsMaxLength_throws() {
        String tooLong = "a".repeat(Post.MAX_CONTENT_LENGTH + 1);

        assertThrows(IllegalArgumentException.class, () -> new Post(1L, tooLong));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("content가 null·빈 값·공백뿐이면 IllegalArgumentException")
    void content_blankOrNull_throws(String blank) {
        assertThrows(IllegalArgumentException.class, () -> new Post(1L, blank));
    }

    @Test
    @DisplayName("content가 정확히 280자면 정상 생성된다 (경계값)")
    void content_atMaxLength_isAccepted() {
        String exact = "a".repeat(Post.MAX_CONTENT_LENGTH);

        Post post = assertDoesNotThrow(() -> new Post(1L, exact));

        assertEquals(Post.MAX_CONTENT_LENGTH, post.getContent().length());
    }

    @Test
    @DisplayName("유효한 content는 정상 생성되고 createdAt이 채워진다")
    void validContent_constructsSuccessfully() {
        Post post = new Post(42L, "안녕, Echo");

        assertEquals(42L, post.getAuthorId());
        assertEquals("안녕, Echo", post.getContent());
        assertNotNull(post.getCreatedAt());
    }
}
