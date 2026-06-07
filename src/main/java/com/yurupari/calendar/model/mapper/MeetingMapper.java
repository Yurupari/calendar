package com.yurupari.calendar.model.mapper;

import com.yurupari.calendar.model.dto.MeetingDto;
import com.yurupari.calendar.model.entity.Meeting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MeetingMapper {

    @Mapping(source = "host.id", target = "hostId")
    MeetingDto toDto(Meeting entity);

    @Mapping(source = "hostId", target = "host.id")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Meeting toEntity(MeetingDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "host", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(MeetingDto dto, @MappingTarget Meeting entity);
}
