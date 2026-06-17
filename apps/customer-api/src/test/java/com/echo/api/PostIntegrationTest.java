package com.echo.api;

import com.echo.api.post.CreatePostRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * POST /posts 통합 테스트.
 * Web + JPA + PostgreSQL(Testcontainers) + 크로스-모듈 의존성이 전부 연결되는지 검증한다.
 */
class PostIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /posts — 정상 글 작성 시 201 반환")
    void createPost_success() throws Exception {
        CreatePostRequest request = new CreatePostRequest("안녕, echo!");

        mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("안녕, echo!"));
    }

    @Test
    @DisplayName("POST /posts — 280자 초과 시 400 반환")
    void createPost_contentTooLong_400() throws Exception {
        // 281자 content
        String longContent = "a".repeat(281);
        CreatePostRequest request = new CreatePostRequest(longContent);

        mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /posts/{id} — 작성한 글을 단건 조회")
    void getPost_success() throws Exception {
        Long id = createPost("단건 조회용 글");

        mockMvc.perform(get("/posts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.content").value("단건 조회용 글"));
    }

    @Test
    @DisplayName("GET /posts/{id} — 없는 id면 404 반환")
    void getPost_notFound_404() throws Exception {
        mockMvc.perform(get("/posts/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /posts — 최신 글부터 정렬한 목록 조회")
    void getPosts_latestFirst() throws Exception {
        createPost("첫 번째 글");
        createPost("두 번째 글");

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // 최신 글(두 번째)이 목록 맨 앞에 온다
                .andExpect(jsonPath("$.data[0].content").value("두 번째 글"))
                .andExpect(jsonPath("$.data[1].content").value("첫 번째 글"));
    }

    /** 글을 작성하고 생성된 id를 반환한다. */
    private Long createPost(String content) throws Exception {
        String response = mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreatePostRequest(content))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("id").asLong();
    }
}
