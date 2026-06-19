package com.echo.moderation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 금칙어 엔티티. 목록은 DB로 관리하며(하드코딩 금지), 운영 중 추가/삭제할 수 있다.
 *
 * <p>domain 계층이므로 Spring 의존 없이 POJO + JPA만 사용한다.
 */
@Entity
@Table(
    name = "banned_words",
    uniqueConstraints = @UniqueConstraint(name = "uk_banned_word", columnNames = "word")
)
public class BannedWord {

    public static final int MAX_WORD_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_WORD_LENGTH)
    private String word;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected BannedWord() {
    }

    public BannedWord(String word) {
        if (word == null || word.isBlank()) {
            throw new IllegalArgumentException("금칙어는 비어 있을 수 없습니다.");
        }
        if (word.length() > MAX_WORD_LENGTH) {
            throw new IllegalArgumentException(
                "금칙어는 %d자를 초과할 수 없습니다.".formatted(MAX_WORD_LENGTH));
        }
        this.word = word;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getWord() { return word; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
