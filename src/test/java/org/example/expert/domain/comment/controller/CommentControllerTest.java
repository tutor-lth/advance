package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    void 댓글_저장이_정상적으로_처리된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("Test comment");

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        CommentSaveResponse expectedResponse = new CommentSaveResponse(
            1L,
            "Test comment",
            userResponse
        );

        given(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class)))
            .willReturn(expectedResponse);

        // when
        ResponseEntity<CommentSaveResponse> response = commentController.saveComment(authUser, todoId, request);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Test comment", response.getBody().getContents());
        verify(commentService).saveComment(authUser, todoId, request);
    }

    @Test
    void 댓글_목록_조회가_정상적으로_처리된다() {
        // given
        long todoId = 1L;

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        CommentResponse commentResponse = new CommentResponse(
            1L,
            "Test comment",
            userResponse
        );

        List<CommentResponse> expectedList = List.of(commentResponse);

        given(commentService.getComments(anyLong())).willReturn(expectedList);

        // when
        ResponseEntity<List<CommentResponse>> response = commentController.getComments(todoId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test comment", response.getBody().get(0).getContents());
        verify(commentService).getComments(todoId);
    }
}
