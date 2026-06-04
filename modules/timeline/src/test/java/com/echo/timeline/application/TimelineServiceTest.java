package com.echo.timeline.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.echo.timeline.domain.FeedItem;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TimelineService 단위 테스트 — Spring 컨텍스트 없이 순수 POJO로 검증.
 */
class TimelineServiceTest {

    private final TimelineService timelineService = new TimelineService();

    @Test
    @DisplayName("후보 글을 createdAt 내림차순(최신순)으로 정렬한다")
    void assembleLatest_sortsByCreatedAtDescending() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0);
        FeedItem older = new FeedItem(1L, 10L, "older", base);
        FeedItem newest = new FeedItem(2L, 10L, "newest", base.plusHours(2));
        FeedItem middle = new FeedItem(3L, 10L, "middle", base.plusHours(1));

        // 뒤섞인 입력 → 최신순으로 조립되어야 한다
        List<FeedItem> feed = timelineService.assembleLatest(List.of(older, newest, middle));

        assertEquals(List.of(newest, middle, older), feed);
    }

    @Test
    @DisplayName("빈 후보 목록은 빈 피드를 반환한다")
    void assembleLatest_emptyCandidates_returnsEmpty() {
        assertTrue(timelineService.assembleLatest(List.of()).isEmpty());
    }
}
