package com.yurupari.calendar.controller.v1;

import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.model.request.CreateMeetingRequest;
import com.yurupari.calendar.model.response.MeetingResponse;
import com.yurupari.calendar.service.MeetingService;
import com.yurupari.calendar.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingControllerV1Test {

    @InjectMocks
    private MeetingControllerV1 meetingController;

    @Mock
    private MeetingService meetingService;

    @Test
    void createMeeting_Success() {
        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                1L, 10L, "UTC", "Test Meeting", "Description", List.of(2L, 3L));
        var hostDto = TestModelFactory.createTestUserDto(1L, "John", "Doe", "john.doe@example.com");
        var participantDto = TestModelFactory.createTestUserDto(1L, "Marlene", "Wollin", "marlene.wollin@example.com");
        var meetingResponse = MeetingResponse.builder()
                .id(1L)
                .host(hostDto)
                .title("Test Meeting")
                .description("Description")
                .participants(List.of(participantDto))
                .build();

        when(meetingService.createMeeting(any(CreateMeetingRequest.class))).thenReturn(meetingResponse);

        var responseEntity = meetingController.createMeeting(createMeetingRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(meetingResponse, responseEntity.getBody());

        verify(meetingService, times(1)).createMeeting(createMeetingRequest);
    }

    @Test
    void getMeetingById_Success() {
        var hostDto = TestModelFactory.createTestUserDto(1L, "John", "Doe", "john.doe@example.com");
        var participantDto = TestModelFactory.createTestUserDto(1L, "Marlene", "Wollin", "marlene.wollin@example.com");
        var meetingResponse = MeetingResponse.builder()
                .id(1L)
                .title("Test Meeting")
                .description("Description")
                .host(hostDto)
                .participants(List.of(participantDto))
                .build();

        when(meetingService.getMeetingById(anyLong())).thenReturn(meetingResponse);

        var responseEntity = meetingController.getMeetingById(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(meetingResponse, responseEntity.getBody());

        verify(meetingService, times(1)).getMeetingById(1L);
    }

    @Test
    void getMeetingById_NotFound_ThrowsException() {
        when(meetingService.getMeetingById(anyLong())).thenThrow(new MeetingNotFoundException(1L));

        assertThrows(MeetingNotFoundException.class, () -> meetingController.getMeetingById(1L));

        verify(meetingService, times(1)).getMeetingById(1L);
    }

    @Test
    void updateMeeting_Success() {
        var request = TestModelFactory.createTestUpdateMeetingRequest(
                "Updated Test Meeting",
                "Updated Description",
                List.of(2L, 3L),
                "UTC");

        doNothing().when(meetingService).updateMeeting(anyLong(), any());

        var responseEntity = meetingController.updateUser(1L, request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Meeting updated successfully", responseEntity.getBody());

        verify(meetingService, times(1)).updateMeeting(1L, request);
    }

    @Test
    void updateMeeting_NotFound_ThrowsException() {
        var request = TestModelFactory.createTestUpdateMeetingRequest(
                "Updated Test Meeting",
                "Updated Description",
                List.of(2L, 3L),
                "UTC");

        doThrow(new MeetingNotFoundException(1L)).when(meetingService).updateMeeting(anyLong(), any());

        assertThrows(MeetingNotFoundException.class, () -> meetingController.updateUser(1L, request));

        verify(meetingService, times(1)).updateMeeting(1L, request);
    }
}
