package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HttpServletRequest httpServletRequest;

    private AuthUserArgumentResolver authUserArgumentResolver;

    @BeforeEach
    void setUp() {
        authUserArgumentResolver = new AuthUserArgumentResolver();
    }

    @Test
    void Auth_어노테이션과_AuthUser_타입이_모두_있으면_true를_반환한다() {
        // given
        given(methodParameter.getParameterAnnotation(Auth.class)).willReturn(mock(Auth.class));
        given(methodParameter.getParameterType()).willReturn((Class) AuthUser.class);

        // when
        boolean result = authUserArgumentResolver.supportsParameter(methodParameter);

        // then
        assertTrue(result);
    }

    @Test
    void Auth_어노테이션과_AuthUser_타입이_모두_없으면_false를_반환한다() {
        // given
        given(methodParameter.getParameterAnnotation(Auth.class)).willReturn(null);
        given(methodParameter.getParameterType()).willReturn((Class) String.class);

        // when
        boolean result = authUserArgumentResolver.supportsParameter(methodParameter);

        // then
        assertFalse(result);
    }

    @Test
    void Auth_어노테이션은_있지만_AuthUser_타입이_아니면_예외가_발생한다() {
        // given
        given(methodParameter.getParameterAnnotation(Auth.class)).willReturn(mock(Auth.class));
        given(methodParameter.getParameterType()).willReturn((Class) String.class);

        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            authUserArgumentResolver.supportsParameter(methodParameter)
        );
        assertEquals("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.", exception.getMessage());
    }

    @Test
    void AuthUser_타입이지만_Auth_어노테이션이_없으면_예외가_발생한다() {
        // given
        given(methodParameter.getParameterAnnotation(Auth.class)).willReturn(null);
        given(methodParameter.getParameterType()).willReturn((Class) AuthUser.class);

        // when & then
        AuthException exception = assertThrows(AuthException.class, () ->
            authUserArgumentResolver.supportsParameter(methodParameter)
        );
        assertEquals("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.", exception.getMessage());
    }

    @Test
    void request_속성에서_AuthUser를_올바르게_생성한다() {
        // given
        Long userId = 1L;
        String email = "test@test.com";
        String userRole = "USER";

        given(webRequest.getNativeRequest()).willReturn(httpServletRequest);
        given(httpServletRequest.getAttribute("userId")).willReturn(userId);
        given(httpServletRequest.getAttribute("email")).willReturn(email);
        given(httpServletRequest.getAttribute("userRole")).willReturn(userRole);

        // when
        Object result = authUserArgumentResolver.resolveArgument(
            methodParameter, null, webRequest, null
        );

        // then
        assertNotNull(result);
        assertTrue(result instanceof AuthUser);

        AuthUser authUser = (AuthUser) result;
        assertEquals(userId, authUser.getId());
        assertEquals(email, authUser.getEmail());
        assertEquals(UserRole.USER, authUser.getUserRole());
    }

    @Test
    void ADMIN_권한으로_AuthUser를_올바르게_생성한다() {
        // given
        Long userId = 2L;
        String email = "admin@test.com";
        String userRole = "ADMIN";

        given(webRequest.getNativeRequest()).willReturn(httpServletRequest);
        given(httpServletRequest.getAttribute("userId")).willReturn(userId);
        given(httpServletRequest.getAttribute("email")).willReturn(email);
        given(httpServletRequest.getAttribute("userRole")).willReturn(userRole);

        // when
        Object result = authUserArgumentResolver.resolveArgument(
            methodParameter, null, webRequest, null
        );

        // then
        assertNotNull(result);
        assertTrue(result instanceof AuthUser);

        AuthUser authUser = (AuthUser) result;
        assertEquals(userId, authUser.getId());
        assertEquals(email, authUser.getEmail());
        assertEquals(UserRole.ADMIN, authUser.getUserRole());
    }
}
