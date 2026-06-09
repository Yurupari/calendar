package com.yurupari.calendar.util;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class TimeUtil {

    public String parseInstantToIsoString(Instant instant, String timezone) {
        if (instant == null || timezone == null) return null;

        return instant.atZone(ZoneId.of(timezone))
                .toLocalDateTime()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Instant parseIsoStringToInstant(String dateStr, String timezone) {
        if (dateStr == null || dateStr.isBlank() || timezone == null || timezone.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.of(timezone))
                .toInstant();
    }
}
