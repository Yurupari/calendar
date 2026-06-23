package com.yurupari.calendar.model.dto;

import com.yurupari.calendar.model.entity.Slot;
import lombok.Builder;

@Builder
public record UserSlotDto(
        Long userId,
        String timezone,
        Slot slot
) {
}
