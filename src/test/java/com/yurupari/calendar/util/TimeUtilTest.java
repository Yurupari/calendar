package com.yurupari.calendar.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TimeUtilTest {

    @InjectMocks
    private TimeUtil timeUtil;

    private final String DEFAULT_TIMEZONE = "UTC";

    @ParameterizedTest
    @CsvSource({
            "2026-01-01T10:00:00Z, America/New_York, 2026-01-01T05:00:00",
            "2026-01-01T10:00:00Z, Europe/London, 2026-01-01T10:00:00",
            "2026-01-01T10:00:00Z, Asia/Tokyo, 2026-01-01T19:00:00"
    })
    void parseInstantToIsoString_Success(String instantStr, String timezone, String expectedLocalDateTimeStr) {
        var instant = Instant.parse(instantStr);

        var result = timeUtil.parseInstantToIsoString(instant, timezone);

        assertEquals(expectedLocalDateTimeStr, result);
    }

    @Test
    void parseInstantToIsoString_NullInstant_ReturnsNull() {
        var result = timeUtil.parseInstantToIsoString(null, DEFAULT_TIMEZONE);
        assertNull(result);
    }

    @Test
    void parseInstantToIsoString_NullTimezone_ReturnsNull() {
        var instant = Instant.parse("2026-01-01T10:00:00Z");
        var result = timeUtil.parseInstantToIsoString(instant, null);
        assertNull(result);
    }

    @Test
    void parseInstantToIsoString_InvalidTimezone_ThrowsException() {
        var instant = Instant.parse("2026-01-01T10:00:00Z");
        assertThrows(ZoneRulesException.class, () -> timeUtil.parseInstantToIsoString(instant, "Invalid/Timezone"));
    }

    @ParameterizedTest
    @CsvSource({
            "2026-01-01T10:00:00, UTC, 2026-01-01T10:00:00Z",
            "2026-01-01T10:00:00, America/New_York, 2026-01-01T15:00:00Z",
            "2026-01-01T10:00:00, Asia/Tokyo, 2026-01-01T01:00:00Z"
    })
    void parseIsoStringToInstant_Success(String dateStr, String timezone, String expectedInstantStr) {
        var expectedInstant = Instant.parse(expectedInstantStr);

        var result = timeUtil.parseIsoStringToInstant(dateStr, timezone);

        assertEquals(expectedInstant, result);
    }

    @Test
    void parseIsoStringToInstant_NullString_ReturnsNull() {
        var result = timeUtil.parseIsoStringToInstant(null, DEFAULT_TIMEZONE);
        assertNull(result);
    }

    @Test
    void parseIsoStringToInstant_BlankString_ReturnsNull() {
        var result = timeUtil.parseIsoStringToInstant("   ", DEFAULT_TIMEZONE);
        assertNull(result);
    }

    @Test
    void parseIsoStringToInstant_NullTimezone_ReturnsNull() {
        var result = timeUtil.parseIsoStringToInstant("2026-01-01T10:00:00", null);
        assertNull(result);
    }

    @Test
    void parseIsoStringToInstant_BlankTimezone_ReturnsNull() {
        var result = timeUtil.parseIsoStringToInstant("2026-01-01T10:00:00", "   ");
        assertNull(result);
    }

    @Test
    void parseIsoStringToInstant_InvalidFormat_ThrowsException() {
        assertThrows(DateTimeParseException.class, () -> timeUtil.parseIsoStringToInstant("invalid-date-string", DEFAULT_TIMEZONE));
    }

    @Test
    void parseIsoStringToInstant_InvalidTimezone_ThrowsException() {
        assertThrows(ZoneRulesException.class, () -> timeUtil.parseIsoStringToInstant("2026-01-01T10:00:00", "Invalid/Timezone"));
    }
}