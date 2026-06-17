package com.echo.engagement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.echo.engagement.domain.AlreadyLikedException;
import com.echo.engagement.domain.Like;
import com.echo.engagement.infrastructure.LikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * LikeService 단위 테스트 — Spring 컨텍스트 없이 리포지토리를 mock으로 검증.
 */
@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    LikeRepository likeRepository;

    @InjectMocks
    LikeService likeService;

    @Test
    @DisplayName("좋아요하면 저장 결과를 반환한다")
    void like_saves() {
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(false);
        when(likeRepository.save(any())).thenReturn(new Like(1L, 2L));

        Like result = likeService.like(1L, 2L);

        assertEquals(1L, result.getPostId());
        assertEquals(2L, result.getUserId());
    }

    @Test
    @DisplayName("이미 좋아요했으면 AlreadyLikedException, 저장하지 않는다")
    void like_duplicate_throws() {
        when(likeRepository.existsByPostIdAndUserId(1L, 2L)).thenReturn(true);

        assertThrows(AlreadyLikedException.class, () -> likeService.like(1L, 2L));
        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 취소는 멱등 — 없어도 오류 없이 호출된다")
    void unlike_idempotent() {
        likeService.unlike(1L, 2L);
        verify(likeRepository).deleteByPostIdAndUserId(1L, 2L);
    }
}
