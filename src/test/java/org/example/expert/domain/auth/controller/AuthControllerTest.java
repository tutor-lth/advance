package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void 회원가입이_정상적으로_처리된다() throws Exception {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "Password123", "USER");
        SignupResponse expectedResponse = new SignupResponse("token123");

        given(authService.signup(any(SignupRequest.class))).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bearerToken").value("token123"));
    }

    @Test
    void 로그인이_정상적으로_처리된다() throws Exception {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "Password123");
        SigninResponse expectedResponse = new SigninResponse("token456");

        given(authService.signin(any(SigninRequest.class))).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bearerToken").value("token456"));
    }
}
