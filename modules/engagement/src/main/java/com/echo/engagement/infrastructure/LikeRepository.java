package com.echo.engagement.infrastructure;

import com.echo.engagement.domain.Like;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Like JPA 리포지토리. infrastructure 계층.
 */
public interface LikeRepository extends JpaRepository<Like, Long> {

    /** 해당 좋아요가 이미 존재하는지. */
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    /** 좋아요를 삭제하고 삭제된 행 수를 반환한다(없으면 0 — 좋아요 취소는 멱등). */
    long deleteByPostIdAndUserId(Long postId, Long userId);

    /** 게시물의 좋아요 수. */
    long countByPostId(Long postId);
}
