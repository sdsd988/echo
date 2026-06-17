package com.echo.api;

import com.echo.api.post.CreatePostRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /timeline 통합 테스트.
 * post → timeline → HTTP 조립 경로가 모듈 간 직접 의존 없이 앱 계층에서 연결되는지 검증한다.
 */
class TimelineIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /timeline — 작성한 글이 최신순으로 조립되어 내려온다")
    void getTimeline_latestFirst() throws Exception {
        createPost("첫 번째 글");
        createPost("두 번째 글");

        mockMvc.perform(get("/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // 최신 글(두 번째)이 피드 맨 앞에 온다
                .andExpect(jsonPath("$.data[0].content").value("두 번째 글"))
                .andExpect(jsonPath("$.data[1].content").value("첫 번째 글"));
    }

    /** 글을 작성한다. */
    private void createPost(String content) throws Exception {
        mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreatePostRequest(content))))
                .andExpect(status().isCreated());
    }
}
