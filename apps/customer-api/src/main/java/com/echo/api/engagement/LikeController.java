package com.echo.api.engagement;

import com.echo.common.ApiResponse;
import com.echo.engagement.application.LikeQueryService;
import com.echo.engagement.application.LikeService;
import com.echo.engagement.domain.AlreadyLikedException;
import com.echo.engagement.domain.Like;
import com.echo.post.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 좋아요 HTTP API 컨트롤러.
 *
 * <p>engagement와 post 두 모듈을 잇는 조립 지점(composition root)이다.
 * 대상 게시물 존재 여부(404)는 post 모듈로 검증하고, 좋아요 관계 자체는 engagement 모듈에 위임한다.
 * 두 모듈은 서로 직접 의존하지 않는다.
 *
 * <p>좋아요 주체는 X-User-Id 헤더로 식별한다(인증은 범위 외).
 */
@RestController
@RequestMapping("/posts/{postId}/likes")
@Tag(name = "Like", description = "좋아요 API")
public class LikeController {

    private final LikeService likeService;
    private final LikeQueryService likeQueryService;
    private final PostService postService;

    public LikeController(LikeService likeService,
                          LikeQueryService likeQueryService,
                          PostService postService) {
        this.likeService = likeService;
        this.likeQueryService = likeQueryService;
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "좋아요", description = "X-User-Id가 postId에 좋아요합니다. 없는 게시물 404, 이미 좋아요 409.")
    public ApiResponse<LikeResponse> like(
            @PathVariable Long postId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {

        requirePostExists(postId);
        try {
            Like like = likeService.like(postId, userId);
            return ApiResponse.ok(LikeResponse.from(like), "좋아요했습니다.");
        } catch (AlreadyLikedException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "좋아요 취소", description = "X-User-Id가 postId 좋아요를 취소합니다. 없는 게시물 404. 멱등.")
    public ApiResponse<Void> unlike(
            @PathVariable Long postId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {

        requirePostExists(postId);
        likeService.unlike(postId, userId);
        return ApiResponse.ok(null, "좋아요를 취소했습니다.");
    }

    @GetMapping("/count")
    @Operation(summary = "좋아요 수 조회", description = "postId의 좋아요 수를 조회합니다. 없는 게시물 404.")
    public ApiResponse<LikeCountResponse> count(@PathVariable Long postId) {
        requirePostExists(postId);
        return ApiResponse.ok(LikeCountResponse.of(postId, likeQueryService.countByPost(postId)));
    }

    /** 대상 게시물이 없으면 404. domain/application은 Spring Web에 의존하지 않으므로 여기서 매핑한다. */
    private void requirePostExists(Long postId) {
        if (postService.findById(postId).isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "게시물을 찾을 수 없습니다. id=" + postId);
        }
    }
}
