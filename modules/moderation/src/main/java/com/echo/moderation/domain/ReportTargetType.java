package com.echo.moderation.domain;

/**
 * 신고 대상의 종류. 대상 식별은 {@code (targetType, targetId)} 쌍으로 한다.
 */
public enum ReportTargetType {
    /** 게시물 신고. */
    POST,
    /** 사용자 신고. */
    USER
}
