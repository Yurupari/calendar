package com.yurupari.calendar.model.response;

import com.yurupari.calendar.model.dto.UserDto;
import lombok.Builder;

import java.util.List;

@Builder
public record MeetingResponse(
        Long id,
        UserDto host,
        String title,
        String description,
        List<UserDto> participants
) {
}
