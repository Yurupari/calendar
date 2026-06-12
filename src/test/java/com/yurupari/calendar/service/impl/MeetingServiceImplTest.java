package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.model.dto.SlotInformationDto;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.enums.MeetingStatus;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.mapper.MeetingMapperImpl;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.repository.MeetingRepository;
import com.yurupari.calendar.service.CalendarService;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
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
    private CalendarService calendarService;

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
        var startTime = "2026-01-01T09:00:00Z";
        var endTime = "2026-01-01T10:00:00Z";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, "UTC", title, description, List.of());

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
        verify(slotService, times(1)).updateSlots(anySet(), any());
        verify(userService, times(1)).getUsers(anyList());
    }

    @Test
    void createMeeting_Success_WithParticipants() {
        Long hostId = 1L;
        Long participant1Id = 2L;
        Long participant2Id = 3L;
        var participantsIds = List.of(participant1Id, participant2Id);
        Long slotId = 10L;
        Long participant1SlotId = 11L;
        Long participant2SlotId = 12L;
        Long meetingId = 100L;
        var title = "Project Sync";
        var description = "Weekly sync";
        var startTime = "2026-01-01T09:00:00Z";
        var endTime = "2026-01-01T10:00:00Z";
        var timezone = "UTC";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, timezone, title, description, participantsIds);

        var hostSlotResponse = TestModelFactory.createTestSlotResponse(
                slotId, 1L, null, startTime, endTime, SlotStatus.FREE, null);
        when(slotService.getSlotById(slotId)).thenReturn(hostSlotResponse);

        var participant1SlotResponse = TestModelFactory.createTestSlotResponse(
                participant1SlotId, 2L, null, startTime, endTime, SlotStatus.FREE, null
        );
        var participant2SlotResponse = TestModelFactory.createTestSlotResponse(
                participant2SlotId, 3L, null, startTime, endTime, SlotStatus.FREE, null
        );
        var participantsSlots = Map.of(
                participant1Id, List.of(participant1SlotResponse),
                participant2Id, List.of(participant2SlotResponse));
        when(slotService.getSlots(eq(new HashSet<>(participantsIds)), eq(startTime), eq(endTime), eq(timezone), eq(SlotStatus.FREE)))
                .thenReturn(participantsSlots);

        doNothing().when(meetingValidator).validateParticipantsAvailability(any());

        var savedMeetingEntity = TestModelFactory.createTestMeeting(
                meetingId,
                title,
                description,
                TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE),
                MeetingStatus.SCHEDULED);
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
        assertEquals(participant1Id, result.participants().getFirst().id());
        assertEquals(participant2Id, result.participants().get(1).id());

        verify(slotService, times(1)).getSlotById(slotId);
        verify(slotService, times(1))
                .getSlots(eq(new HashSet<>(participantsIds)), eq(startTime), eq(endTime), eq(timezone), eq(SlotStatus.FREE));
        verify(meetingValidator, times(1)).validateParticipantsAvailability(any());
        verify(meetingRepository, times(1)).save(any());
        verify(slotService, times(1)).updateSlot(eq(slotId), any());
        verify(slotService, times(1)).updateSlots(anySet(), any());
        verify(userService, times(1)).getUsers(anyList());
    }

    @Test
    void createMeeting_SlotNotFound_ThrowsException() {
        Long hostId = 1L;
        Long slotId = 10L;
        var title = "Project Sync";
        var description = "Weekly sync";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, "UTC", title, description, List.of());

        when(slotService.getSlotById(slotId)).thenThrow(new SlotNotFoundException(slotId));

        assertThrows(SlotNotFoundException.class, () -> meetingService.createMeeting(createMeetingRequest));

        verify(slotService, times(1)).getSlotById(slotId);
        verify(meetingValidator, never()).validateParticipantsAvailability(any());
        verify(meetingRepository, never()).save(any(Meeting.class));
        verify(slotService, never()).updateSlot(anyLong(), any(UpdateSlotRequest.class));
        verify(slotService, never()).updateSlots(anySet(), any(UpdateSlotRequest.class));
        verify(userService, never()).getUsers(anyList());
    }

    @Test
    void createMeeting_ParticipantsNotAvailable_ThrowsException() {
        Long hostId = 1L;
        Long participantId = 2L;
        var participantsIds = List.of(participantId);
        Long slotId = 10L;
        var title = "Project Sync";
        var description = "Weekly sync";
        var startTime = "2026-01-01T09:00:00Z";
        var endTime = "2026-01-01T10:00:00Z";
        var timezone = "UTC";

        var createMeetingRequest = TestModelFactory.createTestCreateMeetingRequest(
                hostId, slotId, timezone, title, description, participantsIds
        );

        var hostSlotResponse = TestModelFactory.createTestSlotResponse(
                slotId, 1L, null, startTime, endTime, SlotStatus.FREE, null
        );
        when(slotService.getSlotById(slotId)).thenReturn(hostSlotResponse);

        when(slotService.getSlots(eq(new HashSet<>(participantsIds)), eq(startTime), eq(endTime), eq(timezone), eq(SlotStatus.FREE)))
                .thenReturn(Map.of(participantId, List.of()));

        doThrow(new SlotConflictException("Some users have busy slots: users=[2]")).when(meetingValidator).validateParticipantsAvailability(any());

        assertThrows(SlotConflictException.class, () -> meetingService.createMeeting(createMeetingRequest));

        verify(slotService, times(1)).getSlotById(slotId);
        verify(slotService, times(1))
                .getSlots(eq(new HashSet<>(participantsIds)), eq(startTime), eq(endTime), eq(timezone), eq(SlotStatus.FREE));
        verify(meetingValidator, times(1)).validateParticipantsAvailability(any());
        verify(meetingRepository, never()).save(any(Meeting.class));
        verify(slotService, never()).updateSlot(anyLong(), any(UpdateSlotRequest.class));
        verify(slotService, never()).updateSlots(anySet(), any(UpdateSlotRequest.class));
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

        when(calendarService.getCalendarByUserId(anyLong())).thenReturn(TestModelFactory.createTestCalendarDto(1L, "UTC", hostId));

        var slotInformation = TestModelFactory.createTestSlotInformation(
                1L, 1L, ParticipantRole.HOST, "2026-06-10T10:00:00", "2026-06-10T11:00:00");
        when(slotService.getSlotInformation(anyLong(), anyString())).thenReturn(List.of(slotInformation));

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
    void getMeetingById_Success_WithFullWorkflow() {
        Long meetingId = 1L;
        Long hostId = 10L;
        Long participantId = 11L;
        var timezone = "Europe/Berlin";
        var title = "Test Meeting";
        var description = "Description";

        var hostUser = TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE);
        var meetingEntity = TestModelFactory.createTestMeeting(meetingId, title, description, hostUser, MeetingStatus.SCHEDULED);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meetingEntity));

        var hostUserDto = TestModelFactory.createTestUserDto(hostId, "Host", "User", "host@example.com");
        var participantUserDto = TestModelFactory.createTestUserDto(participantId, "Participant", "User", "part@example.com");
        when(userService.getUsersByMeetingId(meetingId)).thenReturn(List.of(hostUserDto, participantUserDto));

        when(calendarService.getCalendarByUserId(anyLong())).thenReturn(TestModelFactory.createTestCalendarDto(1L, timezone, hostId));

        var hostSlotInfo = SlotInformationDto.builder()
                .id(100L)
                .calendarId(10L)
                .role(ParticipantRole.HOST)
                .startTime("2026-06-10T10:00:00")
                .endTime("2026-06-10T11:00:00")
                .build();
        when(slotService.getSlotInformation(meetingId, timezone)).thenReturn(List.of(hostSlotInfo));

        var result = meetingService.getMeetingById(meetingId);

        assertNotNull(result);
        assertEquals(meetingId, result.id());
        assertEquals(title, result.title());
        assertEquals(hostId, result.host().id());
        assertEquals(1, result.participants().size());
        assertEquals(participantId, result.participants().getFirst().id());
        assertNotNull(result.slot());
        assertEquals(100L, result.slot().id());

        verify(meetingRepository, times(1)).findById(meetingId);
        verify(userService, times(1)).getUsersByMeetingId(meetingId);
        verify(calendarService, times(1)).getCalendarByUserId(hostId);
        verify(slotService, times(1)).getSlotInformation(meetingId, timezone);
    }

    @Test
    void getMeetingById_SlotNotFound_ThrowsException() {
        Long meetingId = 1L;
        Long hostId = 10L;
        String timezone = "Europe/Berlin";

        var hostUser = TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE);
        var meetingEntity = TestModelFactory.createTestMeeting(meetingId, "Title", "Desc", hostUser, MeetingStatus.SCHEDULED);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meetingEntity));

        var hostUserDto = TestModelFactory.createTestUserDto(hostId, "Host", "User", "host@example.com");
        when(userService.getUsersByMeetingId(meetingId)).thenReturn(List.of(hostUserDto));

        when(calendarService.getCalendarByUserId(anyLong())).thenReturn(TestModelFactory.createTestCalendarDto(1L, timezone, hostId));
        when(slotService.getSlotInformation(meetingId, timezone)).thenReturn(List.of());

        assertThrows(SlotNotFoundException.class, () -> meetingService.getMeetingById(meetingId));

        verify(meetingRepository, times(1)).findById(meetingId);
        verify(slotService, times(1)).getSlotInformation(meetingId, timezone);
    }

    @Test
    void updateMeeting_Success_WithParticipantsChange() {
        Long meetingId = 1L;
        Long hostId = 10L;
        Long newParticipantId = 20L;
        Long hostSlotId = 100L;
        Long oldParticipantSlotId = 101L;
        Long newParticipantSlotId = 102L;
        String timezone = "UTC";
        var title = "Updated Title";
        var description = "Updated Desc";

        var updateMeetingRequest = TestModelFactory.createTestUpdateMeetingRequest(
                title, description, List.of(newParticipantId), timezone);
        when(meetingValidator.validateRequest(updateMeetingRequest)).thenReturn(updateMeetingRequest);

        var hostUser = TestModelFactory.createTestUser(hostId, "host@example.com", Status.ACTIVE);
        var meetingEntity = TestModelFactory.createTestMeeting(meetingId, "Old", "Old", hostUser, MeetingStatus.SCHEDULED);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meetingEntity));
        when(meetingRepository.save(any(Meeting.class))).thenReturn(meetingEntity);

        var hostSlot = SlotInformationDto.builder()
                .id(hostSlotId)
                .role(ParticipantRole.HOST)
                .startTime("2026-06-10T10:00:00Z")
                .endTime("2026-06-10T11:00:00Z")
                .build();
        var oldParticipantSlot = SlotInformationDto.builder()
                .id(oldParticipantSlotId)
                .role(ParticipantRole.INVITEE)
                .build();
        when(slotService.getSlotInformation(meetingId, timezone)).thenReturn(List.of(hostSlot, oldParticipantSlot));

        var newParticipantSlotResponse = TestModelFactory.createTestSlotResponse(
                newParticipantSlotId,
                2L,
                null,
                "2026-06-10T10:00:00Z",
                "2026-06-10T11:00:00Z",
                SlotStatus.FREE,
                null);
        when(slotService.getSlots(Set.of(newParticipantId), "2026-06-10T10:00:00Z", "2026-06-10T11:00:00Z", timezone, SlotStatus.FREE))
                .thenReturn(Map.of(newParticipantId, List.of(newParticipantSlotResponse)));

        meetingService.updateMeeting(meetingId, updateMeetingRequest);

        verify(meetingValidator, times(1)).validateRequest(updateMeetingRequest);
        verify(meetingRepository, times(1)).findById(meetingId);
        verify(meetingMapper, times(1)).updateEntityFromDto(any(), eq(meetingEntity));
        verify(meetingRepository, times(1)).save(meetingEntity);
        verify(slotService, times(1)).getSlotInformation(meetingId, timezone);
        verify(slotService, times(2)).updateSlot(eq(hostSlotId), any());
        verify(slotService, times(1))
                .updateSlots(eq(Set.of(oldParticipantSlotId)), any());
        verify(slotService, times(1))
                .updateSlots(eq(Set.of(newParticipantSlotId)), any());
    }

    @Test
    void updateMeeting_Success_OnlyDetailsChange() {
        Long meetingId = 1L;
        var updateMeetingRequest = TestModelFactory.createTestUpdateMeetingRequest(
                "New Title", "New Desc", null, null);
        when(meetingValidator.validateRequest(updateMeetingRequest)).thenReturn(updateMeetingRequest);

        var hostUser = TestModelFactory.createTestUser(10L, "host@example.com", Status.ACTIVE);
        var meetingEntity = TestModelFactory.createTestMeeting(meetingId, "Old", "Old", hostUser, MeetingStatus.SCHEDULED);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(meetingEntity));

        meetingService.updateMeeting(meetingId, updateMeetingRequest);

        verify(meetingRepository, times(1)).save(any(Meeting.class));
        verify(slotService, never()).getSlotInformation(anyLong(), anyString());
    }

    @Test
    void updateMeeting_NotFound_ThrowsException() {
        Long meetingId = 1L;
        var updateMeetingRequest = TestModelFactory.createTestUpdateMeetingRequest("T", "D", null, null);
        when(meetingValidator.validateRequest(updateMeetingRequest)).thenReturn(updateMeetingRequest);
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.empty());

        assertThrows(MeetingNotFoundException.class, () -> meetingService.updateMeeting(meetingId, updateMeetingRequest));

        verify(meetingRepository, times(1)).findById(meetingId);
        verify(meetingRepository, never()).save(any());
    }
}
