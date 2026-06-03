package com.echo.api.post;

import com.echo.common.ApiResponse;
import com.echo.post.application.PostService;
import com.echo.post.domain.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
