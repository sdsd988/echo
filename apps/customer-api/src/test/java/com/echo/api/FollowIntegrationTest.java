package com.echo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 팔로우 API 통합 테스트. follow + user 모듈 조립과 HTTP 상태 매핑을 검증한다.
 * 데모 시더가 유저 1(alice)/2(bob)/3(carol)을 만든다.
 * 각 테스트는 @Transactional로 팔로우 데이터를 롤백해 서로 간섭하지 않는다(시드 유저는 기동 시 커밋되어 유지됨).
 */
@Transactional
class FollowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /follows/{id} — 정상 팔로우 시 201")
    void follow_success() throws Exception {
        mockMvc.perform(post("/follows/{targetUserId}", 2).header("X-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.followerId").value(1))
                .andExpect(jsonPath("$.data.followeeId").value(2));
    }

    @Test
    @DisplayName("POST /follows/{id} — 없는 유저면 404")
    void follow_unknownTarget_404() throws Exception {
        mockMvc.perform(post("/follows/{targetUserId}", 999999).header("X-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /follows/{id} — 이미 팔로우 중이면 409")
    void follow_duplicate_409() throws Exception {
        mockMvc.perform(post("/follows/{targetUserId}", 2).header("X-User-Id", "1"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/follows/{targetUserId}", 2).header("X-User-Id", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /follows/{id} — 자기 자신은 팔로우 불가 400")
    void follow_self_400() throws Exception {
        mockMvc.perform(post("/follows/{targetUserId}", 1).header("X-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /follows/{id} — 언팔로우 시 200")
    void unfollow_success() throws Exception {
        mockMvc.perform(post("/follows/{targetUserId}", 2).header("X-User-Id", "1"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/follows/{targetUserId}", 2).header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /follows/{id} — 팔로우 중이 아니어도 200(멱등)")
    void unfollow_idempotent_200() throws Exception {
        mockMvc.perform(delete("/follows/{targetUserId}", 3).header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /follows/{id}/followers — 팔로워 id 목록과 수를 반환한다")
    void getFollowers_success() throws Exception {
        // 1, 3이 2를 팔로우 → 2의 팔로워는 [1, 3]
        mockMvc.perform(post("/follows/{targetUserId}", 2).header("X-User-Id", "1"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/follows/{targetUserId}", 2).header("X-User-Id", "3"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/follows/{userId}/followers", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(2))
                .andExpect(jsonPath("$.data.count").value(2))
                .andExpect(jsonPath("$.data.followerIds", org.hamcrest.Matchers.containsInAnyOrder(1, 3)));
    }

    @Test
    @DisplayName("GET /follows/{id}/followers — 팔로워가 없으면 빈 목록과 count 0")
    void getFollowers_empty() throws Exception {
        mockMvc.perform(get("/follows/{userId}/followers", 3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0))
                .andExpect(jsonPath("$.data.followerIds").isEmpty());
    }

    @Test
    @DisplayName("GET /follows/{id}/followers — 없는 유저면 404")
    void getFollowers_unknownUser_404() throws Exception {
        mockMvc.perform(get("/follows/{userId}/followers", 999999))
                .andExpect(status().isNotFound());
    }
}
