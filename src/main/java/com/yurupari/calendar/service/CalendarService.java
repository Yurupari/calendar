package com.yurupari.calendar.service;

import com.yurupari.calendar.model.dto.CalendarDto;

import java.util.List;
import java.util.Set;

public interface CalendarService {
    CalendarDto createCalendar(CalendarDto calendarDto);

    CalendarDto getCalendarById(Long id);

    CalendarDto getCalendarByUserId(Long userId);

    List<CalendarDto> getCalendarsByUserIds(Set<Long> userIds);

    CalendarDto activateCalendar(Long userId);

    void updateCalendar(Long userId, String timezone);

    void deleteCalendarByUserId(Long userId);
}
