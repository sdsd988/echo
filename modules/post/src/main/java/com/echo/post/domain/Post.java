package com.echo.post.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Post 도메인 엔티티. Echo 서비스의 글(텍스트 한 조각).
 * 내용(content)은 280자를 초과할 수 없다 — 도메인 불변식.
 */
@Entity
@Table(name = "posts")
public class Post {

    public static final int MAX_CONTENT_LENGTH = 280;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = MAX_CONTENT_LENGTH)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Post() {
    }

    public Post(Long authorId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 비어 있을 수 없습니다.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                "content는 %d자를 초과할 수 없습니다. 현재: %d자".formatted(MAX_CONTENT_LENGTH, content.length())
            );
        }
        this.authorId = authorId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
