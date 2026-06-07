package com.yurupari.calendar.model.dto;

import jakarta.validation.constraints.NotNull;

public record UserDto(
        Long id,

        @NotNull(message = "Name is required")
        String name,

        @NotNull(message = "Last name is required")
        String lastName,

        @NotNull(message = "Email is required")
        String email
) {
}
