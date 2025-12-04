package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void 회원가입_시_이미_존재하는_이메일이면_예외가_발생한다() {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "password123", "USER");
        given(userRepository.existsByEmail(anyString())).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            authService.signup(request)
        );
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    void 회원가입이_정상적으로_처리된다() {
        // given
        SignupRequest request = new SignupRequest("test@test.com", "password123", "USER");
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        String token = "Bearer testToken";

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);
        given(jwtUtil.createToken(any(), anyString(), any(UserRole.class))).willReturn(token);

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertNotNull(response);
        assertEquals(token, response.getBearerToken());
    }

    @Test
    void 로그인_시_가입되지_않은_이메일이면_예외가_발생한다() {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "password123");
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            authService.signin(request)
        );
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    void 로그인_시_비밀번호가_일치하지_않으면_예외가_발생한다() {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "wrongPassword");
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            authService.signin(request)
        );
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    void 로그인이_정상적으로_처리된다() {
        // given
        SigninRequest request = new SigninRequest("test@test.com", "password123");
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        String token = "Bearer testToken";

        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.createToken(any(), anyString(), any(UserRole.class))).willReturn(token);

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertNotNull(response);
        assertEquals(token, response.getBearerToken());
    }
}
