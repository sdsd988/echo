package com.echo.post.application;

import com.echo.post.domain.Post;
import com.echo.post.infrastructure.PostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Post 도메인 애플리케이션 서비스. 글 작성·조회 유스케이스.
 */
@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public Post create(Long authorId, String content) {
        Post post = new Post(authorId, content);
        return postRepository.save(post);
    }

    /** 단건 조회. 존재 여부 판단(404 등 HTTP 처리)은 호출자(컨트롤러) 몫이라 Optional로 반환한다. */
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    /** 최신 글부터 정렬한 목록 조회. */
    public List<Post> findLatest() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }
}
