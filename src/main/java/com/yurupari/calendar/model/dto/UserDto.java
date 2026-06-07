package com.yurupari.calendar.model.dto;

public record UserDto(
        Long id,
        String name,
        String lastName,
        String email
) {
}
