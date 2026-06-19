package com.echo.moderation.application;

import com.echo.moderation.domain.IllegalReportStatusTransitionException;
import com.echo.moderation.domain.Report;
import com.echo.moderation.domain.ReportNotFoundException;
import com.echo.moderation.domain.ReportStatus;
import com.echo.moderation.domain.ReportTargetType;
import com.echo.moderation.infrastructure.ReportRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 신고 애플리케이션 서비스. 신고 접수·조회·상태 처리 유스케이스.
 *
 * <p>대상(게시물/유저)의 존재 여부 검증은 이 서비스의 책임이 아니다 — 모듈 간 직접 의존을 피하기 위해
 * 필요하다면 조립 지점(컨트롤러)에서 해당 모듈로 검증한다.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /** 신고를 접수한다(초기 상태 PENDING). */
    @Transactional
    public Report submit(Long reporterId, ReportTargetType targetType, Long targetId, String reason) {
        return reportRepository.save(new Report(reporterId, targetType, targetId, reason));
    }

    /** 단건 조회. 존재 여부 판단(404)은 호출자(컨트롤러) 몫이라 Optional로 반환한다. */
    public Optional<Report> findById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    /**
     * 신고 상태를 전이한다. 없는 신고면 {@link ReportNotFoundException}(→ 404),
     * 허용되지 않은 전이면 {@link IllegalReportStatusTransitionException}(→ 409)을 던진다.
     */
    @Transactional
    public Report changeStatus(Long reportId, ReportStatus next) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException(reportId));
        report.changeStatus(next);
        return report; // 변경 감지(dirty checking)로 트랜잭션 커밋 시 반영된다.
    }
}
