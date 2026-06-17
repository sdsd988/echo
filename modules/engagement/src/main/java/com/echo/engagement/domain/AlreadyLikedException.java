package com.echo.engagement.domain;

/**
 * 이미 좋아요한 게시물에 다시 좋아요를 누르려 할 때 발생하는 도메인 예외.
 *
 * <p>HTTP 상태(409) 변환은 컨트롤러(조립 지점)의 몫이다 — domain은 Spring Web에 의존하지 않는다.
 */
public class AlreadyLikedException extends RuntimeException {

    public AlreadyLikedException(Long postId, Long userId) {
        super("이미 좋아요한 게시물입니다. post=%d, user=%d".formatted(postId, userId));
    }
}
