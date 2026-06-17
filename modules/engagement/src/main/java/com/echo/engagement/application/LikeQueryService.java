package com.echo.engagement.application;

import com.echo.engagement.infrastructure.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 조회 전용 서비스(읽기 측). 좋아요/취소 같은 쓰기는 {@link LikeService}가 담당한다.
 *
 * <p>읽기와 쓰기를 분리해 의존 방향과 트랜잭션 경계를 단순하게 유지한다.
 */
@Service
@Transactional(readOnly = true)
public class LikeQueryService {

    private final LikeRepository likeRepository;

    public LikeQueryService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    /** 게시물의 좋아요 수. */
    public long countByPost(Long postId) {
        return likeRepository.countByPostId(postId);
    }
}
