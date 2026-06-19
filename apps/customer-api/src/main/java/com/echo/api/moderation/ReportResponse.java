package com.echo.api.moderation;

import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportStatus;
import com.echo.moderation.domain.ReportTargetType;
import java.time.LocalDateTime;

/**
 * 신고 응답 DTO. POST /reports · GET /reports/{id} · PATCH .../status 공통 응답.
 */
public record ReportResponse(
    Long id,
    Long reporterId,
    ReportTargetType targetType,
    Long targetId,
    String reason,
    ReportStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
            report.getId(),
            report.getReporterId(),
            report.getTargetType(),
            report.getTargetId(),
            report.getReason(),
            report.getStatus(),
            report.getCreatedAt(),
            report.getUpdatedAt()
        );
    }
}
