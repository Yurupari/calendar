package com.yurupari.calendar.model.request;

public record UpdateUserRequest(
        String name,
        String lastname,
        String email,
        String timezone
) {
}
