package com.echo.follow.domain;

/**
 * 이미 팔로우 중인 대상을 다시 팔로우하려 할 때 발생하는 도메인 예외.
 *
 * <p>HTTP 상태(409) 변환은 컨트롤러(조립 지점)의 몫이다 — domain은 Spring Web에 의존하지 않는다.
 */
public class AlreadyFollowingException extends RuntimeException {

    public AlreadyFollowingException(Long followerId, Long followeeId) {
        super("이미 팔로우 중입니다. follower=%d, followee=%d".formatted(followerId, followeeId));
    }
}
