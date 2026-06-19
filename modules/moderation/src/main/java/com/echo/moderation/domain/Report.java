package com.echo.moderation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 신고 엔티티. {@code reporterId}가 {@code (targetType, targetId)} 대상을 신고한다.
 *
 * <p>상태는 {@link ReportStatus#PENDING}으로 시작하며 {@code PENDING → REVIEWED → RESOLVED}
 * 순서로만 전이한다(불변식은 {@link #changeStatus(ReportStatus)}에서 강제).
 * domain 계층이므로 Spring 의존 없이 POJO + JPA만 사용한다.
 */
@Entity
@Table(name = "reports")
public class Report {

    public static final int MAX_REASON_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(nullable = false, length = MAX_REASON_LENGTH)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Report() {
    }

    public Report(Long reporterId, ReportTargetType targetType, Long targetId, String reason) {
        if (reporterId == null) {
            throw new IllegalArgumentException("reporterId는 null일 수 없습니다.");
        }
        if (targetType == null || targetId == null) {
            throw new IllegalArgumentException("신고 대상(targetType/targetId)은 null일 수 없습니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("신고 사유(reason)는 비어 있을 수 없습니다.");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException(
                "신고 사유는 %d자를 초과할 수 없습니다.".formatted(MAX_REASON_LENGTH));
        }
        this.reporterId = reporterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.status = ReportStatus.PENDING;
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 상태를 {@code next}로 전이한다. 허용되지 않은 전이면
     * {@link IllegalReportStatusTransitionException}을 던진다.
     */
    public void changeStatus(ReportStatus next) {
        if (next == null) {
            throw new IllegalArgumentException("변경할 상태(next)는 null일 수 없습니다.");
        }
        if (!status.canTransitionTo(next)) {
            throw new IllegalReportStatusTransitionException(status, next);
        }
        this.status = next;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getReporterId() { return reporterId; }
    public ReportTargetType getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getReason() { return reason; }
    public ReportStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
