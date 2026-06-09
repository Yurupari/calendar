package com.yurupari.calendar.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record MeetingDto(
        Long id,

        @NotNull(message = "Host ID cannot be null")
        Long hostId,

        @NotNull(message = "Title cannot be null")
        String title,

        String description
) {
}
