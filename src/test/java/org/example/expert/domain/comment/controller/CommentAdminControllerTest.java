package org.example.expert.domain.comment.controller;

import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CommentAdminController.class)
class CommentAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentAdminService commentAdminService;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void 댓글_삭제가_정상적으로_처리된다() throws Exception {
        // given
        long commentId = 1L;

        // when & then
        mockMvc.perform(delete("/admin/comments/{commentId}", commentId))
            .andExpect(status().isOk());
    }
}
