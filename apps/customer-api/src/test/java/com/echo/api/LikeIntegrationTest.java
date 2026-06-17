package com.echo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.echo.api.post.CreatePostRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 API 통합 테스트. engagement + post 모듈 조립과 HTTP 상태 매핑을 검증한다.
 * 게시물은 시드되지 않으므로 각 테스트가 글을 만들어 사용한다.
 * @Transactional로 좋아요/게시물 데이터를 롤백해 서로 간섭하지 않는다(시드 유저는 기동 시 커밋되어 유지됨).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LikeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** 글을 하나 작성하고 그 id를 반환한다. */
    private long createPost() throws Exception {
        String body = mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreatePostRequest("좋아요 대상 글"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.path("data").path("id").asLong();
    }

    @Test
    @DisplayName("POST /posts/{id}/likes — 정상 좋아요 시 201")
    void like_success() throws Exception {
        long postId = createPost();

        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.userId").value(2));
    }

    @Test
    @DisplayName("POST /posts/{id}/likes — 같은 사용자가 중복 좋아요면 409")
    void like_duplicate_409() throws Exception {
        long postId = createPost();

        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "2"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "2"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /posts/{id}/likes — 없는 게시물이면 404")
    void like_unknownPost_404() throws Exception {
        mockMvc.perform(post("/posts/{postId}/likes", 999999).header("X-User-Id", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /posts/{id}/likes — 좋아요 취소 시 200")
    void unlike_success() throws Exception {
        long postId = createPost();
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "2"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/posts/{postId}/likes", postId).header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /posts/{id}/likes — 좋아요한 적 없어도 200(멱등)")
    void unlike_idempotent_200() throws Exception {
        long postId = createPost();

        mockMvc.perform(delete("/posts/{postId}/likes", postId).header("X-User-Id", "2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /posts/{id}/likes/count — 좋아요 수를 반환한다")
    void count_success() throws Exception {
        long postId = createPost();
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "2"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/posts/{postId}/likes/count", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.count").value(2));
    }

    @Test
    @DisplayName("GET /posts/{id}/likes/count — 없는 게시물이면 404")
    void count_unknownPost_404() throws Exception {
        mockMvc.perform(get("/posts/{postId}/likes/count", 999999))
                .andExpect(status().isNotFound());
    }
}
