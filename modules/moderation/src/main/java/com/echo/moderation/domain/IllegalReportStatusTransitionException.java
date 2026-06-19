package com.echo.moderation.domain;

/**
 * 허용되지 않은 신고 상태 전이를 시도할 때 발생하는 도메인 예외
 * (예: {@code PENDING → RESOLVED} 건너뛰기, 종료 상태에서의 재전이).
 *
 * <p>HTTP 상태(409) 변환은 조립 지점(컨트롤러)의 몫이다 — domain은 Spring Web에 의존하지 않는다.
 */
public class IllegalReportStatusTransitionException extends RuntimeException {

    public IllegalReportStatusTransitionException(ReportStatus from, ReportStatus to) {
        super("허용되지 않은 상태 전이입니다: %s → %s".formatted(from, to));
    }
}
