package com.echo.moderation.domain;

/**
 * 신고 처리 상태. 한 방향으로만 한 단계씩 전이한다: {@code PENDING → REVIEWED → RESOLVED}.
 */
public enum ReportStatus {

    /** 접수됨(초기 상태). */
    PENDING,
    /** 검토됨. */
    REVIEWED,
    /** 처리 완료(종료 상태). */
    RESOLVED;

    /** {@code next}로 전이할 수 있는지. 다음 단계로만 한 칸 이동 가능하다. */
    public boolean canTransitionTo(ReportStatus next) {
        return switch (this) {
            case PENDING -> next == REVIEWED;
            case REVIEWED -> next == RESOLVED;
            case RESOLVED -> false;
        };
    }
}
