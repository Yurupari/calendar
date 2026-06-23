package com.yurupari.calendar.model.mapper;

import com.yurupari.calendar.model.dto.SlotDto;
import com.yurupari.calendar.model.entity.Slot;
import com.yurupari.calendar.model.response.SlotResponse;
import com.yurupari.calendar.util.TimeUtil;
import org.mapstruct.Context;
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

    @Mapping(target = "calendarId", source = "entity.calendar.id")
    @Mapping(target = "meetingId", source = "entity.meeting.id")
    @Mapping(target = "startTime", expression = "java(timeUtil.parseInstantToIsoString(entity.getStartTime(), timezone))")
    @Mapping(target = "endTime", expression = "java(timeUtil.parseInstantToIsoString(entity.getEndTime(), timezone))")
    @Mapping(target = "status", source = "entity.status")
    @Mapping(target = "role", source = "entity.role")
    SlotResponse toSlotResponse(Slot entity, String timezone, @Context TimeUtil timeUtil);

    @Mapping(source = "meetingId", target = "meeting.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "calendar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SlotDto dto, @MappingTarget Slot entity);
}
