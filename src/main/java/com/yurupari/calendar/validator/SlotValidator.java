package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class SlotValidator {

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
}
