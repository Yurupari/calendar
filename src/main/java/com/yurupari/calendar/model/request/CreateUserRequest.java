package com.yurupari.calendar.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateUserRequest(
        @NotNull(message = "Name cannot be null")
        String name,

        @NotNull(message = "Lastname cannot be null")
        String lastName,

        @NotNull(message = "Email cannot be null")
        String email,

        @NotNull(message = "Timezone cannot be null")
        String timezone
) {
}
