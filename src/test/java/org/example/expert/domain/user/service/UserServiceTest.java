package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void 사용자_조회_시_사용자가_없으면_예외가_발생한다() {
        // given
        long userId = 1L;
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.getUser(userId)
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void 사용자_조회가_정상적으로_처리된다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(userId);

        // then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@test.com", response.getEmail());
    }

    @Test
    void 비밀번호_변경_시_새_비밀번호가_8자_미만이면_예외가_발생한다() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("old123", "New1");

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, request)
        );
        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
    }

    @Test
    void 비밀번호_변경_시_새_비밀번호에_숫자가_없으면_예외가_발생한다() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("old123", "NewPassword");

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, request)
        );
        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
    }

    @Test
    void 비밀번호_변경_시_새_비밀번호에_대문자가_없으면_예외가_발생한다() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("old123", "newpassword123");

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, request)
        );
        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
    }

    @Test
    void 비밀번호_변경_시_사용자가_없으면_예외가_발생한다() {
        // given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPassword1", "NewPassword1");
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, request)
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void 비밀번호_변경_시_새_비밀번호가_기존_비밀번호와_같으면_예외가_발생한다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPassword1", "NewPassword1");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("NewPassword1", user.getPassword())).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, request)
        );
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    void 비밀번호_변경_시_기존_비밀번호가_일치하지_않으면_예외가_발생한다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("WrongOldPassword1", "NewPassword1");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("NewPassword1", user.getPassword())).willReturn(false);
        given(passwordEncoder.matches("WrongOldPassword1", user.getPassword())).willReturn(false);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            userService.changePassword(userId, request)
        );
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    void 비밀번호_변경이_정상적으로_처리된다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "encodedOldPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPassword1", "NewPassword1");

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(passwordEncoder.matches("NewPassword1", user.getPassword())).willReturn(false);
        given(passwordEncoder.matches("OldPassword1", user.getPassword())).willReturn(true);
        given(passwordEncoder.encode("NewPassword1")).willReturn("encodedNewPassword");

        // when & then
        assertDoesNotThrow(() -> userService.changePassword(userId, request));
    }
}
