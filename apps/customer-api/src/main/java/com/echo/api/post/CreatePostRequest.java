package com.echo.api.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * POST /posts 요청 DTO.
 * content는 도메인에서도 280자 제한을 강제하지만, Bean Validation으로 HTTP 400을 먼저 반환한다.
 */
public record CreatePostRequest(
    @NotBlank(message = "content는 비어 있을 수 없습니다.")
    @Size(max = 280, message = "content는 280자를 초과할 수 없습니다.")
    String content
) {
}
