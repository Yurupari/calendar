package com.yurupari.calendar.controller.v1;

import com.yurupari.calendar.exception.CalendarNotFoundException;
import com.yurupari.calendar.model.request.UpdateCalendarRequest;
import com.yurupari.calendar.service.CalendarService;
import com.yurupari.calendar.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarControllerV1Test {

    @InjectMocks
    private CalendarControllerV1 calendarController;

    @Mock
    private CalendarService calendarService;

    @Test
    void getCalendarById_Success() {
        var calendarDto = TestModelFactory.createTestCalendarDto(1L, "America/New_York", 10L);

        when(calendarService.getCalendarById(anyLong())).thenReturn(calendarDto);

        var responseEntity = calendarController.getCalendarById(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(calendarDto, responseEntity.getBody());

        verify(calendarService, times(1)).getCalendarById(1L);
    }

    @Test
    void getCalendarById_NotFound_ThrowsException() {
        when(calendarService.getCalendarById(anyLong())).thenThrow(new CalendarNotFoundException("Calendar not found"));

        assertThrows(CalendarNotFoundException.class, () -> calendarController.getCalendarById(1L));

        verify(calendarService, times(1)).getCalendarById(1L);
    }

    @Test
    void updateCalendar_Success() {
        var updateCalendarRequest = new UpdateCalendarRequest("Europe/London");

        doNothing().when(calendarService).updateCalendar(anyLong(), anyString());

        var responseEntity = calendarController.updateCalendar(1L, updateCalendarRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Calendar updated successfully", responseEntity.getBody());

        verify(calendarService, times(1)).updateCalendar(1L, "Europe/London");
    }

    @Test
    void updateCalendar_NotFound_ThrowsException() {
        var updateCalendarRequest = new UpdateCalendarRequest("Europe/London");

        doThrow(new CalendarNotFoundException("Calendar not found")).when(calendarService).updateCalendar(anyLong(), anyString());

        assertThrows(CalendarNotFoundException.class, () -> calendarController.updateCalendar(1L, updateCalendarRequest));

        verify(calendarService, times(1)).updateCalendar(1L, "Europe/London");
    }
}