package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.CalendarNotFoundException;
import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.mapper.CalendarMapper;
import com.yurupari.calendar.repository.CalendarRepository;
import com.yurupari.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;

    private final CalendarMapper calendarMapper;

    @Override
    @Transactional
    public CalendarDto createCalendar(CalendarDto calendarDto) {
        log.info("Creating calendar: calendarDto={}", calendarDto);

        var calendar = calendarMapper.toEntity(calendarDto);
        var savedCalendar = calendarRepository.save(calendar);

        return calendarMapper.toDto(savedCalendar);
    }

    @Override
    public CalendarDto getCalendarById(Long id) {
        log.info("Getting calendar: id={}", id);

        return calendarRepository.findById(id)
                .map(calendarMapper::toDto)
                .orElseThrow(() -> new CalendarNotFoundException(
                        String.format("Calendar not found: id=%s", id)
                ));
    }

    @Override
    public CalendarDto getCalendarByUserId(Long userId) {
        log.info("Getting calendar: userId={}", userId);

        return calendarRepository.findByUserId(userId)
                .map(calendarMapper::toDto)
                .orElseThrow(() -> new CalendarNotFoundException(
                        String.format("Calendar not found: userId=%s", userId)
                ));
    }

    @Override
    @Transactional
    public CalendarDto activateCalendar(Long userId) {
        log.info("Activate calendar: userId={}", userId);

        var calendar = calendarRepository.findByUserId(userId)
                .orElseThrow(() -> new CalendarNotFoundException(
                        String.format("Calendar not found: userId=%s", userId)
                ));

        calendar.setStatus(Status.ACTIVE);
        var savedCalendar = calendarRepository.save(calendar);

        return calendarMapper.toDto(savedCalendar);
    }

    @Override
    @Transactional
    public void updateCalendar(Long userId, String timezone) {
        log.info("Update calendar: userId={}, timezone={}", userId, timezone);

        Optional.ofNullable(timezone)
                .filter(t -> !t.isBlank())
                .ifPresentOrElse(
                        t -> {
                            var calendar = calendarRepository.findByUserId(userId)
                                    .orElseThrow(() -> new CalendarNotFoundException(
                                            String.format("Calendar not found: userId=%s", userId)
                                    ));

                            var calendarDto = CalendarDto.builder()
                                    .timezone(t)
                                    .build();
                            calendarMapper.updateEntityFromDto(calendarDto, calendar);

                            calendarRepository.save(calendar);
                            },
                        () -> { throw new InvalidFormatException("Invalid timezone format"); });
    }

    @Override
    @Transactional
    public void deleteCalendarByUserId(Long userId) {
        log.info("Inactivate calendar: userId={}", userId);

        var calendar = calendarRepository.findByUserId(userId)
                .orElseThrow(() -> new CalendarNotFoundException(
                        String.format("Calendar not found: userId=%s", userId)
                ));

        calendar.setStatus(Status.INACTIVE);
        calendarRepository.save(calendar);
    }
}
