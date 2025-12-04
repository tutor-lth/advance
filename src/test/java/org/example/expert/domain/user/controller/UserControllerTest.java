package org.example.expert.domain.user.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void 특정_사용자_조회가_정상적으로_처리된다() {
        // given
        long userId = 1L;
        UserResponse expectedResponse = new UserResponse(userId, "test@test.com");

        given(userService.getUser(anyLong())).willReturn(expectedResponse);

        // when
        ResponseEntity<UserResponse> response = userController.getUser(userId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        assertEquals("test@test.com", response.getBody().getEmail());
        verify(userService).getUser(userId);
    }

    @Test
    void 비밀번호_변경이_정상적으로_처리된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass123", "NewPass123");

        // when
        userController.changePassword(authUser, request);

        // then
        verify(userService).changePassword(authUser.getId(), request);
    }
}
