package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.repository.UserRepository;
import com.yurupari.calendar.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @InjectMocks
    private UserValidator userValidator;

    @Mock
    private UserRepository userRepository; // Mocked but not used in validateUser, still good practice

    @Test
    void validateUser_ValidEmail_DoesNotThrowException() {
        var userDto = TestModelFactory.createTestUserDto(
                1L,
                "John",
                "Doe",
                "john.doe@example.com");

        assertDoesNotThrow(() -> userValidator.validateUser(userDto));

        verify(userRepository, never()).findByEmail(userDto.email());
    }

    @Test
    void validateUser_InvalidEmailFormat_ThrowsInvalidFormatException() {
        var userDto = TestModelFactory.createTestUserDto(
                1L,
                "John",
                "Doe",
                "invalid-email");

        assertThrows(InvalidFormatException.class, () -> userValidator.validateUser(userDto));

        verify(userRepository, never()).findByEmail(userDto.email());
    }

    @Test
    void validateUser_NullEmail_DoesNotThrowException() {
        var userDto = TestModelFactory.createTestUserDto(
                1L,
                "John",
                "Doe",
                null);

        assertDoesNotThrow(() -> userValidator.validateUser(userDto));

        verify(userRepository, never()).findByEmail(userDto.email());
    }

    @Test
    void validateUser_BlankEmail_DoesNotThrowException() {
        var userDto = TestModelFactory.createTestUserDto(
                1L,
                "John",
                "Doe",
                "   ");

        assertDoesNotThrow(() -> userValidator.validateUser(userDto));

        verify(userRepository, never()).findByEmail(userDto.email());
    }
}