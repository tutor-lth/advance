package org.example.expert.domain.todo.controller;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    @Test
    void Todo_생성이_정상적으로_처리된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        TodoSaveRequest request = new TodoSaveRequest("Title", "Contents");

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        TodoSaveResponse expectedResponse = new TodoSaveResponse(
            1L,
            "Title",
            "Contents",
            "Sunny",
            userResponse
        );

        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class)))
            .willReturn(expectedResponse);

        // when
        ResponseEntity<TodoSaveResponse> response = todoController.saveTodo(authUser, request);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Title", response.getBody().getTitle());
        assertEquals("Contents", response.getBody().getContents());
        verify(todoService).saveTodo(authUser, request);
    }

    @Test
    void Todo_목록_조회가_정상적으로_처리된다() {
        // given
        int page = 1;
        int size = 10;

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        TodoResponse todoResponse = new TodoResponse(
            1L,
            "Title",
            "Contents",
            "Sunny",
            userResponse,
            null,
            null
        );

        Page<TodoResponse> expectedPage = new PageImpl<>(List.of(todoResponse));

        given(todoService.getTodos(anyInt(), anyInt())).willReturn(expectedPage);

        // when
        ResponseEntity<Page<TodoResponse>> response = todoController.getTodos(page, size);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(todoService).getTodos(page, size);
    }

    @Test
    void 특정_Todo_조회가_정상적으로_처리된다() {
        // given
        long todoId = 1L;

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        TodoResponse expectedResponse = new TodoResponse(
            todoId,
            "Title",
            "Contents",
            "Sunny",
            userResponse,
            null,
            null
        );

        given(todoService.getTodo(anyLong())).willReturn(expectedResponse);

        // when
        ResponseEntity<TodoResponse> response = todoController.getTodo(todoId);

        // then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(todoId, response.getBody().getId());
        assertEquals("Title", response.getBody().getTitle());
        verify(todoService).getTodo(todoId);
    }
}
