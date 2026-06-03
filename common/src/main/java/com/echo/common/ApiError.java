package com.echo.common;

/**
 * HTTP 오류 응답 표준 포맷.
 */
public record ApiError(String code, String message) {

    public static ApiError of(String code, String message) {
        return new ApiError(code, message);
    }
}
