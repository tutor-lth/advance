package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Test
    void 할일_저장이_정상적으로_처리된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        TodoSaveRequest request = new TodoSaveRequest("Title", "Contents");
        String weather = "Sunny";

        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("Title", "Contents", weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        // when
        TodoSaveResponse response = todoService.saveTodo(authUser, request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Title", response.getTitle());
        assertEquals("Contents", response.getContents());
        assertEquals(weather, response.getWeather());
    }

    @Test
    void 할일_목록_조회가_정상적으로_처리된다() {
        // given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);

        User user = new User("test@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        Page<Todo> todoPage = new PageImpl<>(List.of(todo), pageable, 1);

        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todoPage);

        // when
        Page<TodoResponse> result = todoService.getTodos(page, size);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Title", result.getContent().get(0).getTitle());
    }

    @Test
    void 할일_단건_조회_시_할일이_없으면_예외가_발생한다() {
        // given
        long todoId = 1L;
        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            todoService.getTodo(todoId)
        );
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void 할일_단건_조회가_정상적으로_처리된다() {
        // given
        long todoId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(todoRepository.findByIdWithUser(anyLong())).willReturn(Optional.of(todo));

        // when
        TodoResponse response = todoService.getTodo(todoId);

        // then
        assertNotNull(response);
        assertEquals(todoId, response.getId());
        assertEquals("Title", response.getTitle());
        assertEquals("Contents", response.getContents());
    }
}
