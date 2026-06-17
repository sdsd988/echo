package com.echo.follow.application;

import com.echo.follow.domain.Follow;
import com.echo.follow.infrastructure.FollowRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 팔로우 조회 전용 서비스(읽기 측). 팔로우/언팔로우 같은 쓰기는 {@link FollowService}가 담당한다.
 *
 * <p>읽기와 쓰기를 분리해 의존 방향과 트랜잭션 경계를 단순하게 유지한다.
 */
@Service
@Transactional(readOnly = true)
public class FollowQueryService {

    private final FollowRepository followRepository;

    public FollowQueryService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    /** followerId가 followeeId를 팔로우 중인지. */
    public boolean isFollowing(Long followerId, Long followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    /** userId의 팔로워 수(나를 팔로우하는 사람 수). */
    public long countFollowers(Long userId) {
        return followRepository.countByFolloweeId(userId);
    }

    /** userId의 팔로잉 수(내가 팔로우하는 사람 수). */
    public long countFollowings(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    /** userId가 팔로우하는 유저 id 목록(타임라인 팬아웃 등에 사용). */
    public List<Long> findFollowingIds(Long userId) {
        return followRepository.findByFollowerId(userId).stream()
                .map(Follow::getFolloweeId)
                .toList();
    }

    /** userId를 팔로우하는 유저 id 목록. */
    public List<Long> findFollowerIds(Long userId) {
        return followRepository.findByFolloweeId(userId).stream()
                .map(Follow::getFollowerId)
                .toList();
    }
}
