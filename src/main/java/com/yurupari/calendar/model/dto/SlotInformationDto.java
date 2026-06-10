package com.yurupari.calendar.model.dto;

import com.yurupari.calendar.model.enums.ParticipantRole;
import lombok.Builder;

@Builder
public record SlotInformationDto(
        Long id,
        Long calendarId,
        ParticipantRole role,
        String startTime,
        String endTime
) {
}
