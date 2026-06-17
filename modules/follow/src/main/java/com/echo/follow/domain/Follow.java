package com.echo.follow.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 팔로우 관계 엔티티. {@code followerId}가 {@code followeeId}를 팔로우한다.
 *
 * <p>같은 (follower, followee) 쌍은 한 번만 존재할 수 있다(DB 유니크 제약 + 애플리케이션 검증).
 * domain 계층이므로 Spring 의존 없이 POJO + JPA만 사용한다.
 */
@Entity
@Table(
    name = "follows",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_follow_follower_followee",
        columnNames = {"follower_id", "followee_id"}
    )
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "followee_id", nullable = false)
    private Long followeeId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Follow() {
    }

    public Follow(Long followerId, Long followeeId) {
        if (followerId == null || followeeId == null) {
            throw new IllegalArgumentException("followerId/followeeId는 null일 수 없습니다.");
        }
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getFollowerId() { return followerId; }
    public Long getFolloweeId() { return followeeId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
