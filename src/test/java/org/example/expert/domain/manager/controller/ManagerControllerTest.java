package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ManagerController.class)
class ManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ManagerService managerService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void 매니저_저장이_정상적으로_처리된다() throws Exception {
        // given
        long todoId = 1L;
        ManagerSaveRequest request = new ManagerSaveRequest(2L);

        UserResponse userResponse = new UserResponse(2L, "manager@test.com");
        ManagerSaveResponse expectedResponse = new ManagerSaveResponse(
            1L,
            userResponse
        );

        given(managerService.saveManager(any(), anyLong(), any(ManagerSaveRequest.class)))
            .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/managers", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", 1L)
                .requestAttr("email", "test@test.com")
                .requestAttr("userRole", "USER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.user.id").value(2L));
    }

    @Test
    void 매니저_목록_조회가_정상적으로_처리된다() throws Exception {
        // given
        long todoId = 1L;

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        ManagerResponse managerResponse = new ManagerResponse(
            1L,
            userResponse
        );

        List<ManagerResponse> expectedList = List.of(managerResponse);

        given(managerService.getManagers(anyLong())).willReturn(expectedList);

        // when & then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].user.id").value(1L));
    }

    @Test
    void 매니저_삭제가_정상적으로_처리된다() throws Exception {
        // given
        String bearerToken = "Bearer test.jwt.token";
        long todoId = 1L;
        long managerId = 1L;
        long userId = 1L;

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn(String.valueOf(userId));
        given(jwtUtil.extractClaims("test.jwt.token")).willReturn(claims);

        // when & then
        mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                .header("Authorization", bearerToken))
            .andExpect(status().isOk());
    }
}
