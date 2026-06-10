package com.yurupari.calendar.error;

import com.yurupari.calendar.exception.CalendarNotFoundException;
import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.exception.SlotAlreadyExistsException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.exception.UserAlreadyExistsException;
import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        var errorResponse = buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleSlotNotFoundException(SlotNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleSlotAlreadyExistsException(SlotAlreadyExistsException e) {
        var errorResponse = buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleParticipantSlotConflictException(SlotConflictException e) {
        var errorResponse = buildErrorResponse(HttpStatus.CONFLICT, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleCalendarNotFoundException(CalendarNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMeetingNotFoundException(MeetingNotFoundException e) {
        var errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException e) {
        var errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        var errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException e) {
        var errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        var errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        return new ResponseEntity<>(errorResponse, errorResponse.httpStatus());
    }

    private ErrorResponse buildErrorResponse(HttpStatus httpStatus, String message) {
        log.error(message);

        return ErrorResponse.builder()
                .httpStatus(httpStatus)
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
    }
}
