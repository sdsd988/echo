package com.echo.api.follow;

import com.echo.common.ApiResponse;
import com.echo.follow.application.FollowQueryService;
import com.echo.follow.application.FollowService;
import com.echo.follow.domain.AlreadyFollowingException;
import com.echo.follow.domain.Follow;
import com.echo.user.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 팔로우 HTTP API 컨트롤러.
 *
 * <p>follow와 user 두 모듈을 잇는 조립 지점(composition root)이다.
 * 대상 유저 존재 여부(404)는 user 모듈로 검증하고, 팔로우 관계 자체는 follow 모듈에 위임한다.
 * 두 모듈은 서로 직접 의존하지 않는다.
 *
 * <p>팔로우 주체는 X-User-Id 헤더로 식별한다(인증은 범위 외).
 */
@RestController
@RequestMapping("/follows")
@Tag(name = "Follow", description = "팔로우 API")
public class FollowController {

    private final FollowService followService;
    private final FollowQueryService followQueryService;
    private final UserService userService;

    public FollowController(FollowService followService,
                           FollowQueryService followQueryService,
                           UserService userService) {
        this.followService = followService;
        this.followQueryService = followQueryService;
        this.userService = userService;
    }

    @PostMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "팔로우", description = "X-User-Id가 targetUserId를 팔로우합니다. 없는 유저 404, 이미 팔로우 중 409.")
    public ApiResponse<FollowResponse> follow(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long targetUserId) {

        requireUserExists(targetUserId);
        try {
            Follow follow = followService.follow(userId, targetUserId);
            return ApiResponse.ok(FollowResponse.from(follow), "팔로우했습니다.");
        } catch (AlreadyFollowingException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            // 자기 자신 팔로우 등 도메인 불변식 위반
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{targetUserId}")
    @Operation(summary = "언팔로우", description = "X-User-Id가 targetUserId 팔로우를 취소합니다. 없는 유저 404. 멱등.")
    public ApiResponse<Void> unfollow(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable Long targetUserId) {

        requireUserExists(targetUserId);
        followService.unfollow(userId, targetUserId);
        return ApiResponse.ok(null, "언팔로우했습니다.");
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 조회", description = "userId를 팔로우하는 유저 id 목록과 수를 조회합니다. 없는 유저 404.")
    public ApiResponse<FollowersResponse> getFollowers(@PathVariable Long userId) {
        requireUserExists(userId);
        return ApiResponse.ok(FollowersResponse.of(userId, followQueryService.findFollowerIds(userId)));
    }

    /** 대상 유저가 없으면 404. domain/application은 Spring Web에 의존하지 않으므로 여기서 매핑한다. */
    private void requireUserExists(Long targetUserId) {
        if (!userService.exists(targetUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다. id=" + targetUserId);
        }
    }
}
