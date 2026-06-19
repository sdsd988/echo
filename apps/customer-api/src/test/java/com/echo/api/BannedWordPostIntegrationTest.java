package com.echo.api;

import com.echo.api.post.CreatePostRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 금칙어 필터 통합 테스트 — 게시물 등록 시 금칙어(DB 관리, V2 시드)가 포함되면 400으로 막는다.
 * post 모듈과 moderation 모듈이 PostController 조립 지점에서 연결되는지 검증한다.
 */
class BannedWordPostIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /posts — 금칙어가 포함되면 400")
    void createPost_bannedWord_400() throws Exception {
        // 'badword'는 V2 마이그레이션에서 시드된 금칙어다.
        CreatePostRequest request = new CreatePostRequest("이 글에는 badword 가 들어 있다");

        mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /posts — 금칙어가 없으면 정상 201")
    void createPost_clean_201() throws Exception {
        CreatePostRequest request = new CreatePostRequest("깨끗하고 평범한 글입니다");

        mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("깨끗하고 평범한 글입니다"));
    }
}
