package org.example.expert.domain.manager.controller;

import io.jsonwebtoken.Claims;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerControllerTest {

    @Mock
    private ManagerService managerService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ManagerController managerController;

    @Test
    void 매니저_저장이_정상적으로_처리된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        long todoId = 1L;
        ManagerSaveRequest request = new ManagerSaveRequest(2L);

        UserResponse userResponse = new UserResponse(2L, "manager@test.com");
        ManagerSaveResponse expectedResponse = new ManagerSaveResponse(
            1L,
            userResponse
        );

        given(managerService.saveManager(any(AuthUser.class), anyLong(), any(ManagerSaveRequest.class)))
            .willReturn(expectedResponse);

        // when
        ResponseEntity<ManagerSaveResponse> response = managerController.saveManager(authUser, todoId, request);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().getUser().getId());
        verify(managerService).saveManager(authUser, todoId, request);
    }

    @Test
    void 매니저_목록_조회가_정상적으로_처리된다() {
        // given
        long todoId = 1L;

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        ManagerResponse managerResponse = new ManagerResponse(
            1L,
            userResponse
        );

        List<ManagerResponse> expectedList = List.of(managerResponse);

        given(managerService.getManagers(anyLong())).willReturn(expectedList);

        // when
        ResponseEntity<List<ManagerResponse>> response = managerController.getMembers(todoId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(managerService).getManagers(todoId);
    }

    @Test
    void 매니저_삭제가_정상적으로_처리된다() {
        // given
        String bearerToken = "Bearer test.jwt.token";
        long todoId = 1L;
        long managerId = 1L;
        long userId = 1L;

        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn(String.valueOf(userId));
        given(jwtUtil.extractClaims("test.jwt.token")).willReturn(claims);

        // when
        managerController.deleteManager(bearerToken, todoId, managerId);

        // then
        verify(jwtUtil).extractClaims("test.jwt.token");
        verify(managerService).deleteManager(userId, todoId, managerId);
    }
}
