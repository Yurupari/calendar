package com.yurupari.calendar.model.mapper;

import com.yurupari.calendar.model.dto.SlotDto;
import com.yurupari.calendar.model.entity.Slot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SlotMapper {

    @Mapping(source = "calendar.id", target = "calendarId")
    @Mapping(source = "meeting.id", target = "meetingId")
    SlotDto toDto(Slot entity);

    @Mapping(source = "calendarId", target = "calendar.id")
    @Mapping(target = "status", constant = "FREE")
    @Mapping(target = "meeting", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Slot toEntity(SlotDto dto);

    @Mapping(source = "meetingId", target = "meeting.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "calendar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SlotDto dto, @MappingTarget Slot entity);
}
