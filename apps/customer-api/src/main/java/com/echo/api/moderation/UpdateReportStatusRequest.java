package com.echo.api.moderation;

import com.echo.moderation.domain.ReportStatus;
import jakarta.validation.constraints.NotNull;

/**
 * PATCH /reports/{reportId}/status 요청 DTO.
 * 전이할 목표 상태를 담는다(PENDING → REVIEWED → RESOLVED).
 */
public record UpdateReportStatusRequest(
    @NotNull(message = "status는 필수입니다. (PENDING | REVIEWED | RESOLVED)")
    ReportStatus status
) {
}
