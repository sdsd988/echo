package com.echo.follow.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.echo.follow.domain.Follow;
import com.echo.follow.infrastructure.FollowRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FollowQueryService 단위 테스트 — Spring 컨텍스트 없이 리포지토리를 mock으로 검증.
 */
@ExtendWith(MockitoExtension.class)
class FollowQueryServiceTest {

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowQueryService followQueryService;

    @Test
    @DisplayName("isFollowing은 리포지토리의 존재 여부를 그대로 반환한다")
    void isFollowing_delegates() {
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 3L)).thenReturn(false);

        assertTrue(followQueryService.isFollowing(1L, 2L));
        assertFalse(followQueryService.isFollowing(1L, 3L));
    }

    @Test
    @DisplayName("countFollowers는 followee 기준 개수를 반환한다")
    void countFollowers_byFolloweeId() {
        when(followRepository.countByFolloweeId(10L)).thenReturn(3L);

        assertEquals(3L, followQueryService.countFollowers(10L));
    }

    @Test
    @DisplayName("countFollowings는 follower 기준 개수를 반환한다")
    void countFollowings_byFollowerId() {
        when(followRepository.countByFollowerId(10L)).thenReturn(5L);

        assertEquals(5L, followQueryService.countFollowings(10L));
    }

    @Test
    @DisplayName("findFollowingIds는 내가 팔로우하는 관계에서 followeeId만 추출한다")
    void findFollowingIds_extractsFolloweeIds() {
        when(followRepository.findByFollowerId(1L)).thenReturn(List.of(
                new Follow(1L, 2L),
                new Follow(1L, 3L)));

        assertEquals(List.of(2L, 3L), followQueryService.findFollowingIds(1L));
    }

    @Test
    @DisplayName("findFollowerIds는 나를 팔로우하는 관계에서 followerId만 추출한다")
    void findFollowerIds_extractsFollowerIds() {
        when(followRepository.findByFolloweeId(2L)).thenReturn(List.of(
                new Follow(1L, 2L),
                new Follow(3L, 2L)));

        assertEquals(List.of(1L, 3L), followQueryService.findFollowerIds(2L));
    }
}
