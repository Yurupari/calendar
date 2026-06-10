package com.yurupari.calendar.model.request;

import lombok.Builder;

import java.util.List;

@Builder
public record UpdateMeetingRequest(
        String timezone,
        String title,
        String description,
        List<Long> participants
) {
}
