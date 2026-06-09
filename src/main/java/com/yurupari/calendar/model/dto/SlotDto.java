package com.yurupari.calendar.model.dto;

import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

@Builder
public record SlotDto(
        Long id,

        @NotNull(message = "Calendar ID is required")
        Long calendarId,

        Long meetingId,

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        Instant startTime,

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        Instant endTime,

        SlotStatus status,

        ParticipantRole role
) {
}
