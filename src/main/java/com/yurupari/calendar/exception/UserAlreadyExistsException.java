package com.yurupari.calendar.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super(String.format("User already exists: email=%s", email));
    }
}
