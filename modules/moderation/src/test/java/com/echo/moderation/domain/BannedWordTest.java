package com.echo.moderation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * BannedWord 도메인 불변식 단위 테스트.
 */
class BannedWordTest {

    @Test
    @DisplayName("정상 생성 시 word와 createdAt이 설정된다")
    void create_success() {
        BannedWord word = new BannedWord("욕설");

        assertEquals("욕설", word.getWord());
        assertNotNull(word.getCreatedAt());
    }

    @Test
    @DisplayName("빈 금칙어는 만들 수 없다")
    void create_blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> new BannedWord(" "));
        assertThrows(IllegalArgumentException.class, () -> new BannedWord(null));
    }

    @Test
    @DisplayName("최대 길이를 넘으면 예외")
    void create_tooLong_throws() {
        String tooLong = "a".repeat(BannedWord.MAX_WORD_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> new BannedWord(tooLong));
    }
}
