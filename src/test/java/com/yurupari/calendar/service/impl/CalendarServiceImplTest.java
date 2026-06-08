package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.CalendarNotFoundException;
import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.entity.Calendar;
import com.yurupari.calendar.model.entity.User;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.mapper.CalendarMapperImpl;
import com.yurupari.calendar.repository.CalendarRepository;
import com.yurupari.calendar.repository.UserRepository;
import com.yurupari.calendar.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceImplTest {

    @InjectMocks
    private CalendarServiceImpl calendarService;

    @Mock
    private CalendarRepository calendarRepository;

    @Spy
    private CalendarMapperImpl calendarMapper = new CalendarMapperImpl();

    @Test
    void createCalendar_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        var calendarDto = TestModelFactory.createTestCalendarDto(null, "America/New_York", user.getId());
        var calendarEntity = TestModelFactory.createTestCalendar(null, "America/New_York", user, Status.ACTIVE);
        var savedCalendarEntity = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.ACTIVE);

        when(calendarMapper.toEntity(any(CalendarDto.class))).thenReturn(calendarEntity);
        when(calendarRepository.save(any(Calendar.class))).thenReturn(savedCalendarEntity);
        when(calendarMapper.toDto(any(Calendar.class))).thenReturn(TestModelFactory.createTestCalendarDto(1L, "America/New_York", user.getId()));

        var result = calendarService.createCalendar(calendarDto);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("America/New_York", result.timezone());
        assertEquals(user.getId(), result.userId());

        verify(calendarMapper, times(1)).toEntity(any(CalendarDto.class));
        verify(calendarRepository, times(1)).save(any(Calendar.class));
        verify(calendarMapper, times(1)).toDto(any(Calendar.class));
    }

    @Test
    void getCalendarById_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        var calendarEntity = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.ACTIVE);

        when(calendarRepository.findById(anyLong())).thenReturn(Optional.of(calendarEntity));
        when(calendarMapper.toDto(any(Calendar.class))).thenReturn(TestModelFactory.createTestCalendarDto(1L, "America/New_York", user.getId()));

        var result = calendarService.getCalendarById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("America/New_York", result.timezone());
        assertEquals(user.getId(), result.userId());

        verify(calendarRepository, times(1)).findById(anyLong());
        verify(calendarMapper, times(1)).toDto(any(Calendar.class));
    }

    @Test
    void getCalendarById_NotFound_ThrowsException() {
        when(calendarRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(CalendarNotFoundException.class, () -> calendarService.getCalendarById(1L));

        verify(calendarRepository, times(1)).findById(anyLong());
        verify(calendarMapper, never()).toDto(any(Calendar.class));
    }

    @Test
    void getCalendarByUserId_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        var calendarEntity = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.ACTIVE);

        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.of(calendarEntity));
        when(calendarMapper.toDto(any(Calendar.class))).thenReturn(TestModelFactory.createTestCalendarDto(1L, "America/New_York", user.getId()));

        var result = calendarService.getCalendarByUserId(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("America/New_York", result.timezone());
        assertEquals(user.getId(), result.userId());

        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarMapper, times(1)).toDto(any(Calendar.class));
    }

    @Test
    void getCalendarByUserId_NotFound_ThrowsException() {
        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(CalendarNotFoundException.class, () -> calendarService.getCalendarByUserId(1L));

        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarMapper, never()).toDto(any(Calendar.class));
    }

    @Test
    void activateCalendar_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.INACTIVE);
        var inactiveCalendar = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.INACTIVE);
        var activeCalendar = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.ACTIVE);

        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.of(inactiveCalendar));
        when(calendarRepository.save(any(Calendar.class))).thenReturn(activeCalendar);
        when(calendarMapper.toDto(any(Calendar.class))).thenReturn(TestModelFactory.createTestCalendarDto(1L, "America/New_York", user.getId()));

        var result = calendarService.activateCalendar(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("America/New_York", result.timezone());
        assertEquals(user.getId(), result.userId());
        assertEquals(Status.ACTIVE, inactiveCalendar.getStatus());

        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarRepository, times(1)).save(any(Calendar.class));
        verify(calendarMapper, times(1)).toDto(any(Calendar.class));
    }

    @Test
    void activateCalendar_NotFound_ThrowsException() {
        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(CalendarNotFoundException.class, () -> calendarService.activateCalendar(1L));

        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarRepository, never()).save(any(Calendar.class));
        verify(calendarMapper, never()).toDto(any(Calendar.class));
    }

    @Test
    void updateCalendar_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        var existingCalendar = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.ACTIVE);

        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.of(existingCalendar));
        when(calendarRepository.save(any(Calendar.class))).thenReturn(existingCalendar);

        calendarService.updateCalendar(1L, "Europe/London");

        assertEquals("Europe/London", existingCalendar.getTimezone());
        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarMapper, times(1)).updateEntityFromDto(any(CalendarDto.class), any(Calendar.class));
        verify(calendarRepository, times(1)).save(any(Calendar.class));
    }

    @ParameterizedTest
    @CsvSource({
            "null",
            "''",
            "'   '"
    })
    void updateCalendar_InvalidTimezone_NoUpdate(String timezone) {
        timezone = timezone.equals("null") ? null : timezone;
        calendarService.updateCalendar(1L, timezone);

        verify(calendarRepository, never()).findByUserId(anyLong());
        verify(calendarMapper, never()).updateEntityFromDto(any(CalendarDto.class), any(Calendar.class));
        verify(calendarRepository, never()).save(any(Calendar.class));
    }

    @Test
    void updateCalendar_NotFound_ThrowsException() {
        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(CalendarNotFoundException.class, () -> calendarService.updateCalendar(1L, "Europe/London"));

        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarMapper, never()).updateEntityFromDto(any(CalendarDto.class), any(Calendar.class));
        verify(calendarRepository, never()).save(any(Calendar.class));
    }

    @Test
    void deleteCalendarByUserId_Success() {
        var user = TestModelFactory.createTestUser(1L, "john.doe@example.com", Status.ACTIVE);
        var activeCalendar = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.ACTIVE);
        var inactiveCalendar = TestModelFactory.createTestCalendar(1L, "America/New_York", user, Status.INACTIVE);

        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.of(activeCalendar));
        when(calendarRepository.save(any(Calendar.class))).thenReturn(inactiveCalendar);

        calendarService.deleteCalendarByUserId(1L);

        assertEquals(Status.INACTIVE, activeCalendar.getStatus());
        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarRepository, times(1)).save(any(Calendar.class));

    }

    @Test
    void deleteCalendarByUserId_NotFound_ThrowsException() {
        when(calendarRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThrows(CalendarNotFoundException.class, () -> calendarService.deleteCalendarByUserId(1L));

        verify(calendarRepository, times(1)).findByUserId(anyLong());
        verify(calendarRepository, never()).save(any(Calendar.class));
    }
}
