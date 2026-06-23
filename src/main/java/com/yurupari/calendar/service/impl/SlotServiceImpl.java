package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.SlotAlreadyExistsException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.dto.SlotDto;
import com.yurupari.calendar.model.dto.SlotInformationDto;
import com.yurupari.calendar.model.dto.UserSlotDto;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.entity.Slot;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.mapper.SlotMapper;
import com.yurupari.calendar.model.request.CreateSlotRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.model.response.SlotResponse;
import com.yurupari.calendar.repository.SlotRepository;
import com.yurupari.calendar.service.CalendarService;
import com.yurupari.calendar.service.SlotService;
import com.yurupari.calendar.util.TimeUtil;
import com.yurupari.calendar.validator.SlotValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotServiceImpl implements SlotService {

    private final CalendarService calendarService;

    private final SlotValidator slotValidator;

    private final SlotRepository slotRepository;

    private final SlotMapper slotMapper;

    private final TimeUtil timeUtil;

    @Override
    @Transactional
    public SlotResponse createSlot(CreateSlotRequest createSlotRequest) {
        log.info("Creating slot: request={}", createSlotRequest);

        slotValidator.validateDates(createSlotRequest.startTime(), createSlotRequest.endTime());

        var calendarDto = calendarService.getCalendarByUserId(createSlotRequest.userId());

        var startTime = timeUtil.parseIsoStringToInstant(createSlotRequest.startTime(), calendarDto.timezone());
        var endTime = timeUtil.parseIsoStringToInstant(createSlotRequest.endTime(), calendarDto.timezone());

        slotRepository.findByCalendarIdAndStartTimeAndEndTime(
                calendarDto.id(),
                startTime,
                endTime
                ).ifPresent(s -> {
                    throw new SlotAlreadyExistsException(s.getId());
        });

        var slot = SlotDto.builder()
                .startTime(startTime)
                .endTime(endTime)
                .calendarId(calendarDto.id())
                .build();
        var savedSlot = slotRepository.save(slotMapper.toEntity(slot));

        return slotMapper.toSlotResponse(savedSlot, calendarDto.timezone(), timeUtil);
    }

    @Override
    public SlotResponse getSlotById(Long id) {
        log.info("Getting slot: id={}", id);

        var slot = slotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException(id));

        return slotMapper.toSlotResponse(slot, slot.getCalendar().getTimezone(), timeUtil);
    }

    @Override
    public List<SlotResponse> getSlots(Long userId, String from, String until, SlotStatus status) {
        log.info("Getting slots: userId={}, from={}, until={}", userId, from, until);

        var calendarDto = calendarService.getCalendarByUserId(userId);

        var fromInstant = timeUtil.parseIsoStringToInstant(from, calendarDto.timezone());
        var untilInstant = timeUtil.parseIsoStringToInstant(until, calendarDto.timezone());

        return slotRepository.findSlotsWithOptionalStatus(calendarDto.id(), fromInstant, untilInstant, status).stream()
                .map(slot -> slotMapper.toSlotResponse(slot, calendarDto.timezone(), timeUtil))
                .toList();
    }

    @Override
    public Map<Long, List<SlotResponse>> getSlots(Set<Long> userIds, String from, String until, String timezone, SlotStatus status) {
        log.info("Getting slots: userIds=[{}], from={}, until={}", userIds, from, until);

        var fromInstant = timeUtil.parseIsoStringToInstant(from, timezone);
        var untilInstant = timeUtil.parseIsoStringToInstant(until, timezone);

        var userSlots = slotRepository.findSlotsWithOptionalStatusInUsers(userIds, fromInstant, untilInstant, status);

        return userSlots.stream()
                .collect(Collectors.groupingBy(
                        UserSlotDto::userId,
                        Collectors.mapping(
                                userSlot -> slotMapper.toSlotResponse(userSlot.slot(), userSlot.timezone(), timeUtil),
                                Collectors.toList()
                        )
                ));
    }

    @Override
    public List<SlotInformationDto> getSlotInformation(Long meetingId, String timezone) {
        log.info("Getting slot information: meetingId={}", meetingId);

        return slotRepository.findByMeetingId(meetingId).stream()
                .map(slot -> SlotInformationDto.builder()
                        .id(slot.getId())
                        .calendarId(slot.getCalendar().getId())
                        .role(slot.getRole())
                        .startTime(timeUtil.parseInstantToIsoString(slot.getStartTime(), timezone))
                        .endTime(timeUtil.parseInstantToIsoString(slot.getEndTime(), timezone))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void updateSlot(Long id, UpdateSlotRequest updateSlotRequest) {
        log.info("Updating slot: id={}, request={}", id, updateSlotRequest);

        slotValidator.validateDates(updateSlotRequest.startTime(), updateSlotRequest.endTime());

        var slot = slotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException(id));

        var timezone = slot.getCalendar().getTimezone();
        var startTime = timeUtil.parseIsoStringToInstant(updateSlotRequest.startTime(), timezone);
        var endTime = timeUtil.parseIsoStringToInstant(updateSlotRequest.endTime(), timezone);

        slotValidator.validateExistingTimeFrame(slot.getCalendar().getId(), startTime, endTime);

        var slotDto = SlotDto.builder()
                .meetingId(updateSlotRequest.meetingId())
                .startTime(startTime)
                .endTime(endTime)
                .status(updateSlotRequest.status())
                .role(updateSlotRequest.role())
                .build();
        slotMapper.updateEntityFromDto(slotDto, slot);
        if (updateSlotRequest.meetingId() == null) slot.setMeeting(null);

        slotRepository.save(slot);
    }

    @Override
    @Transactional
    public void updateSlots(Set<Long> ids, UpdateSlotRequest updateSlotRequest) {
        log.info("Updating slots: ids=[{}], request={}", ids, updateSlotRequest);

        var slots = slotRepository.findAllById(ids);

        slots.forEach(slot -> {
            var slotDto = SlotDto.builder()
                    .meetingId(updateSlotRequest.meetingId())
                    .status(updateSlotRequest.status())
                    .role(updateSlotRequest.role())
                    .build();

            slotMapper.updateEntityFromDto(slotDto, slot);
        });

        slotRepository.saveAll(slots);
    }

    @Override
    @Transactional
    public void deleteSlot(Long id) {
        log.info("Deleting slot: id={}", id);

        var slot = slotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException(id));

        if (Optional.ofNullable(slot.getMeeting()).isPresent()) {
            throw new SlotConflictException(String.format("Slot have an associated meeting: id=%s, meetingId=%s",
                    id, slot.getMeeting().getId()));
        }

        slotRepository.deleteById(id);
    }
}
