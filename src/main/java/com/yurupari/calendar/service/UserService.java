package com.yurupari.calendar.service;

import com.yurupari.calendar.model.dto.UserDto;
import com.yurupari.calendar.model.request.CreateUserRequest;
import com.yurupari.calendar.model.request.UpdateUserRequest;
import com.yurupari.calendar.model.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    List<UserDto> getUsers(List<Long> userIds);

    List<UserDto> getUsersByMeetingId(Long meetingId);

    void updateUser(Long id, UpdateUserRequest updateUserRequest);

    void deleteUser(Long id);
}
