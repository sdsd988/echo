package com.echo.common;

/**
 * HTTP 응답 표준 포맷. 성공 시 data 필드에 페이로드를 담는다.
 */
public record ApiResponse<T>(boolean success, T data, String message) {

    /** 성공 응답 — 데이터 있음 */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /** 성공 응답 — 메시지만 */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
}
