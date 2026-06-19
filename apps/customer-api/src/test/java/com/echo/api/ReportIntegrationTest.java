package com.echo.api;

import com.echo.api.moderation.CreateReportRequest;
import com.echo.api.moderation.UpdateReportStatusRequest;
import com.echo.moderation.domain.ReportStatus;
import com.echo.moderation.domain.ReportTargetType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 신고(/reports) 통합 테스트 — 접수·조회·상태 전이가 끝까지 연결되는지 검증한다.
 */
class ReportIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /reports — 접수 시 201, 상태는 PENDING")
    void submit_pending_201() throws Exception {
        CreateReportRequest request = new CreateReportRequest(ReportTargetType.POST, 10L, "스팸입니다");

        mockMvc.perform(post("/reports")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reporterId").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /reports — reason이 비면 400")
    void submit_blankReason_400() throws Exception {
        CreateReportRequest request = new CreateReportRequest(ReportTargetType.POST, 10L, "  ");

        mockMvc.perform(post("/reports")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /reports/{id} — 접수한 신고를 단건 조회")
    void get_success() throws Exception {
        Long id = submitReport();

        mockMvc.perform(get("/reports/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /reports/{id} — 없는 id면 404")
    void get_notFound_404() throws Exception {
        mockMvc.perform(get("/reports/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /reports/{id}/status — PENDING → REVIEWED → RESOLVED 전이")
    void changeStatus_fullLifecycle() throws Exception {
        Long id = submitReport();

        changeStatus(id, ReportStatus.REVIEWED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEWED"));

        changeStatus(id, ReportStatus.RESOLVED)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("PATCH /reports/{id}/status — 단계를 건너뛰면 409")
    void changeStatus_skip_409() throws Exception {
        Long id = submitReport();

        changeStatus(id, ReportStatus.RESOLVED)
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /reports/{id}/status — 없는 신고면 404")
    void changeStatus_notFound_404() throws Exception {
        changeStatus(999999L, ReportStatus.REVIEWED)
                .andExpect(status().isNotFound());
    }

    /** 신고를 접수하고 생성된 id를 반환한다. */
    private Long submitReport() throws Exception {
        String response = mockMvc.perform(post("/reports")
                .header("X-User-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateReportRequest(ReportTargetType.POST, 10L, "신고 사유"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.path("data").path("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions changeStatus(Long id, ReportStatus status)
            throws Exception {
        return mockMvc.perform(patch("/reports/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateReportStatusRequest(status))));
    }
}
