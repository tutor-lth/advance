package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void 특정_사용자_조회가_정상적으로_처리된다() throws Exception {
        // given
        long userId = 1L;
        UserResponse expectedResponse = new UserResponse(userId, "test@test.com");

        given(userService.getUser(anyLong())).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/users/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void 비밀번호_변경이_정상적으로_처리된다() throws Exception {
        // given
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass123", "NewPass123");

        // when & then
        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", 1L)
                .requestAttr("email", "test@test.com")
                .requestAttr("userRole", "USER"))
            .andExpect(status().isOk());
    }
}
