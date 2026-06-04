package com.echo.api.post;

import com.echo.common.ApiResponse;
import com.echo.post.application.PostService;
import com.echo.post.domain.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 글(Post) HTTP API 컨트롤러.
 * X-User-Id 헤더로 작성자를 식별한다 (인증은 15장에서 도입).
 */
@RestController
@RequestMapping("/posts")
@Tag(name = "Post", description = "글 작성 API")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "글 작성", description = "새 글을 작성합니다. X-User-Id 헤더로 작성자를 지정합니다.")
    public ApiResponse<PostResponse> createPost(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @Valid @RequestBody CreatePostRequest request) {

        Post post = postService.create(userId, request.content());
        return ApiResponse.ok(PostResponse.from(post), "글이 작성되었습니다.");
    }

    @GetMapping("/{id}")
    @Operation(summary = "글 단건 조회", description = "id로 글 하나를 조회합니다. 없으면 404.")
    public ApiResponse<PostResponse> getPost(@PathVariable Long id) {
        // 도메인/application은 Spring Web에 의존하지 않으므로, 없는 글에 대한 404는 컨트롤러에서 처리한다.
        Post post = postService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "글을 찾을 수 없습니다. id=" + id));
        return ApiResponse.ok(PostResponse.from(post));
    }

    @GetMapping
    @Operation(summary = "글 목록 조회", description = "최신 글부터 정렬한 전체 목록을 조회합니다.")
    public ApiResponse<List<PostResponse>> getPosts() {
        List<PostResponse> posts = postService.findLatest().stream()
                .map(PostResponse::from)
                .toList();
        return ApiResponse.ok(posts);
    }
}
