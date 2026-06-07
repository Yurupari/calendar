package com.yurupari.calendar.error;

import com.yurupari.calendar.model.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        var errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    private ErrorResponse buildErrorResponse(HttpStatus httpStatus, String message) {
        return ErrorResponse.builder()
                .httpStatus(httpStatus)
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
    }
}
