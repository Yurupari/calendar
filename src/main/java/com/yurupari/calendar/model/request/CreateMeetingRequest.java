package com.yurupari.calendar.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateMeetingRequest(
        @NotNull(message = "Host ID is required")
        Long hostId,

        @NotNull
        String timezone,

        @NotNull(message = "Slot ID is required")
        Long slotId,

        @NotNull(message = "Title is required")
        String title,

        String description,

        List<Long> participants
) {
}
