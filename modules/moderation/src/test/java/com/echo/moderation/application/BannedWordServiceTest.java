package com.echo.moderation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import com.echo.moderation.domain.BannedWord;
import com.echo.moderation.domain.BannedWordViolationException;
import com.echo.moderation.infrastructure.BannedWordRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * BannedWordService 단위 테스트 — 리포지토리를 mock으로 검증.
 */
@ExtendWith(MockitoExtension.class)
class BannedWordServiceTest {

    @Mock
    private BannedWordRepository bannedWordRepository;

    @InjectMocks
    private BannedWordService bannedWordService;

    @Test
    @DisplayName("금칙어가 포함되면 assertClean이 예외를 던진다")
    void assertClean_violation_throws() {
        when(bannedWordRepository.findAll()).thenReturn(List.of(new BannedWord("badword")));

        BannedWordViolationException ex = assertThrows(BannedWordViolationException.class,
            () -> bannedWordService.assertClean("이건 badword 가 들어간 글"));
        assertEquals("badword", ex.getWord());
    }

    @Test
    @DisplayName("금칙어가 없으면 assertClean은 통과한다")
    void assertClean_clean_ok() {
        when(bannedWordRepository.findAll()).thenReturn(List.of(new BannedWord("badword")));

        assertDoesNotThrow(() -> bannedWordService.assertClean("깨끗한 글입니다"));
    }

    @Test
    @DisplayName("대소문자를 구분하지 않고 금칙어를 잡는다")
    void findViolation_caseInsensitive() {
        when(bannedWordRepository.findAll()).thenReturn(List.of(new BannedWord("BadWord")));

        assertTrue(bannedWordService.findViolation("contains BADWORD here").isPresent());
    }

    @Test
    @DisplayName("빈 콘텐츠는 검사 없이 통과한다")
    void findViolation_blank_empty() {
        assertTrue(bannedWordService.findViolation("   ").isEmpty());
    }
}
