package com.yurupari.calendar.model.response;

import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import lombok.Builder;

@Builder
public record SlotResponse(
        Long id,
        Long calendarId,
        Long meetingId,
        String startTime,
        String endTime,
        SlotStatus status,
        ParticipantRole role
) {
}
