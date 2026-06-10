package com.yurupari.calendar.controller.v1;

import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.request.CreateUserRequest;
import com.yurupari.calendar.model.request.UpdateUserRequest;
import com.yurupari.calendar.service.UserService;
import com.yurupari.calendar.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerV1Test {

    @InjectMocks
    private UserControllerV1 userController;

    @Mock
    private UserService userService;

    @Test
    void createUser_Success() {
        var createUserRequest = TestModelFactory.createTestCreateUserRequest(
                "John", "Doe", "john.doe@example.com", "UTC");
        var userResponse = TestModelFactory.createTestUserResponse(
                1L, "John", "Doe", "john.doe@example.com", 10L, "UTC");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userResponse);

        var responseEntity = userController.createUser(createUserRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(userResponse, responseEntity.getBody());

        verify(userService, times(1)).createUser(createUserRequest);
    }

    @Test
    void getUserById_Success() {
        var userResponse = TestModelFactory.createTestUserResponse(
                1L, "John", "Doe", "john.doe@example.com", 10L, "UTC");

        when(userService.getUserById(anyLong())).thenReturn(userResponse);

        var responseEntity = userController.getUserById(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(userResponse, responseEntity.getBody());

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userService.getUserById(anyLong())).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> userController.getUserById(1L));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserByEmail_Success() {
        var userResponse = TestModelFactory.createTestUserResponse(
                1L, "John", "Doe", "john.doe@example.com", 10L, "UTC");

        when(userService.getUserByEmail(anyString())).thenReturn(userResponse);

        var responseEntity = userController.getUserByEmail("john.doe@example.com");

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(userResponse, responseEntity.getBody());

        verify(userService, times(1)).getUserByEmail("john.doe@example.com");
    }

    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        when(userService.getUserByEmail(anyString())).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(UserNotFoundException.class, () -> userController.getUserByEmail("nonexistent@example.com"));

        verify(userService, times(1)).getUserByEmail("nonexistent@example.com");
    }

    @Test
    void updateUser_Success() {
        var updateUserRequest = TestModelFactory.createTestUpdateUserRequest(
                "Jane", "Smith", "jane.smith@example.com", "Europe/London");

        doNothing().when(userService).updateUser(anyLong(), any(UpdateUserRequest.class));

        var responseEntity = userController.updateUser(1L, updateUserRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User updated successfully", responseEntity.getBody());

        verify(userService, times(1)).updateUser(1L, updateUserRequest);
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        var updateUserRequest = TestModelFactory.createTestUpdateUserRequest(
                "Jane", "Smith", "jane.smith@example.com", "Europe/London");

        doThrow(new UserNotFoundException("User not found")).when(userService).updateUser(anyLong(), any(UpdateUserRequest.class));

        assertThrows(UserNotFoundException.class, () -> userController.updateUser(1L, updateUserRequest));

        verify(userService, times(1)).updateUser(1L, updateUserRequest);
    }

    @Test
    void deleteUser_Success() {
        doNothing().when(userService).deleteUser(anyLong());

        var responseEntity = userController.deleteUser(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(anyLong());

        assertThrows(UserNotFoundException.class, () -> userController.deleteUser(1L));

        verify(userService, times(1)).deleteUser(1L);
    }
}