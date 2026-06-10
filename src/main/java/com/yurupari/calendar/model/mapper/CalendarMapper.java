package com.yurupari.calendar.model.mapper;

import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.entity.Calendar;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CalendarMapper {

    @Mapping(source = "user.id", target = "userId")
    CalendarDto toDto(Calendar entity);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Calendar toEntity(CalendarDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CalendarDto dto, @MappingTarget Calendar entity);
}
