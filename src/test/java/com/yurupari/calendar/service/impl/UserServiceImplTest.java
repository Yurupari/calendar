package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.UserAlreadyExistsException;
import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.dto.UserDto;
import com.yurupari.calendar.model.entity.User;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.mapper.UserMapperImpl;
import com.yurupari.calendar.model.request.CreateUserRequest;
import com.yurupari.calendar.model.request.UpdateUserRequest;
import com.yurupari.calendar.repository.UserRepository;
import com.yurupari.calendar.service.CalendarService;
import com.yurupari.calendar.utils.TestModelFactory;
import com.yurupari.calendar.validator.UserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private CalendarService calendarService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Spy
    private UserMapperImpl userMapper = new UserMapperImpl();

    @Test
    void createUser_UserDoesNotExists_Success() {
        var createUserRequest = TestModelFactory.createTestCreateUserRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "UTC"
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(user);

        var calendar = TestModelFactory.createTestCalendarDto(10L, "UTC", 1L);
        when(calendarService.createCalendar(any())).thenReturn(calendar);

        var userResponse = userService.createUser(createUserRequest);

        assertNotNull(userResponse);
        assertNotNull(userResponse.id());
        assertEquals(1L, userResponse.id());
        assertNotNull(userResponse.name());
        assertNotNull(userResponse.lastName());
        assertNotNull(userResponse.email());
        assertNotNull(userResponse.calendarId());
        assertEquals("UTC", userResponse.timezone());
        assertEquals(10L, userResponse.calendarId());

        verify(userValidator, times(1)).validateUser(any(UserDto.class));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(calendarService, times(1)).createCalendar(any(CalendarDto.class));
        verify(calendarService, never()).activateCalendar(anyLong());
    }

    @Test
    void createUser_UserAlreadyExistsAndActive_ThrowsException() {
        var createUserRequest = CreateUserRequest.builder()
                .name("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .timezone("UTC")
                .build();

        var existingUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));

        verify(userValidator, times(1)).validateUser(any(UserDto.class));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(calendarService, never()).createCalendar(any(CalendarDto.class));
        verify(calendarService, never()).activateCalendar(anyLong());
    }

    @Test
    void createUser_UserAlreadyExistsAndInactive_ReactivatesUser() {
        var createUserRequest = CreateUserRequest.builder()
                .name("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .timezone("UTC")
                .build();

        var inactiveUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.INACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(inactiveUser));

        var updatedUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        var calendar = TestModelFactory.createTestCalendarDto(10L, "UTC", 1L);
        when(calendarService.activateCalendar(anyLong())).thenReturn(calendar);

        var userResponse = userService.createUser(createUserRequest);

        assertNotNull(userResponse);
        assertEquals(Status.ACTIVE, updatedUser.getStatus());
        assertEquals(1L, userResponse.id());
        assertEquals(10L, userResponse.calendarId());
        assertEquals("UTC", userResponse.timezone());

        verify(userValidator, times(1)).validateUser(any(UserDto.class));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userMapper, times(1)).updateEntityFromDto(any(UserDto.class), any(User.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(calendarService, times(1)).activateCalendar(anyLong());
        verify(calendarService, never()).createCalendar(any(CalendarDto.class));
    }

    @Test
    void getUserById_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        var calendar = TestModelFactory.createTestCalendarDto(10L, "UTC", 1L);
        when(calendarService.getCalendarByUserId(anyLong())).thenReturn(calendar);

        var userResponse = userService.getUserById(1L);

        assertNotNull(userResponse);
        assertEquals(1L, userResponse.id());
        assertEquals("John", userResponse.name());
        assertEquals("Doe", userResponse.lastName());
        assertEquals("john.doe@example.com", userResponse.email());
        assertEquals(10L, userResponse.calendarId());
        assertEquals("UTC", userResponse.timezone());

        verify(userRepository, times(1)).findById(anyLong());
        verify(calendarService, times(1)).getCalendarByUserId(anyLong());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(calendarService, never()).getCalendarByUserId(anyLong());
    }

    @Test
    void getUserById_InactiveUser_ThrowsException() {
        var inactiveUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.INACTIVE);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(inactiveUser));

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(calendarService, never()).getCalendarByUserId(anyLong());
    }

    @Test
    void getUserByEmail_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        var calendar = TestModelFactory.createTestCalendarDto(10L, "UTC", 1L);
        when(calendarService.getCalendarByUserId(anyLong())).thenReturn(calendar);

        var userResponse = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(userResponse);
        assertEquals(1L, userResponse.id());
        assertEquals("John", userResponse.name());
        assertEquals("Doe", userResponse.lastName());
        assertEquals("john.doe@example.com", userResponse.email());
        assertEquals(10L, userResponse.calendarId());
        assertEquals("UTC", userResponse.timezone());

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(calendarService, times(1)).getCalendarByUserId(anyLong());
    }

    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("nonexistent@example.com"));

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(calendarService, never()).getCalendarByUserId(anyLong());
    }

    @Test
    void getUserByEmail_InactiveUser_ThrowsException() {
        var inactiveUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.INACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(inactiveUser));

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("john.doe@example.com"));

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(calendarService, never()).getCalendarByUserId(anyLong());
    }

    @Test
    void updateUser_Success() {
        var updateUserRequest = UpdateUserRequest.builder()
                .name("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .timezone("America/Los_Angeles")
                .build();

        var existingUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.updateUser(1L, updateUserRequest);

        verify(userValidator, times(1)).validateUser(any(UserDto.class));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, times(1)).updateEntityFromDto(any(UserDto.class), any(User.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(calendarService, times(1)).updateCalendar(anyLong(), anyString());
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        var updateUserRequest = UpdateUserRequest.builder()
                .name("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .timezone("America/Los_Angeles")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updateUserRequest));

        verify(userValidator, times(1)).validateUser(any(UserDto.class));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, never()).updateEntityFromDto(any(UserDto.class), any(User.class));
        verify(userRepository, never()).save(any(User.class));
        verify(calendarService, never()).updateCalendar(anyLong(), anyString());
    }

    @Test
    void updateUser_InactiveUser_ThrowsException() {
        var updateUserRequest = UpdateUserRequest.builder()
                .name("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .timezone("America/Los_Angeles")
                .build();

        var inactiveUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.INACTIVE);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(inactiveUser));

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updateUserRequest));

        verify(userValidator, times(1)).validateUser(any(UserDto.class));
        verify(userRepository, times(1)).findById(anyLong());
        verify(userMapper, never()).updateEntityFromDto(any(UserDto.class), any(User.class));
        verify(userRepository, never()).save(any(User.class));
        verify(calendarService, never()).updateCalendar(anyLong(), anyString());
    }

    @Test
    void deleteUser_Success() {
        var existingUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        userService.deleteUser(1L);

        assertEquals(Status.INACTIVE, existingUser.getStatus());
        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(any(User.class));
        verify(calendarService, times(1)).deleteCalendarByUserId(anyLong());
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(calendarService, never()).deleteCalendarByUserId(anyLong());
    }

    @Test
    void deleteUser_InactiveUser_ThrowsException() {
        var inactiveUser = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.INACTIVE);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(inactiveUser));

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(calendarService, never()).deleteCalendarByUserId(anyLong());
    }

    @Test
    void getUsers_Success() {
        var userIds = List.of(1L, 2L);
        var user1 = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        var user2 = TestModelFactory.createTestUser(2L, "jane.doe@example.com", Status.ACTIVE);
        var userEntities = List.of(user1, user2);
        var userDto1 = TestModelFactory.createTestUserDto(1L, "John", "Doe", "john.doe@example.com");
        var userDto2 = TestModelFactory.createTestUserDto(2L, "Jane", "Doe", "jane.doe@example.com");

        when(userRepository.findAllById(userIds)).thenReturn(userEntities);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto1, userDto2);

        var result = userService.getUsers(userIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("john.doe@example.com", result.get(0).email());
        assertEquals(2L, result.get(1).id());
        assertEquals("jane.doe@example.com", result.get(1).email());

        verify(userRepository, times(1)).findAllById(userIds);
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    @Test
    void getUsers_EmptyList_ReturnsEmptyList() {
        List<Long> emptyUserIds = List.of();

        when(userRepository.findAllById(emptyUserIds)).thenReturn(List.of());

        var result = userService.getUsers(emptyUserIds);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findAllById(emptyUserIds);
        verify(userMapper, never()).toDto(any(User.class));
    }

    @Test
    void getUsersByMeetingId_Success() {
        Long meetingId = 100L;
        var user1 = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        var user2 = TestModelFactory.createTestUser(2L, "jane.doe@example.com", Status.ACTIVE);
        var userEntities = List.of(user1, user2);
        var userDto1 = TestModelFactory.createTestUserDto(1L, "John", "Doe", "john.doe@example.com");
        var userDto2 = TestModelFactory.createTestUserDto(2L, "Jane", "Doe", "jane.doe@example.com");

        when(userRepository.findUsersByMeetingId(meetingId)).thenReturn(userEntities);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto1, userDto2);

        var result = userService.getUsersByMeetingId(meetingId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("john.doe@example.com", result.get(0).email());
        assertEquals(2L, result.get(1).id());
        assertEquals("jane.doe@example.com", result.get(1).email());

        verify(userRepository, times(1)).findUsersByMeetingId(meetingId);
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    @Test
    void getUsersByMeetingId_EmptyList_ReturnsEmptyList() {
        Long meetingId = 100L;

        when(userRepository.findUsersByMeetingId(meetingId)).thenReturn(List.of());

        var result = userService.getUsersByMeetingId(meetingId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findUsersByMeetingId(meetingId);
        verify(userMapper, never()).toDto(any());
    }
}
