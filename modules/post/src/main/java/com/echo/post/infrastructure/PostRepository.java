package com.echo.post.infrastructure;

import com.echo.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Post JPA 리포지토리. infrastructure 계층.
 */
public interface PostRepository extends JpaRepository<Post, Long> {
}
