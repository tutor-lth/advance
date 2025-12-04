package org.example.expert.domain.auth.controller;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void 회원가입이_정상적으로_처리된다() {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "Password123", "USER");
        SignupResponse expectedResponse = new SignupResponse("token123");

        given(authService.signup(any(SignupRequest.class))).willReturn(expectedResponse);

        // when
        SignupResponse response = authController.signup(request);

        // then
        assertNotNull(response);
        assertEquals("token123", response.getBearerToken());
        verify(authService).signup(request);
    }

    @Test
    void 로그인이_정상적으로_처리된다() {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "Password123");
        SigninResponse expectedResponse = new SigninResponse("token456");

        given(authService.signin(any(SigninRequest.class))).willReturn(expectedResponse);

        // when
        SigninResponse response = authController.signin(request);

        // then
        assertNotNull(response);
        assertEquals("token456", response.getBearerToken());
        verify(authService).signin(request);
    }
}
