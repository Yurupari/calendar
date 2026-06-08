package com.yurupari.calendar.model.response;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String name,
        String lastname,
        String email,
        Long calendarId
) {
}
