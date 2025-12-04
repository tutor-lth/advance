package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtFilter jwtFilter;
    private ObjectMapper objectMapper;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        jwtFilter = new JwtFilter(jwtUtil, objectMapper);
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        lenient().when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void auth_경로는_인증_없이_통과한다() throws Exception {
        // given
        given(request.getRequestURI()).willReturn("/auth/signin");

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).substringToken(anyString());
    }

    @Test
    void Authorization_헤더가_없으면_401_에러를_반환한다() throws Exception {
        // given
        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn(null);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(401);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(any(), any());

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("인증이 필요합니다."));
    }

    @Test
    void 유효한_JWT로_인증에_성공한다() throws Exception {
        // given
        String bearerToken = "Bearer valid.jwt.token";
        String jwt = "valid.jwt.token";

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("test@test.com");
        given(claims.get("userRole")).willReturn("USER");
        given(claims.get(eq("userRole"), eq(String.class))).willReturn("USER");

        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(jwt);
        given(jwtUtil.extractClaims(jwt)).willReturn(claims);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(request).setAttribute("userId", 1L);
        verify(request).setAttribute("email", "test@test.com");
        verify(request).setAttribute("userRole", "USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void Claims가_null이면_401_에러를_반환한다() throws Exception {
        // given
        String bearerToken = "Bearer invalid.jwt.token";
        String jwt = "invalid.jwt.token";

        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(jwt);
        given(jwtUtil.extractClaims(jwt)).willReturn(null);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("인증이 필요합니다."));
    }

    @Test
    void USER_권한으로_admin_경로_접근_시_403_에러를_반환한다() throws Exception {
        // given
        String bearerToken = "Bearer valid.jwt.token";
        String jwt = "valid.jwt.token";

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("test@test.com");
        given(claims.get("userRole")).willReturn("USER");
        given(claims.get(eq("userRole"), eq(String.class))).willReturn("USER");

        given(request.getRequestURI()).willReturn("/admin/users");
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(jwt);
        given(jwtUtil.extractClaims(jwt)).willReturn(claims);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(403);
        verify(filterChain, never()).doFilter(any(), any());

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("접근 권한이 없습니다."));
    }

    @Test
    void ADMIN_권한으로_admin_경로_접근에_성공한다() throws Exception {
        // given
        String bearerToken = "Bearer valid.jwt.token";
        String jwt = "valid.jwt.token";

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");
        given(claims.get("email")).willReturn("admin@test.com");
        given(claims.get("userRole")).willReturn("ADMIN");
        given(claims.get(eq("userRole"), eq(String.class))).willReturn("ADMIN");

        given(request.getRequestURI()).willReturn("/admin/users");
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(jwt);
        given(jwtUtil.extractClaims(jwt)).willReturn(claims);

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(request).setAttribute("userId", 1L);
        verify(request).setAttribute("email", "admin@test.com");
        verify(request).setAttribute("userRole", "ADMIN");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void JWT_만료_시_401_에러를_반환한다() throws Exception {
        // given
        String bearerToken = "Bearer expired.jwt.token";
        String jwt = "expired.jwt.token";

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("1");

        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(jwt);
        given(jwtUtil.extractClaims(jwt)).willThrow(new ExpiredJwtException(null, claims, "JWT expired"));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("인증이 필요합니다."));
    }

    @Test
    void JWT_형식이_잘못되면_400_에러를_반환한다() throws Exception {
        // given
        String bearerToken = "Bearer malformed.jwt.token";
        String jwt = "malformed.jwt.token";

        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(jwt);
        given(jwtUtil.extractClaims(jwt)).willThrow(new MalformedJwtException("JWT is malformed"));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(400);
        verify(filterChain, never()).doFilter(any(), any());

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("인증이 필요합니다."));
    }

    @Test
    void 예상치_못한_예외_발생_시_500_에러를_반환한다() throws Exception {
        // given
        String bearerToken = "Bearer valid.jwt.token";
        String jwt = "valid.jwt.token";

        given(request.getRequestURI()).willReturn("/todos");
        given(request.getHeader("Authorization")).willReturn(bearerToken);
        given(jwtUtil.substringToken(bearerToken)).willReturn(jwt);
        given(jwtUtil.extractClaims(jwt)).willThrow(new RuntimeException("Unexpected error"));

        // when
        jwtFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(response).setStatus(500);
        verify(filterChain, never()).doFilter(any(), any());

        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("요청 처리 중 오류가 발생했습니다."));
    }
}
