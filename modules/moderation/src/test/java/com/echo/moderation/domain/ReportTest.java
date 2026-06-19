package com.echo.moderation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Report 도메인 불변식·상태 전이 단위 테스트.
 */
class ReportTest {

    @Test
    @DisplayName("정상 생성 시 상태는 PENDING이고 created/updated가 설정된다")
    void create_success_pending() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "스팸입니다");

        assertEquals(1L, report.getReporterId());
        assertEquals(ReportTargetType.POST, report.getTargetType());
        assertEquals(10L, report.getTargetId());
        assertEquals("스팸입니다", report.getReason());
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertNotNull(report.getCreatedAt());
        assertNotNull(report.getUpdatedAt());
    }

    @Test
    @DisplayName("reporterId/target/reason이 비면 예외")
    void create_invalid_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new Report(null, ReportTargetType.POST, 10L, "사유"));
        assertThrows(IllegalArgumentException.class,
            () -> new Report(1L, null, 10L, "사유"));
        assertThrows(IllegalArgumentException.class,
            () -> new Report(1L, ReportTargetType.POST, null, "사유"));
        assertThrows(IllegalArgumentException.class,
            () -> new Report(1L, ReportTargetType.POST, 10L, "  "));
    }

    @Test
    @DisplayName("PENDING → REVIEWED → RESOLVED 순서로 전이할 수 있다")
    void changeStatus_forward_ok() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "사유");

        report.changeStatus(ReportStatus.REVIEWED);
        assertEquals(ReportStatus.REVIEWED, report.getStatus());

        report.changeStatus(ReportStatus.RESOLVED);
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
    }

    @Test
    @DisplayName("단계를 건너뛰면(PENDING → RESOLVED) 예외")
    void changeStatus_skip_throws() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "사유");

        assertThrows(IllegalReportStatusTransitionException.class,
            () -> report.changeStatus(ReportStatus.RESOLVED));
    }

    @Test
    @DisplayName("종료 상태(RESOLVED)에서는 더 이상 전이할 수 없다")
    void changeStatus_fromResolved_throws() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "사유");
        report.changeStatus(ReportStatus.REVIEWED);
        report.changeStatus(ReportStatus.RESOLVED);

        assertThrows(IllegalReportStatusTransitionException.class,
            () -> report.changeStatus(ReportStatus.REVIEWED));
    }

    @Test
    @DisplayName("뒤로 가는 전이(REVIEWED → PENDING)는 막는다")
    void changeStatus_backward_throws() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "사유");
        report.changeStatus(ReportStatus.REVIEWED);

        assertThrows(IllegalReportStatusTransitionException.class,
            () -> report.changeStatus(ReportStatus.PENDING));
    }
}
