package com.yurupari.calendar.error;

import com.yurupari.calendar.exception.CalendarNotFoundException;
import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.exception.SlotAlreadyExistsException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.exception.UserAlreadyExistsException;
import com.yurupari.calendar.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    private static final String TEST_MESSAGE = "Test message";
    private static final Long TEST_ID = 1L;

    @Test
    void handleUserNotFoundException_ReturnsNotFound() {
        var exception = new UserNotFoundException(TEST_ID);
        var responseEntity = errorHandler.handleUserNotFoundException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains(String.valueOf(TEST_ID)));
    }

    @Test
    void handleUserAlreadyExistsException_ReturnsConflict() {
        var exception = new UserAlreadyExistsException(TEST_MESSAGE);
        var responseEntity = errorHandler.handleUserAlreadyExistsException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.CONFLICT, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains(TEST_MESSAGE));
    }

    @Test
    void handleSlotNotFoundException_ReturnsNotFound() {
        var exception = new SlotNotFoundException(TEST_ID);
        var responseEntity = errorHandler.handleSlotNotFoundException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains(String.valueOf(TEST_ID)));
    }

    @Test
    void handleSlotAlreadyExistsException_ReturnsConflict() {
        var exception = new SlotAlreadyExistsException(TEST_ID);
        var responseEntity = errorHandler.handleSlotAlreadyExistsException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.CONFLICT, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains(String.valueOf(TEST_ID)));
    }

    @Test
    void handleParticipantSlotConflictException_ReturnsConflict() {
        var exception = new SlotConflictException(TEST_ID);
        var responseEntity = errorHandler.handleParticipantSlotConflictException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.CONFLICT, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains(String.valueOf(TEST_ID)));
    }

    @Test
    void handleCalendarNotFoundException_ReturnsNotFound() {
        var exception = new CalendarNotFoundException(TEST_MESSAGE);
        var responseEntity = errorHandler.handleCalendarNotFoundException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getBody().httpStatus());
        assertEquals(TEST_MESSAGE, responseEntity.getBody().message());
    }

    @Test
    void handleMeetingNotFoundException_ReturnsNotFound() {
        var exception = new MeetingNotFoundException(TEST_ID);
        var responseEntity = errorHandler.handleMeetingNotFoundException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains(String.valueOf(TEST_ID)));
    }

    @Test
    void handleInvalidFormatException_ReturnsBadRequest() {
        var exception = new InvalidFormatException(TEST_MESSAGE);
        var responseEntity = errorHandler.handleInvalidFormatException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getBody().httpStatus());
        assertEquals(TEST_MESSAGE, responseEntity.getBody().message());
    }

    @Test
    void handleValidationException_ReturnsBadRequest() throws NoSuchMethodException {
        var bindingResult = mock(BindingResult.class);
        var fieldError = new FieldError("objectName", "fieldName", "Validation failed");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        var mockMethod = this.getClass().getDeclaredMethod("handleValidationException_ReturnsBadRequest");
        var methodParameter = new MethodParameter(mockMethod, -1);

        var exception = new MethodArgumentNotValidException(methodParameter, bindingResult);
        var responseEntity = errorHandler.handleValidationException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains("Validation failed"));
    }

    @Test
    void handleMalformedJson_ReturnsBadRequest() {
        var httpInputMessage = mock(HttpInputMessage.class);
        var exception = new HttpMessageNotReadableException(TEST_MESSAGE, httpInputMessage);
        var responseEntity = errorHandler.handleMalformedJson(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getBody().httpStatus());
        assertTrue(responseEntity.getBody().message().contains(TEST_MESSAGE));
    }

    @Test
    void handleException_ReturnsInternalServerError() {
        var exception = new RuntimeException(TEST_MESSAGE);
        var responseEntity = errorHandler.handleException(exception);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getBody().httpStatus());
        assertEquals(TEST_MESSAGE, responseEntity.getBody().message());
    }
}