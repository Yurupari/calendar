package com.yurupari.calendar.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateSlotRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Start time is required")
        String startTime,

        @NotNull(message = "End time is required")
        String endTime
) {
}
