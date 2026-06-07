package com.yurupari.calendar.model.dto;

import jakarta.validation.constraints.NotNull;

public record CalendarDto(
        Long id,

        @NotNull(message = "Timezone is required")
        String timezone,

        @NotNull(message = "User ID is required")
        Long userId
) {
}
