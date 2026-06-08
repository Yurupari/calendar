package com.yurupari.calendar.service;

import com.yurupari.calendar.model.dto.CalendarDto;

public interface CalendarService {
    CalendarDto createCalendar(CalendarDto calendarDto);

    CalendarDto getCalendarById(Long id);

    CalendarDto getCalendarByUserId(Long userId);

    CalendarDto activateCalendar(Long userId);

    void updateCalendar(Long userId, String timezone);

    void deleteCalendarByUserId(Long userId);
}
