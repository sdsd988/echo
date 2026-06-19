package com.echo.api.moderation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportStatus;
import com.echo.moderation.domain.ReportTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ReportResponse DTO 단위 테스트.
 */
class ReportResponseTest {

    @Test
    @DisplayName("from은 도메인의 필드를 그대로 옮긴다")
    void from_copiesFields() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "스팸");

        ReportResponse response = ReportResponse.from(report);

        assertEquals(report.getReporterId(), response.reporterId());
        assertEquals(ReportTargetType.POST, response.targetType());
        assertEquals(10L, response.targetId());
        assertEquals("스팸", response.reason());
        assertEquals(ReportStatus.PENDING, response.status());
        assertEquals(report.getCreatedAt(), response.createdAt());
        assertEquals(report.getUpdatedAt(), response.updatedAt());
    }
}
