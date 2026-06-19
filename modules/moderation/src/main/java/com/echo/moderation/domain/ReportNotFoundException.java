package com.echo.moderation.domain;

/**
 * 존재하지 않는 신고를 조회/처리하려 할 때 발생하는 도메인 예외.
 *
 * <p>HTTP 상태(404) 변환은 조립 지점(컨트롤러)의 몫이다 — domain은 Spring Web에 의존하지 않는다.
 */
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(Long reportId) {
        super("신고를 찾을 수 없습니다. id=" + reportId);
    }
}
