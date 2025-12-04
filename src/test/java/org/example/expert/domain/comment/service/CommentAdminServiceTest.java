package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentAdminServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private CommentAdminService commentAdminService;

    @Test
    void 댓글_삭제가_정상적으로_처리된다() {
        // given
        long commentId = 1L;
        willDoNothing().given(commentRepository).deleteById(anyLong());

        // when & then
        assertDoesNotThrow(() -> commentAdminService.deleteComment(commentId));
        verify(commentRepository, times(1)).deleteById(commentId);
    }
}
