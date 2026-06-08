package com.yurupari.calendar.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super(String.format("User not found: id=%s", userId));
    }

    public UserNotFoundException(String email) {
        super(String.format("User not found: email=%s", email));
    }
}
