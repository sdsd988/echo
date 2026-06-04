package com.echo.timeline.domain;

import java.time.LocalDateTime;

/**
 * 타임라인을 구성하는 한 항목.
 *
 * <p>timeline 모듈이 소유하는 중립 표현이다. post 모듈을 import하지 않으며,
 * 앱 계층(customer-api)이 글 데이터를 이 record로 변환해 넘겨준다.
 */
public record FeedItem(Long postId, Long authorId, String content, LocalDateTime createdAt) {
}
