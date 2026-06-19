package com.echo.api.moderation;

import com.echo.common.ApiResponse;
import com.echo.moderation.application.ReportService;
import com.echo.moderation.domain.IllegalReportStatusTransitionException;
import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 신고(Report) HTTP API 컨트롤러.
 *
 * <p>신고 접수 주체(reporter)는 X-User-Id 헤더로 식별한다(인증은 범위 외).
 * 도메인/application은 Spring Web에 의존하지 않으므로, 상태 전이 위반(409)·미존재(404)는 여기서 매핑한다.
 */
@RestController
@RequestMapping("/reports")
@Tag(name = "Report", description = "신고 API")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "신고 접수", description = "X-User-Id가 대상을 신고합니다. 접수 시 상태는 PENDING.")
    public ApiResponse<ReportResponse> submit(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody CreateReportRequest request) {

        Report report = reportService.submit(
                userId, request.targetType(), request.targetId(), request.reason());
        return ApiResponse.ok(ReportResponse.from(report), "신고가 접수되었습니다.");
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "신고 조회", description = "reportId로 신고 하나를 조회합니다. 없으면 404.")
    public ApiResponse<ReportResponse> get(@PathVariable Long reportId) {
        Report report = reportService.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다. id=" + reportId));
        return ApiResponse.ok(ReportResponse.from(report));
    }

    @PatchMapping("/{reportId}/status")
    @Operation(summary = "신고 상태 처리",
            description = "PENDING→REVIEWED→RESOLVED 순으로 한 단계씩 전이합니다. 미존재 404, 허용되지 않은 전이 409.")
    public ApiResponse<ReportResponse> changeStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest request) {

        try {
            Report report = reportService.changeStatus(reportId, request.status());
            return ApiResponse.ok(ReportResponse.from(report), "신고 상태를 변경했습니다.");
        } catch (ReportNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalReportStatusTransitionException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
