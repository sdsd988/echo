package com.echo.moderation.application;

import com.echo.moderation.domain.BannedWord;
import com.echo.moderation.domain.BannedWordViolationException;
import com.echo.moderation.infrastructure.BannedWordRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 금칙어 애플리케이션 서비스. 콘텐츠에 DB로 관리되는 금칙어가 포함되어 있는지 검사한다.
 *
 * <p>이 서비스는 "검사"만 담당한다 — 어떤 콘텐츠(게시물 등)에 적용할지는 조립 지점(컨트롤러)이 정한다.
 * 덕분에 post 모듈과 moderation 모듈이 서로 직접 의존하지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class BannedWordService {

    private final BannedWordRepository bannedWordRepository;

    public BannedWordService(BannedWordRepository bannedWordRepository) {
        this.bannedWordRepository = bannedWordRepository;
    }

    /**
     * 콘텐츠에 금칙어가 포함되어 있으면 {@link BannedWordViolationException}(→ 400)을 던진다.
     */
    public void assertClean(String content) {
        findViolation(content).ifPresent(word -> {
            throw new BannedWordViolationException(word);
        });
    }

    /**
     * 콘텐츠에 포함된 첫 금칙어를 반환한다(없으면 빈 값). 대소문자는 구분하지 않는다.
     */
    public Optional<String> findViolation(String content) {
        if (content == null || content.isBlank()) {
            return Optional.empty();
        }
        String haystack = content.toLowerCase(Locale.ROOT);
        return bannedWordRepository.findAll().stream()
            .map(BannedWord::getWord)
            .filter(word -> haystack.contains(word.toLowerCase(Locale.ROOT)))
            .findFirst();
    }
}
