package com.yurupari.calendar.model.response;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String name,
        String lastName,
        String email,
        Long calendarId
) {
}
