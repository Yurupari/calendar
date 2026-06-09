package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.enums.MeetingStatus;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.mapper.MeetingMapperImpl;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.repository.MeetingRepository;
import com.yurupari.calendar.service.SlotService;
import com.yurupari.calendar.service.UserService;
import com.yurupari.calendar.utils.TestModelFactory;
import com.yurupari.calendar.validator.MeetingValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceImplTest {

    @InjectMocks
    private MeetingServiceImpl meetingService;

    @Mock
    private SlotService slotService;

    @Mock
    private UserService userService;

    @Mock
    private MeetingValidator meetingValidator;

    @Mock
    private MeetingRepository meetingRepository;

    @Spy
    private MeetingMapperImpl meetingMapper = new MeetingMapperImpl();

    @Test
    void createMeeting_Success_NoParticipants() {
        Long hostId = 1L;
        Long slotId = 10L;
        Long meetingId = 100L;
        var title = "Project Sync";
        var description = "Weekly sync";
        var startTime = "2024-01-01T09:00:00Z";
        var endTime = "2024-01-01T10:00:00Z";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, title, description, List.of()
        );

        var slotResponse = TestModelFactory.createTestSlotResponse(
                slotId, 1L, null, startTime, endTime, SlotStatus.FREE, null
        );
        when(slotService.getSlotById(slotId)).thenReturn(slotResponse);

        doNothing().when(meetingValidator).validateParticipantsAvailability(any());

        var savedMeetingEntity = TestModelFactory.createTestMeeting(
                meetingId, title, description, TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE), MeetingStatus.SCHEDULED
        );
        when(meetingRepository.save(any())).thenReturn(savedMeetingEntity);

        var hostUserDto = TestModelFactory.createTestUserDto(hostId, "Host", "User", "host@example.com");
        when(userService.getUsers(anyList())).thenReturn(List.of(hostUserDto));

        var result = meetingService.createMeeting(createMeetingRequest);

        assertNotNull(result);
        assertEquals(meetingId, result.id());
        assertEquals(title, result.title());
        assertEquals(hostId, result.host().id());
        assertNotNull(result.participants());
        assertEquals(0, result.participants().size());

        verify(slotService, times(1)).getSlotById(slotId);
        verify(meetingValidator, times(1)).validateParticipantsAvailability(any());
        verify(meetingRepository, times(1)).save(any());
        verify(slotService, times(1)).updateSlot(anyLong(), any());
        verify(slotService, times(1)).updateSlots(anyList(), any());
        verify(userService, times(1)).getUsers(anyList());
    }

    @Test
    void createMeeting_Success_WithParticipants() {
        Long hostId = 1L;
        Long participant1Id = 2L;
        Long participant2Id = 3L;
        Long slotId = 10L;
        Long participant1SlotId = 11L;
        Long participant2SlotId = 12L;
        Long meetingId = 100L;
        var title = "Project Sync";
        var description = "Weekly sync";
        var startTime = "2024-01-01T09:00:00Z";
        var endTime = "2024-01-01T10:00:00Z";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, title, description, List.of(participant1Id, participant2Id)
        );

        var hostSlotResponse = TestModelFactory.createTestSlotResponse(
                slotId, 1L, null, startTime, endTime, SlotStatus.FREE, null
        );
        when(slotService.getSlotById(slotId)).thenReturn(hostSlotResponse);

        var participant1SlotResponse = TestModelFactory.createTestSlotResponse(
                participant1SlotId, 2L, null, startTime, endTime, SlotStatus.FREE, null
        );
        var participant2SlotResponse = TestModelFactory.createTestSlotResponse(
                participant2SlotId, 3L, null, startTime, endTime, SlotStatus.FREE, null
        );
        when(slotService.getSlots(eq(participant1Id), anyString(), anyString()))
                .thenReturn(List.of(participant1SlotResponse));
        when(slotService.getSlots(eq(participant2Id), anyString(), anyString()))
                .thenReturn(List.of(participant2SlotResponse));

        doNothing().when(meetingValidator).validateParticipantsAvailability(any(Map.class));

        var savedMeetingEntity = TestModelFactory.createTestMeeting(
                meetingId, title, description, TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE), MeetingStatus.SCHEDULED
        );
        when(meetingRepository.save(any(Meeting.class))).thenReturn(savedMeetingEntity);

        var hostUserDto = TestModelFactory.createTestUserDto(hostId, "Host", "User", "host@example.com");
        var participant1UserDto = TestModelFactory.createTestUserDto(participant1Id, "P1", "User", "p1@example.com");
        var participant2UserDto = TestModelFactory.createTestUserDto(participant2Id, "P2", "User", "p2@example.com");
        when(userService.getUsers(anyList())).thenReturn(List.of(hostUserDto, participant1UserDto, participant2UserDto));

        var result = meetingService.createMeeting(createMeetingRequest);

        assertNotNull(result);
        assertEquals(meetingId, result.id());
        assertEquals(title, result.title());
        assertEquals(hostId, result.host().id());
        assertNotNull(result.participants());
        assertEquals(2, result.participants().size());
        assertEquals(participant1Id, result.participants().get(0).id());
        assertEquals(participant2Id, result.participants().get(1).id());

        verify(slotService, times(1)).getSlotById(slotId);
        verify(slotService, times(1)).getSlots(eq(participant1Id), anyString(), anyString());
        verify(slotService, times(1)).getSlots(eq(participant2Id), anyString(), anyString());
        verify(meetingValidator, times(1)).validateParticipantsAvailability(any());
        verify(meetingRepository, times(1)).save(any());
        verify(slotService, times(1)).updateSlot(eq(slotId), any());
        verify(slotService, times(1)).updateSlots(
                anyList(), any()
        );
        verify(userService, times(1)).getUsers(anyList());
    }

    @Test
    void createMeeting_SlotNotFound_ThrowsException() {
        Long hostId = 1L;
        Long slotId = 10L;
        var title = "Project Sync";
        var description = "Weekly sync";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, title, description, List.of()
        );

        when(slotService.getSlotById(slotId)).thenThrow(new SlotNotFoundException(slotId));

        assertThrows(SlotNotFoundException.class, () -> meetingService.createMeeting(createMeetingRequest));

        verify(slotService, times(1)).getSlotById(slotId);
        verify(meetingValidator, never()).validateParticipantsAvailability(any(Map.class));
        verify(meetingRepository, never()).save(any(Meeting.class));
        verify(slotService, never()).updateSlot(anyLong(), any(UpdateSlotRequest.class));
        verify(slotService, never()).updateSlots(anyList(), any(UpdateSlotRequest.class));
        verify(userService, never()).getUsers(anyList());
    }

    @Test
    void createMeeting_ParticipantsNotAvailable_ThrowsException() {
        Long hostId = 1L;
        Long participant1Id = 2L;
        Long slotId = 10L;
        var title = "Project Sync";
        var description = "Weekly sync";
        var startTime = "2024-01-01T09:00:00Z";
        var endTime = "2024-01-01T10:00:00Z";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, title, description, List.of(participant1Id)
        );

        var hostSlotResponse = TestModelFactory.createTestSlotResponse(
                slotId, 1L, null, startTime, endTime, SlotStatus.FREE, null
        );
        when(slotService.getSlotById(slotId)).thenReturn(hostSlotResponse);

        when(slotService.getSlots(eq(participant1Id), anyString(), anyString()))
                .thenReturn(List.of()); // Participant not available

        doThrow(new IllegalArgumentException("Participants not available")).when(meetingValidator).validateParticipantsAvailability(any(Map.class));

        assertThrows(IllegalArgumentException.class, () -> meetingService.createMeeting(createMeetingRequest));

        verify(slotService, times(1)).getSlotById(slotId);
        verify(slotService, times(1)).getSlots(eq(participant1Id), anyString(), anyString());
        verify(meetingValidator, times(1)).validateParticipantsAvailability(any(Map.class));
        verify(meetingRepository, never()).save(any(Meeting.class));
        verify(slotService, never()).updateSlot(anyLong(), any(UpdateSlotRequest.class));
        verify(slotService, never()).updateSlots(anyList(), any(UpdateSlotRequest.class));
        verify(userService, never()).getUsers(anyList());
    }

    @Test
    void getMeetingById_Success() {
        Long meetingId = 1L;
        Long hostId = 10L;
        var title = "Test Meeting";
        var description = "Description";

        var meetingEntity = TestModelFactory.createTestMeeting(
                meetingId, title, description, TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE), MeetingStatus.SCHEDULED
        );
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meetingEntity));

        var hostUserDto = TestModelFactory.createTestUserDto(hostId, "Host", "User", "host@example.com");
        when(userService.getUsersByMeetingId(meetingId)).thenReturn(List.of(hostUserDto));

        var result = meetingService.getMeetingById(meetingId);

        assertNotNull(result);
        assertEquals(meetingId, result.id());
        assertEquals(title, result.title());
        assertEquals(description, result.description());
        assertEquals(hostId, result.host().id());
        assertNotNull(result.participants());
        assertEquals(0, result.participants().size());

        verify(meetingRepository, times(1)).findById(meetingId);
        verify(userService, times(1)).getUsersByMeetingId(meetingId);
    }

    @Test
    void getMeetingById_NotFound_ThrowsException() {
        Long meetingId = 1L;

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        assertThrows(MeetingNotFoundException.class, () -> meetingService.getMeetingById(meetingId));

        verify(meetingRepository, times(1)).findById(meetingId);
        verify(userService, never()).getUsersByMeetingId(anyLong());
    }

    @Test
    void getMeetingById_HostNotFound_ThrowsException() {
        Long meetingId = 1L;
        Long hostId = 10L;
        var title = "Test Meeting";
        var description = "Description";

        var meetingEntity = TestModelFactory.createTestMeeting(
                meetingId, title, description, TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE), MeetingStatus.SCHEDULED
        );
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meetingEntity));

        when(userService.getUsersByMeetingId(meetingId)).thenReturn(List.of()); // Host not found in the list

        assertThrows(UserNotFoundException.class, () -> meetingService.getMeetingById(meetingId));

        verify(meetingRepository, times(1)).findById(meetingId);
        verify(userService, times(1)).getUsersByMeetingId(meetingId);
    }
}