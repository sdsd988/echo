package com.echo.follow.infrastructure;

import com.echo.follow.domain.Follow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Follow JPA 리포지토리. infrastructure 계층.
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /** 해당 팔로우 관계가 이미 존재하는지. */
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    /** 팔로우 관계를 삭제하고 삭제된 행 수를 반환한다(없으면 0 — 언팔로우는 멱등). */
    long deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    /** userId의 팔로워 수(나를 팔로우하는 사람 수). */
    long countByFolloweeId(Long followeeId);

    /** userId의 팔로잉 수(내가 팔로우하는 사람 수). */
    long countByFollowerId(Long followerId);

    /** userId가 팔로우하는 관계 목록(팔로잉). */
    List<Follow> findByFollowerId(Long followerId);

    /** userId를 팔로우하는 관계 목록(팔로워). */
    List<Follow> findByFolloweeId(Long followeeId);
}
