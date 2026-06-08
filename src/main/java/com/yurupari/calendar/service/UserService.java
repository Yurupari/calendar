package com.yurupari.calendar.service;

import com.yurupari.calendar.model.request.CreateUserRequest;
import com.yurupari.calendar.model.request.UpdateUserRequest;
import com.yurupari.calendar.model.response.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    void updateUser(Long id, UpdateUserRequest updateUserRequest);

    void deleteUser(Long id);
}
