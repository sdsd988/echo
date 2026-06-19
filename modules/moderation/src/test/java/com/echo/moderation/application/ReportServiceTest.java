package com.echo.moderation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.echo.moderation.domain.IllegalReportStatusTransitionException;
import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportNotFoundException;
import com.echo.moderation.domain.ReportStatus;
import com.echo.moderation.domain.ReportTargetType;
import com.echo.moderation.infrastructure.ReportRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ReportService 단위 테스트 — 리포지토리를 mock으로 검증.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("접수하면 PENDING 신고를 저장하고 반환한다")
    void submit_savesPending() {
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        Report result = reportService.submit(1L, ReportTargetType.POST, 10L, "스팸");

        assertEquals(ReportStatus.PENDING, result.getStatus());
        assertEquals(1L, result.getReporterId());
        assertEquals(10L, result.getTargetId());
    }

    @Test
    @DisplayName("상태 전이는 로드한 신고에 위임한다(PENDING → REVIEWED)")
    void changeStatus_delegates() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "사유");
        when(reportRepository.findById(7L)).thenReturn(Optional.of(report));

        Report result = reportService.changeStatus(7L, ReportStatus.REVIEWED);

        assertEquals(ReportStatus.REVIEWED, result.getStatus());
    }

    @Test
    @DisplayName("없는 신고를 처리하면 ReportNotFoundException")
    void changeStatus_notFound_throws() {
        when(reportRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReportNotFoundException.class,
            () -> reportService.changeStatus(99L, ReportStatus.REVIEWED));
    }

    @Test
    @DisplayName("허용되지 않은 전이는 IllegalReportStatusTransitionException")
    void changeStatus_invalid_throws() {
        Report report = new Report(1L, ReportTargetType.POST, 10L, "사유");
        when(reportRepository.findById(7L)).thenReturn(Optional.of(report));

        assertThrows(IllegalReportStatusTransitionException.class,
            () -> reportService.changeStatus(7L, ReportStatus.RESOLVED));
    }
}
