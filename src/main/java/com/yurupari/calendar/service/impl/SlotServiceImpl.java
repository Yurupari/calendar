package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.SlotAlreadyExistsException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.model.dto.SlotDto;
import com.yurupari.calendar.model.entity.Slot;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        log.info("Saving slot: request={}", createSlotRequest);

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

        return SlotResponse.builder()
                .id(savedSlot.getId())
                .calendarId(calendarDto.id())
                .startTime(timeUtil.parseInstantToIsoString(savedSlot.getStartTime(), calendarDto.timezone()))
                .endTime(timeUtil.parseInstantToIsoString(savedSlot.getEndTime(), calendarDto.timezone()))
                .status(savedSlot.getStatus())
                .role(savedSlot.getRole())
                .build();
    }

    @Override
    public SlotResponse getSlotById(Long id) {
        var slot = slotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException(id));

        return buildSlotResponse(slot);
    }

    @Override
    public List<SlotResponse> getSlots(Long userId, String from, String until) {
        var calendarDto = calendarService.getCalendarByUserId(userId);

        var fromInstant = timeUtil.parseIsoStringToInstant(from, calendarDto.timezone());
        var untilInstant = timeUtil.parseIsoStringToInstant(until, calendarDto.timezone());

        return slotRepository.findByCalendarIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(calendarDto.id(), fromInstant, untilInstant).stream()
                .map(this::buildSlotResponse)
                .toList();
    }

    @Override
    public void updateSlot(Long id, UpdateSlotRequest updateSlotRequest) {
        slotValidator.validateDates(updateSlotRequest.startTime(), updateSlotRequest.endTime());

        var slot = slotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException(id));

        var timezone = slot.getCalendar().getTimezone();

        var startTime = timeUtil.parseIsoStringToInstant(updateSlotRequest.startTime(), timezone);
        var endTime = timeUtil.parseIsoStringToInstant(updateSlotRequest.endTime(), timezone);

        var slotDto = SlotDto.builder()
                .meetingId(updateSlotRequest.meetingId())
                .startTime(startTime)
                .endTime(endTime)
                .status(updateSlotRequest.status())
                .role(updateSlotRequest.role())
                .build();
        slotMapper.updateEntityFromDto(slotDto, slot);

        slotRepository.save(slot);
    }

    @Override
    public void deleteSlot(Long id) {
        slotRepository.deleteById(id);
    }

    private SlotResponse buildSlotResponse(Slot slot) {
        var timezone = slot.getCalendar().getTimezone();

        return SlotResponse.builder()
                .id(slot.getId())
                .calendarId(slot.getCalendar().getId())
                .meetingId(slot.getMeeting().getId())
                .startTime(timeUtil.parseInstantToIsoString(slot.getStartTime(), timezone))
                .endTime(timeUtil.parseInstantToIsoString(slot.getEndTime(), timezone))
                .status(slot.getStatus())
                .role(slot.getRole())
                .build();
    }
}
