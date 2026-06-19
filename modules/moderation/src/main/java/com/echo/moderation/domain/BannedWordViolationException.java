package com.echo.moderation.domain;

/**
 * 콘텐츠에 금칙어가 포함되어 있을 때 발생하는 도메인 예외.
 *
 * <p>HTTP 상태(400) 변환은 조립 지점(컨트롤러)의 몫이다 — domain은 Spring Web에 의존하지 않는다.
 */
public class BannedWordViolationException extends RuntimeException {

    private final String word;

    public BannedWordViolationException(String word) {
        super("금칙어가 포함되어 있습니다: " + word);
        this.word = word;
    }

    /** 위반에 걸린 금칙어. */
    public String getWord() {
        return word;
    }
}
