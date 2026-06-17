package com.echo.engagement.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 좋아요 관계 엔티티. {@code userId}가 {@code postId} 게시물에 좋아요를 누른다.
 *
 * <p>같은 (post, user) 쌍은 한 번만 존재할 수 있다(DB 유니크 제약 + 애플리케이션 검증).
 * domain 계층이므로 Spring 의존 없이 POJO + JPA만 사용한다.
 */
@Entity
@Table(
    name = "likes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_like_post_user",
        columnNames = {"post_id", "user_id"}
    )
)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Like() {
    }

    public Like(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new IllegalArgumentException("postId/userId는 null일 수 없습니다.");
        }
        this.postId = postId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
