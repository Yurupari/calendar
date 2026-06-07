package com.yurupari.calendar.model.dto;

import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;

import java.time.LocalDateTime;

public record SlotDto(
        Long id,
        Long calendarId,
        Long meetingId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SlotStatus status,
        ParticipantRole role
) {
}
