package com.echo.api.moderation;

import com.echo.moderation.domain.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * POST /reports 요청 DTO.
 * 신고 주체(reporter)는 본문이 아니라 X-User-Id 헤더로 식별한다.
 */
public record CreateReportRequest(
    @NotNull(message = "targetType은 필수입니다. (POST | USER)")
    ReportTargetType targetType,

    @NotNull(message = "targetId는 필수입니다.")
    Long targetId,

    @NotBlank(message = "reason은 비어 있을 수 없습니다.")
    @Size(max = 500, message = "reason은 500자를 초과할 수 없습니다.")
    String reason
) {
}
