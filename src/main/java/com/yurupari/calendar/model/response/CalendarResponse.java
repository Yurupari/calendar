package com.yurupari.calendar.model.response;

import lombok.Builder;

@Builder
public record CalendarResponse(
        Long id,
        String timezone,
        Long userId
) {
}
