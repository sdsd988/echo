package com.echo.api.timeline;

import com.echo.timeline.domain.FeedItem;
import java.time.LocalDateTime;

/**
 * GET /timeline 응답 항목 DTO.
 */
public record TimelineItemResponse(Long postId, Long authorId, String content, LocalDateTime createdAt) {

    public static TimelineItemResponse from(FeedItem item) {
        return new TimelineItemResponse(
            item.postId(),
            item.authorId(),
            item.content(),
            item.createdAt()
        );
    }
}
