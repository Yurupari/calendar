package com.yurupari.calendar.model.dto;

public record CalendarDto(
        Long id,
        String timezone,
        Long userId
) {
}
