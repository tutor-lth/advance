package org.example.expert.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminApiLogAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AdminApiLogAspect adminApiLogAspect;

    @BeforeEach
    void setUp() {
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);
    }

    @Test
    void Admin_API_로그를_기록하고_메서드를_실행한다() throws Throwable {
        // given
        Long userId = 1L;
        String requestUri = "/admin/users";
        Object expectedResult = "result";

        given(request.getAttribute("userId")).willReturn(userId);
        given(request.getRequestURI()).willReturn(requestUri);
        given(joinPoint.proceed()).willReturn(expectedResult);

        // when
        Object result = adminApiLogAspect.logAdminApi(joinPoint);

        // then
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        verify(request).getAttribute("userId");
        verify(request).getRequestURI();
    }

    @Test
    void userId가_null인_경우에도_로그를_기록하고_메서드를_실행한다() throws Throwable {
        // given
        String requestUri = "/admin/users";
        Object expectedResult = "result";

        given(request.getAttribute("userId")).willReturn(null);
        given(request.getRequestURI()).willReturn(requestUri);
        given(joinPoint.proceed()).willReturn(expectedResult);

        // when
        Object result = adminApiLogAspect.logAdminApi(joinPoint);

        // then
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        verify(request).getAttribute("userId");
        verify(request).getRequestURI();
    }

    @Test
    void 다른_userId로_Admin_API_로그를_기록한다() throws Throwable {
        // given
        Long userId = 999L;
        String requestUri = "/admin/comments";
        Object expectedResult = "deleted";

        given(request.getAttribute("userId")).willReturn(userId);
        given(request.getRequestURI()).willReturn(requestUri);
        given(joinPoint.proceed()).willReturn(expectedResult);

        // when
        Object result = adminApiLogAspect.logAdminApi(joinPoint);

        // then
        assertEquals(expectedResult, result);
        verify(joinPoint).proceed();
        verify(request).getAttribute("userId");
        verify(request).getRequestURI();
    }

    @Test
    void joinPoint_실행_중_예외가_발생하면_그대로_전파된다() throws Throwable {
        // given
        Long userId = 1L;
        String requestUri = "/admin/users";
        RuntimeException expectedException = new RuntimeException("Test exception");

        given(request.getAttribute("userId")).willReturn(userId);
        given(request.getRequestURI()).willReturn(requestUri);
        given(joinPoint.proceed()).willThrow(expectedException);

        // when & then
        try {
            adminApiLogAspect.logAdminApi(joinPoint);
        } catch (RuntimeException e) {
            assertEquals(expectedException, e);
        }

        verify(joinPoint).proceed();
        verify(request).getAttribute("userId");
        verify(request).getRequestURI();
    }
}
