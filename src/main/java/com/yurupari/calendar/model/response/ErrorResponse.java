package com.yurupari.calendar.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        HttpStatus httpStatus,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp,
        String message
) {
}
