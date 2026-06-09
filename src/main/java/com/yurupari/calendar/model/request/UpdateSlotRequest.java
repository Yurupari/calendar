package com.yurupari.calendar.model.request;

import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record UpdateSlotRequest(
        Long meetingId,
        String startTime,
        String endTime,
        SlotStatus status,
        ParticipantRole role
) {
}
