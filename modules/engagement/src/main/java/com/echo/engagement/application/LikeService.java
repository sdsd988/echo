package com.echo.engagement.application;

import com.echo.engagement.domain.AlreadyLikedException;
import com.echo.engagement.domain.Like;
import com.echo.engagement.infrastructure.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 애플리케이션 서비스(쓰기 측). 좋아요/좋아요 취소 유스케이스.
 *
 * <p>대상 게시물의 존재 여부(404)는 이 서비스의 책임이 아니다 — 모듈 간 직접 의존을 피하기 위해
 * 조립 지점(컨트롤러)에서 post 모듈로 검증한 뒤 호출한다.
 */
@Service
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;

    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    /** 좋아요한다. 이미 좋아요했으면 {@link AlreadyLikedException}(→ 409)을 던진다. */
    @Transactional
    public Like like(Long postId, Long userId) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new AlreadyLikedException(postId, userId);
        }
        return likeRepository.save(new Like(postId, userId));
    }

    /** 좋아요를 취소한다. 좋아요한 적이 없어도 오류 없이 처리한다(멱등). */
    @Transactional
    public void unlike(Long postId, Long userId) {
        likeRepository.deleteByPostIdAndUserId(postId, userId);
    }
}
