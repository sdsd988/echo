package com.echo.api.follow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FollowersResponse DTO 단위 테스트.
 */
class FollowersResponseTest {

    @Test
    @DisplayName("of는 목록 크기로 count를 채운다")
    void of_setsCountFromListSize() {
        FollowersResponse response = FollowersResponse.of(2L, List.of(1L, 3L));

        assertEquals(2L, response.userId());
        assertEquals(2, response.count());
        assertEquals(List.of(1L, 3L), response.followerIds());
    }

    @Test
    @DisplayName("팔로워가 없으면 count는 0")
    void of_empty_countZero() {
        FollowersResponse response = FollowersResponse.of(5L, List.of());

        assertEquals(0, response.count());
        assertEquals(List.of(), response.followerIds());
    }
}
