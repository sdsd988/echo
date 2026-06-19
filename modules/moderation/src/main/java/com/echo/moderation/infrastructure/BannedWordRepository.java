package com.echo.moderation.infrastructure;

import com.echo.moderation.domain.BannedWord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * BannedWord JPA 리포지토리. infrastructure 계층.
 */
public interface BannedWordRepository extends JpaRepository<BannedWord, Long> {

    /** 같은 금칙어가 이미 등록되어 있는지(중복 방지). */
    boolean existsByWord(String word);
}
