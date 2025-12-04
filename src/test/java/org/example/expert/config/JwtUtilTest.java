package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Base64로 인코딩된 256비트 시크릿 키 설정
        String secretKey = Base64.getEncoder().encodeToString(
            "test-secret-key-for-jwt-token-generation-and-validation-purpose".getBytes()
        );
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        jwtUtil.init();
    }

    @Test
    void JWT_토큰_생성이_정상적으로_처리된다() {
        // given
        Long userId = 1L;
        String email = "test@test.com";
        UserRole userRole = UserRole.USER;

        // when
        String token = jwtUtil.createToken(userId, email, userRole);

        // then
        assertNotNull(token);
        assertTrue(token.startsWith("Bearer "));
    }

    @Test
    void Bearer_토큰에서_순수_토큰을_추출한다() {
        // given
        String bearerToken = "Bearer test-token-value";

        // when
        String token = jwtUtil.substringToken(bearerToken);

        // then
        assertEquals("test-token-value", token);
    }

    @Test
    void 토큰_추출_시_Bearer_prefix가_없으면_예외가_발생한다() {
        // given
        String invalidToken = "test-token-without-bearer";

        // when & then
        ServerException exception = assertThrows(ServerException.class, () ->
            jwtUtil.substringToken(invalidToken)
        );
        assertEquals("Not Found Token", exception.getMessage());
    }

    @Test
    void 토큰_추출_시_토큰이_비어있으면_예외가_발생한다() {
        // given
        String emptyToken = "";

        // when & then
        ServerException exception = assertThrows(ServerException.class, () ->
            jwtUtil.substringToken(emptyToken)
        );
        assertEquals("Not Found Token", exception.getMessage());
    }

    @Test
    void JWT_토큰에서_Claims를_정상적으로_추출한다() {
        // given
        Long userId = 1L;
        String email = "test@test.com";
        UserRole userRole = UserRole.USER;

        String bearerToken = jwtUtil.createToken(userId, email, userRole);
        String token = jwtUtil.substringToken(bearerToken);

        // when
        Claims claims = jwtUtil.extractClaims(token);

        // then
        assertNotNull(claims);
        assertEquals(String.valueOf(userId), claims.getSubject());
        assertEquals(email, claims.get("email"));
        assertEquals(userRole.name(), claims.get("userRole"));
    }
}
