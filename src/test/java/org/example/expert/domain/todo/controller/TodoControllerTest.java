package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void Todo_생성이_정상적으로_처리된다() throws Exception {
        // given
        TodoSaveRequest request = new TodoSaveRequest("Title", "Contents");

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        TodoSaveResponse expectedResponse = new TodoSaveResponse(
            1L,
            "Title",
            "Contents",
            "Sunny",
            userResponse
        );

        given(todoService.saveTodo(any(), any(TodoSaveRequest.class)))
            .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", 1L)
                .requestAttr("email", "test@test.com")
                .requestAttr("userRole", "USER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("Title"))
            .andExpect(jsonPath("$.contents").value("Contents"))
            .andExpect(jsonPath("$.weather").value("Sunny"));
    }

    @Test
    void Todo_목록_조회가_정상적으로_처리된다() throws Exception {
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

        // when & then
        mockMvc.perform(get("/todos")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].title").value("Title"))
            .andExpect(jsonPath("$.content[0].contents").value("Contents"));
    }

    @Test
    void 특정_Todo_조회가_정상적으로_처리된다() throws Exception {
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

        // when & then
        mockMvc.perform(get("/todos/{todoId}", todoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(todoId))
            .andExpect(jsonPath("$.title").value("Title"))
            .andExpect(jsonPath("$.contents").value("Contents"))
            .andExpect(jsonPath("$.weather").value("Sunny"));
    }
}
