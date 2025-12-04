package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAccessInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AdminAccessInterceptor adminAccessInterceptor;

    @Test
    void ADMIN_권한이_아닌_경우_403_에러를_반환하고_false를_리턴한다() throws Exception {
        // given
        given(request.getAttribute("userRole")).willReturn("USER");

        // when
        boolean result = adminAccessInterceptor.preHandle(request, response, new Object());

        // then
        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 필요합니다.");
    }

    @Test
    void ADMIN_권한인_경우_true를_리턴한다() throws Exception {
        // given
        given(request.getAttribute("userRole")).willReturn("ADMIN");
        given(request.getAttribute("userId")).willReturn(1L);
        given(request.getRequestURI()).willReturn("/admin/users");

        // when
        boolean result = adminAccessInterceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
    }

    @Test
    void userRole이_null인_경우_InvalidRequestException이_발생한다() throws Exception {
        // given
        given(request.getAttribute("userRole")).willReturn(null);

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
            org.example.expert.domain.common.exception.InvalidRequestException.class,
            () -> adminAccessInterceptor.preHandle(request, response, new Object())
        );
    }

    @Test
    void userRole이_잘못된_값인_경우_InvalidRequestException이_발생한다() throws Exception {
        // given
        given(request.getAttribute("userRole")).willReturn("INVALID_ROLE");

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
            org.example.expert.domain.common.exception.InvalidRequestException.class,
            () -> adminAccessInterceptor.preHandle(request, response, new Object())
        );
    }
}
