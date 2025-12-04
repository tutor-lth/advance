package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void InvalidRequestException을_처리하면_BAD_REQUEST를_반환한다() {
        // given
        String errorMessage = "잘못된 요청입니다.";
        InvalidRequestException exception = new InvalidRequestException(errorMessage);

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.invalidRequestExceptionException(exception);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().get("status"));
        assertEquals(400, response.getBody().get("code"));
        assertEquals(errorMessage, response.getBody().get("message"));
    }

    @Test
    void AuthException을_처리하면_UNAUTHORIZED를_반환한다() {
        // given
        String errorMessage = "인증에 실패했습니다.";
        AuthException exception = new AuthException(errorMessage);

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleAuthException(exception);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().get("status"));
        assertEquals(401, response.getBody().get("code"));
        assertEquals(errorMessage, response.getBody().get("message"));
    }

    @Test
    void ServerException을_처리하면_INTERNAL_SERVER_ERROR를_반환한다() {
        // given
        String errorMessage = "서버 오류가 발생했습니다.";
        ServerException exception = new ServerException(errorMessage);

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleServerException(exception);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().get("status"));
        assertEquals(500, response.getBody().get("code"));
        assertEquals(errorMessage, response.getBody().get("message"));
    }

    @Test
    void getErrorResponse가_올바른_에러_응답_형식을_반환한다() {
        // given
        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = "리소스를 찾을 수 없습니다.";

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.getErrorResponse(status, message);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().get("status"));
        assertEquals(404, response.getBody().get("code"));
        assertEquals(message, response.getBody().get("message"));
    }
}
