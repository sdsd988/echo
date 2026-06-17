package com.echo.follow.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.echo.follow.domain.AlreadyFollowingException;
import com.echo.follow.domain.Follow;
import com.echo.follow.infrastructure.FollowRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FollowService 단위 테스트 — Spring 컨텍스트 없이 리포지토리를 mock으로 검증.
 */
@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowService followService;

    @Test
    @DisplayName("팔로우 중이 아니면 새 Follow를 저장하고 반환한다")
    void follow_success_saves() {
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenAnswer(inv -> inv.getArgument(0));

        Follow result = followService.follow(1L, 2L);

        assertEquals(1L, result.getFollowerId());
        assertEquals(2L, result.getFolloweeId());

        ArgumentCaptor<Follow> saved = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(saved.capture());
        assertEquals(1L, saved.getValue().getFollowerId());
        assertEquals(2L, saved.getValue().getFolloweeId());
    }

    @Test
    @DisplayName("이미 팔로우 중이면 AlreadyFollowingException을 던지고 저장하지 않는다")
    void follow_alreadyFollowing_throws() {
        when(followRepository.existsByFollowerIdAndFolloweeId(1L, 2L)).thenReturn(true);

        assertThrows(AlreadyFollowingException.class, () -> followService.follow(1L, 2L));

        verify(followRepository, never()).save(any());
    }

    @Test
    @DisplayName("자기 자신을 팔로우하면 IllegalArgumentException을 던지고 저장하지 않는다")
    void follow_self_throws() {
        // existsBy는 false(기본값)로 통과한 뒤 도메인 생성자에서 불변식이 막는다.
        assertThrows(IllegalArgumentException.class, () -> followService.follow(1L, 1L));

        verify(followRepository, never()).save(any());
    }

    @Test
    @DisplayName("언팔로우는 리포지토리 삭제에 위임한다(멱등)")
    void unfollow_delegatesToRepository() {
        followService.unfollow(1L, 2L);

        verify(followRepository).deleteByFollowerIdAndFolloweeId(1L, 2L);
    }
}
