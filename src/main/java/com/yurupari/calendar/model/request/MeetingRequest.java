package com.yurupari.calendar.model.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MeetingRequest(
        @NotNull(message = "Host ID cannot be null")
        Long hostId,

        @NotNull(message = "Title cannot be null")
        String title,

        String description,

        List<Long> participantIds
) {
}
