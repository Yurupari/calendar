package com.yurupari.calendar.model.dto;

public record MeetingDto(
        Long id,
        Long hostId,
        String title,
        String description
) {
}
