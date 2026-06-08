package com.yurupari.calendar.model.request;

import lombok.Builder;

@Builder
public record UpdateUserRequest(
        String name,
        String lastName,
        String email,
        String timezone
) {
}
