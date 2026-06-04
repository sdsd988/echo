package com.echo.post.infrastructure;

import com.echo.post.domain.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Post JPA 리포지토리. infrastructure 계층.
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /** 최신 글부터(작성 시각 내림차순) 전체 조회. */
    List<Post> findAllByOrderByCreatedAtDesc();
}
