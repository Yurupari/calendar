package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.repository.SlotRepository;
import com.yurupari.calendar.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class SlotValidator {

    private final SlotRepository slotRepository;

    public void validateDates(String from, String until) {
        if (from != null && until != null) {
            try {
                var fromInstant = LocalDateTime.parse(from, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                var untilInstant = LocalDateTime.parse(until, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                if (fromInstant.isAfter(untilInstant)) {
                    throw new InvalidFormatException(String.format(
                            "The startDate must be before the endDate: startDate=%s, endDate=%s",
                            from, until));
                }
            } catch (DateTimeParseException e) {
                throw new InvalidFormatException(String.format(
                        "Could not parse dates. Ensure they match the format 'YYYY-MM-DDTHH:mm:ss': startDate=%s, endDate=%s",
                        from, until));
            }
        }
    }

    public void validateExistingTimeFrame(Long calendarId, Instant startTime, Instant endTime) {
        if (slotRepository.existsByCalendarIdAndStartTimeAndEndTime(calendarId, startTime, endTime)) {
            throw new SlotConflictException("A slot exists with the same time frame");
        }
    }
}
