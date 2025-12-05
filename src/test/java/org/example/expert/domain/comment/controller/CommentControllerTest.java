package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private AuthUserArgumentResolver authUserArgumentResolver;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void 댓글_저장이_정상적으로_처리된다() throws Exception {
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("Test comment");

        UserResponse userResponse = new UserResponse(1L, "test@test.com");
        CommentSaveResponse expectedResponse = new CommentSaveResponse(
            1L,
            "Test comment",
            userResponse
        );

        given(commentService.saveComment(any(), anyLong(), any(CommentSaveRequest.class)))
            .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/todos/{todoId}/comments", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", 1L)
                .requestAttr("email", "test@test.com")
                .requestAttr("userRole", "USER"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.contents").value("Test comment"));
    }

    @Test
    void 댓글_목록_조회가_정상적으로_처리된다() throws Exception {
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

        // when & then
        mockMvc.perform(get("/todos/{todoId}/comments", todoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].contents").value("Test comment"));
    }
}
