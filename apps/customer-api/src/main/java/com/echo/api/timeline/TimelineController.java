package com.echo.api.timeline;

import com.echo.common.ApiResponse;
import com.echo.post.application.PostService;
import com.echo.timeline.application.TimelineService;
import com.echo.timeline.domain.FeedItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 타임라인 HTTP API 컨트롤러.
 *
 * <p>post와 timeline 두 모듈을 잇는 조립 지점(composition root)이다.
 * post에서 글을 받아 timeline의 중립 타입({@link FeedItem})으로 변환해 넘기고,
 * timeline이 조립한 피드를 응답으로 변환한다. 두 모듈은 서로 직접 의존하지 않는다.
 */
@RestController
@RequestMapping("/timeline")
@Tag(name = "Timeline", description = "타임라인 조회 API")
public class TimelineController {

    private final PostService postService;
    private final TimelineService timelineService;

    public TimelineController(PostService postService, TimelineService timelineService) {
        this.postService = postService;
        this.timelineService = timelineService;
    }

    @GetMapping
    @Operation(summary = "타임라인 조회", description = "최신순으로 조립한 피드를 조회합니다.")
    public ApiResponse<List<TimelineItemResponse>> getTimeline() {
        // 1) post 모듈에서 글을 받아 timeline의 중립 타입으로 변환
        List<FeedItem> candidates = postService.findLatest().stream()
                .map(p -> new FeedItem(p.getId(), p.getAuthorId(), p.getContent(), p.getCreatedAt()))
                .toList();

        // 2) timeline이 최신순으로 조립
        List<FeedItem> feed = timelineService.assembleLatest(candidates);

        // 3) HTTP 응답으로 변환 + ApiResponse 래핑
        List<TimelineItemResponse> body = feed.stream()
                .map(TimelineItemResponse::from)
                .toList();
        return ApiResponse.ok(body);
    }
}
