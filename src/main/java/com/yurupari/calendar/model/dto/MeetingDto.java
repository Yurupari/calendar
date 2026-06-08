package com.yurupari.calendar.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record MeetingDto(
        Long id,

        Long hostId,

        String title,

        String description
) {
}
