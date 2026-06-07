package com.yurupari.calendar.model.dto;

import jakarta.validation.constraints.NotNull;

public record MeetingDto(
        Long id,

        @NotNull(message = "Host ID is required")
        Long hostId,

        @NotNull(message = "Title is required")
        String title,

        String description
) {
}
