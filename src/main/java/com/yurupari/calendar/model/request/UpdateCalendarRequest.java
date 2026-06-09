package com.yurupari.calendar.model.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCalendarRequest(
        @NotNull(message = "Timezone is required")
        String timezone
) {
}
