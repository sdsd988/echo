package com.echo.moderation.infrastructure;

import com.echo.moderation.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Report JPA 리포지토리. infrastructure 계층.
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
}
