package org.example.expert.domain.user.controller;

import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserAdminControllerTest {

    @Mock
    private UserAdminService userAdminService;

    @InjectMocks
    private UserAdminController userAdminController;

    @Test
    void 사용자_권한_변경이_정상적으로_처리된다() {
        // given
        long userId = 1L;
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        // when
        userAdminController.changeUserRole(userId, request);

        // then
        verify(userAdminService).changeUserRole(userId, request);
    }
}
