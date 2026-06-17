package com.echo.follow.application;

import com.echo.follow.domain.AlreadyFollowingException;
import com.echo.follow.domain.Follow;
import com.echo.follow.infrastructure.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 팔로우 도메인 애플리케이션 서비스. 팔로우/언팔로우 유스케이스.
 *
 * <p>대상 유저의 존재 여부(404)는 이 서비스의 책임이 아니다 — 모듈 간 직접 의존을 피하기 위해
 * 조립 지점(컨트롤러)에서 user 모듈로 검증한 뒤 호출한다.
 */
@Service
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    /**
     * 팔로우한다. 이미 팔로우 중이면 {@link AlreadyFollowingException}(→ 409),
     * 자기 자신이면 {@link IllegalArgumentException}(→ 400)을 던진다.
     */
    @Transactional
    public Follow follow(Long followerId, Long followeeId) {
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new AlreadyFollowingException(followerId, followeeId);
        }
        // 자기 자신 팔로우 금지 등 불변식은 도메인 생성자에서 강제된다.
        return followRepository.save(new Follow(followerId, followeeId));
    }

    /** 언팔로우한다. 팔로우 중이 아니어도 오류 없이 처리한다(멱등). */
    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }
}
