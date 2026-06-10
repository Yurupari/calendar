package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.repository.SlotRepository;
import com.yurupari.calendar.util.TimeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotValidatorTest {

    @InjectMocks
    private SlotValidator slotValidator;

    @Mock
    private SlotRepository slotRepository;

    @Test
    void validateDates_Success() {
        var from = "2026-01-01T09:00:00";
        var until = "2026-01-01T10:00:00";

        assertDoesNotThrow(() -> slotValidator.validateDates(from, until));
    }

    @Test
    void validateDates_FromAfterUntil_ThrowsException() {
        var from = "2026-01-01T10:00:00";
        var until = "2026-01-01T09:00:00";

        assertThrows(InvalidFormatException.class, () -> slotValidator.validateDates(from, until));
    }

    @Test
    void validateDates_FromEqualsUntil_Success() {
        var from = "2026-01-01T10:00:00";
        var until = "2026-01-01T10:00:00";

        assertDoesNotThrow(() -> slotValidator.validateDates(from, until));
    }

    @Test
    void validateDates_NullFrom_Success() {
        String from = null;
        var until = "2026-01-01T10:00:00";

        assertDoesNotThrow(() -> slotValidator.validateDates(from, until));
    }

    @Test
    void validateDates_NullUntil_Success() {
        var from = "2026-01-01T09:00:00";
        String until = null;

        assertDoesNotThrow(() -> slotValidator.validateDates(from, until));
    }

    @Test
    void validateDates_BlankFrom_ThrowsInvalidFormatException() {
        String from = "   ";
        var until = "2026-01-01T10:00:00";

        assertThrows(InvalidFormatException.class, () -> slotValidator.validateDates(from, until));
    }

    @Test
    void validateDates_BlankUntil_ThrowsInvalidFormatException() {
        var from = "2026-01-01T09:00:00";
        String until = "   ";

        assertThrows(InvalidFormatException.class, () -> slotValidator.validateDates(from, until));
    }

    @Test
    void validateExistingTimeFrame_Success_NoConflict() {
        Long calendarId = 1L;
        var startTime = Instant.parse("2026-01-01T09:00:00Z");
        var endTime = Instant.parse("2026-01-01T10:00:00Z");

        when(slotRepository.existsByCalendarIdAndStartTimeAndEndTime(calendarId, startTime, endTime))
                .thenReturn(false);

        assertDoesNotThrow(() -> slotValidator.validateExistingTimeFrame(calendarId, startTime, endTime));

        verify(slotRepository, times(1))
                .existsByCalendarIdAndStartTimeAndEndTime(calendarId, startTime, endTime);
    }

    @Test
    void validateExistingTimeFrame_Conflict_ThrowsSlotConflictException() {
        Long calendarId = 1L;
        Instant startTime = Instant.parse("2026-01-01T09:00:00Z");
        Instant endTime = Instant.parse("2026-01-01T10:00:00Z");

        when(slotRepository.existsByCalendarIdAndStartTimeAndEndTime(calendarId, startTime, endTime))
                .thenReturn(true);

        assertThrows(SlotConflictException.class,
                () -> slotValidator.validateExistingTimeFrame(calendarId, startTime, endTime));

        verify(slotRepository, times(1))
                .existsByCalendarIdAndStartTimeAndEndTime(calendarId, startTime, endTime);
    }
}