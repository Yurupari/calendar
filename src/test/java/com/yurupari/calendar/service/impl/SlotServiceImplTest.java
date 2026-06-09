package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.CalendarNotFoundException;
import com.yurupari.calendar.exception.SlotAlreadyExistsException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.dto.SlotDto;
import com.yurupari.calendar.model.entity.Calendar;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.entity.Slot;
import com.yurupari.calendar.model.entity.User;
import com.yurupari.calendar.model.enums.MeetingStatus;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.mapper.SlotMapperImpl;
import com.yurupari.calendar.repository.SlotRepository;
import com.yurupari.calendar.service.CalendarService;
import com.yurupari.calendar.util.TimeUtil;
import com.yurupari.calendar.utils.TestModelFactory;
import com.yurupari.calendar.validator.SlotValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @InjectMocks
    private SlotServiceImpl slotService;

    @Mock
    private CalendarService calendarService;

    @Mock
    private SlotValidator slotValidator;

    @Mock
    private SlotRepository slotRepository;

    @Spy
    private SlotMapperImpl slotMapper = new SlotMapperImpl();

    @Mock
    private TimeUtil timeUtil;

    private User createTestUser(Long id) {
        return TestModelFactory.createTestUser(id, "test@example.com", Status.ACTIVE);
    }

    private Calendar createTestCalendar(Long id, User user) {
        return TestModelFactory.createTestCalendar(id, "UTC", user, Status.ACTIVE);
    }

    private Meeting createTestMeeting(Long id, User host) {
        return TestModelFactory.createTestMeeting(id, "Test Meeting", "Description", host, MeetingStatus.SCHEDULED);
    }

    private Slot createTestSlot(Long id, Calendar calendar, Meeting meeting, Instant startTime, Instant endTime) {
        var slotStatus = meeting == null ? SlotStatus.FREE : SlotStatus.BUSY;
        var participantRol = meeting == null ? null : ParticipantRole.INVITEE;
        return TestModelFactory.createTestSlot(id, calendar, meeting, startTime, endTime, slotStatus, participantRol);
    }

    private CalendarDto createTestCalendarDto(Long id, Long userId) {
        return TestModelFactory.createTestCalendarDto(id, "UTC", userId);
    }

    @Test
    void createSlot_Success() {
        Long userId = 1L;
        Long calendarId = 10L;
        Long slotId = 100L;
        var startTimeStr = "2024-01-01T09:00:00";
        var endTimeStr = "2024-01-01T10:00:00";
        var startTime = Instant.parse("2024-01-01T09:00:00Z");
        var endTime = Instant.parse("2024-01-01T10:00:00Z");

        var createSlotRequest = TestModelFactory.createTestCreateSlotRequest(userId, startTimeStr, endTimeStr);
        var calendarDto = createTestCalendarDto(calendarId, userId);
        var user = createTestUser(userId);
        var calendar = createTestCalendar(calendarId, user);
        var slotEntity = createTestSlot(null, calendar, null, startTime, endTime);
        var savedSlotEntity = createTestSlot(slotId, calendar, null, startTime, endTime);

        doNothing().when(slotValidator).validateDates(anyString(), anyString());
        when(calendarService.getCalendarByUserId(userId)).thenReturn(calendarDto);
        when(timeUtil.parseIsoStringToInstant(startTimeStr, calendarDto.timezone())).thenReturn(startTime);
        when(timeUtil.parseIsoStringToInstant(endTimeStr, calendarDto.timezone())).thenReturn(endTime);
        when(slotRepository.findByCalendarIdAndStartTimeAndEndTime(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(Optional.empty());
        when(slotMapper.toEntity(any(SlotDto.class))).thenReturn(slotEntity);
        when(slotRepository.save(any(Slot.class))).thenReturn(savedSlotEntity);
        when(timeUtil.parseInstantToIsoString(any(Instant.class), anyString())).thenReturn(startTimeStr, endTimeStr);

        var result = slotService.createSlot(createSlotRequest);

        assertNotNull(result);
        assertEquals(slotId, result.id());
        assertEquals(calendarId, result.calendarId());
        assertEquals(startTimeStr, result.startTime());
        assertEquals(endTimeStr, result.endTime());
        assertEquals(SlotStatus.FREE, result.status());
        assertEquals(null, result.role());

        verify(slotValidator, times(1)).validateDates(startTimeStr, endTimeStr);
        verify(calendarService, times(1)).getCalendarByUserId(userId);
        verify(timeUtil, times(1)).parseIsoStringToInstant(startTimeStr, calendarDto.timezone());
        verify(timeUtil, times(1)).parseIsoStringToInstant(endTimeStr, calendarDto.timezone());
        verify(slotRepository, times(1))
                .findByCalendarIdAndStartTimeAndEndTime(calendarId, startTime, endTime);
        verify(slotMapper, times(1)).toEntity(any(SlotDto.class));
        verify(slotRepository, times(1)).save(any(Slot.class));
        verify(timeUtil, times(2)).parseInstantToIsoString(any(Instant.class), anyString());
    }

    @Test
    void createSlot_SlotAlreadyExists_ThrowsException() {
        Long userId = 1L;
        Long calendarId = 10L;
        Long existingSlotId = 100L;
        var startTimeStr = "2024-01-01T09:00:00";
        var endTimeStr = "2024-01-01T10:00:00";
        var startTime = Instant.parse("2024-01-01T09:00:00Z");
        var endTime = Instant.parse("2024-01-01T10:00:00Z");

        var createSlotRequest = TestModelFactory.createTestCreateSlotRequest(userId, startTimeStr, endTimeStr);
        var calendarDto = createTestCalendarDto(calendarId, userId);
        var user = createTestUser(userId);
        var calendar = createTestCalendar(calendarId, user);
        var existingSlot = createTestSlot(existingSlotId, calendar, null, startTime, endTime);

        doNothing().when(slotValidator).validateDates(anyString(), anyString());
        when(calendarService.getCalendarByUserId(userId)).thenReturn(calendarDto);
        when(timeUtil.parseIsoStringToInstant(startTimeStr, calendarDto.timezone())).thenReturn(startTime);
        when(timeUtil.parseIsoStringToInstant(endTimeStr, calendarDto.timezone())).thenReturn(endTime);
        when(slotRepository.findByCalendarIdAndStartTimeAndEndTime(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(Optional.of(existingSlot));

        assertThrows(SlotAlreadyExistsException.class, () -> slotService.createSlot(createSlotRequest));

        verify(slotValidator, times(1)).validateDates(startTimeStr, endTimeStr);
        verify(calendarService, times(1)).getCalendarByUserId(userId);
        verify(timeUtil, times(1)).parseIsoStringToInstant(startTimeStr, calendarDto.timezone());
        verify(timeUtil, times(1)).parseIsoStringToInstant(endTimeStr, calendarDto.timezone());
        verify(slotRepository, times(1))
                .findByCalendarIdAndStartTimeAndEndTime(calendarId, startTime, endTime);
        verify(slotMapper, never()).toEntity(any(SlotDto.class));
        verify(slotRepository, never()).save(any(Slot.class));
    }

    @Test
    void createSlot_InvalidDates_ThrowsException() {
        Long userId = 1L;
        var startTimeStr = "2024-01-01T10:00:00";
        var endTimeStr = "2024-01-01T09:00:00";

        var createSlotRequest = TestModelFactory.createTestCreateSlotRequest(userId, startTimeStr, endTimeStr);

        doThrow(new IllegalArgumentException("Invalid dates")).when(slotValidator).validateDates(anyString(), anyString());

        assertThrows(IllegalArgumentException.class, () -> slotService.createSlot(createSlotRequest));

        verify(slotValidator, times(1)).validateDates(startTimeStr, endTimeStr);
        verify(calendarService, never()).getCalendarByUserId(anyLong());
        verify(timeUtil, never()).parseIsoStringToInstant(anyString(), anyString());
        verify(slotRepository, never())
                .findByCalendarIdAndStartTimeAndEndTime(anyLong(), any(Instant.class), any(Instant.class));
        verify(slotMapper, never()).toEntity(any(SlotDto.class));
        verify(slotRepository, never()).save(any(Slot.class));
    }

    @Test
    void createSlot_CalendarNotFound_ThrowsException() {
        Long userId = 1L;
        var startTimeStr = "2024-01-01T09:00:00";
        var endTimeStr = "2024-01-01T10:00:00";

        var createSlotRequest = TestModelFactory.createTestCreateSlotRequest(userId, startTimeStr, endTimeStr);

        doNothing().when(slotValidator).validateDates(anyString(), anyString());
        when(calendarService.getCalendarByUserId(userId)).thenThrow(new CalendarNotFoundException("Calendar not found"));

        assertThrows(CalendarNotFoundException.class, () -> slotService.createSlot(createSlotRequest));

        verify(slotValidator, times(1)).validateDates(startTimeStr, endTimeStr);
        verify(calendarService, times(1)).getCalendarByUserId(userId);
        verify(timeUtil, never()).parseIsoStringToInstant(anyString(), anyString());
        verify(slotRepository, never())
                .findByCalendarIdAndStartTimeAndEndTime(anyLong(), any(Instant.class), any(Instant.class));
        verify(slotMapper, never()).toEntity(any(SlotDto.class));
        verify(slotRepository, never()).save(any(Slot.class));
    }

    @Test
    void getSlotById_Success() {
        Long slotId = 1L;
        Long calendarId = 10L;
        Long meetingId = 20L;
        Long userId = 1L;
        var startTimeStr = "2024-01-01T09:00:00";
        var endTimeStr = "2024-01-01T10:00:00";
        var startTime = Instant.parse("2024-01-01T09:00:00Z");
        var endTime = Instant.parse("2024-01-01T10:00:00Z");

        var user = createTestUser(userId);
        var calendar = createTestCalendar(calendarId, user);
        var meeting = createTestMeeting(meetingId, user);
        var slotEntity = createTestSlot(slotId, calendar, meeting, startTime, endTime);
        slotEntity.setStatus(SlotStatus.BUSY);
        slotEntity.setRole(ParticipantRole.HOST);

        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slotEntity));
        when(timeUtil.parseInstantToIsoString(any(Instant.class), anyString())).thenReturn(startTimeStr, endTimeStr);

        var result = slotService.getSlotById(slotId);

        assertNotNull(result);
        assertEquals(slotId, result.id());
        assertEquals(calendarId, result.calendarId());
        assertEquals(meetingId, result.meetingId());
        assertEquals(startTimeStr, result.startTime());
        assertEquals(endTimeStr, result.endTime());
        assertEquals(SlotStatus.BUSY, result.status());
        assertEquals(ParticipantRole.HOST, result.role());

        verify(slotRepository, times(1)).findById(slotId);
        verify(timeUtil, times(2)).parseInstantToIsoString(any(Instant.class), anyString());
    }

    @Test
    void getSlotById_NotFound_ThrowsException() {
        Long slotId = 1L;

        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        assertThrows(SlotNotFoundException.class, () -> slotService.getSlotById(slotId));

        verify(slotRepository, times(1)).findById(slotId);
        verify(timeUtil, never()).parseInstantToIsoString(any(Instant.class), anyString());
    }

    @Test
    void getSlots_Success() {
        Long userId = 1L;
        Long calendarId = 10L;
        Long meetingId1 = 20L;
        Long meetingId2 = 21L;

        var fromStr = "2024-01-01T00:00:00";
        var untilStr = "2024-01-01T23:59:59";
        var fromInstant = Instant.parse("2024-01-01T00:00:00Z");
        var untilInstant = Instant.parse("2024-01-01T23:59:59Z");

        var user = createTestUser(userId);
        var calendar = createTestCalendar(calendarId, user);
        var calendarDto = createTestCalendarDto(calendarId, userId);
        var meeting1 = createTestMeeting(meetingId1, user);
        var meeting2 = createTestMeeting(meetingId2, user);

        var slot1 = createTestSlot(
                100L,
                calendar,
                meeting1,
                Instant.parse("2024-01-01T09:00:00Z"),
                Instant.parse("2024-01-01T10:00:00Z"));
        slot1.setStatus(SlotStatus.FREE);
        slot1.setRole(ParticipantRole.HOST);
        var slot2 = createTestSlot(
                101L,
                calendar,
                meeting2,
                Instant.parse("2024-01-01T11:00:00Z"),
                Instant.parse("2024-01-01T12:00:00Z"));
        slot2.setStatus(SlotStatus.BUSY);
        slot2.setRole(ParticipantRole.INVITEE);

        var foundSlots = List.of(slot1, slot2);

        when(calendarService.getCalendarByUserId(anyLong())).thenReturn(calendarDto);
        when(timeUtil.parseIsoStringToInstant(fromStr, calendarDto.timezone())).thenReturn(fromInstant);
        when(timeUtil.parseIsoStringToInstant(untilStr, calendarDto.timezone())).thenReturn(untilInstant);
        when(slotRepository.findByCalendarIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(foundSlots);
        when(timeUtil.parseInstantToIsoString(any(Instant.class), anyString())).thenReturn(
                "2024-01-01T09:00:00", "2024-01-01T10:00:00",
                "2024-01-01T11:00:00", "2024-01-01T12:00:00"
        );

        var result = slotService.getSlots(userId, fromStr, untilStr);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(slot1.getId(), result.get(0).id());
        assertEquals(slot2.getId(), result.get(1).id());

        verify(timeUtil, times(1)).parseIsoStringToInstant(fromStr, calendar.getTimezone());
        verify(timeUtil, times(1)).parseIsoStringToInstant(untilStr, calendar.getTimezone());
        verify(slotRepository, times(1))
                .findByCalendarIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(anyLong(), any(), any());
        verify(timeUtil, times(4)).parseInstantToIsoString(any(Instant.class), anyString());
    }

    @Test
    void getSlots_EmptyList() {
        Long userId = 1L;
        var fromStr = "2024-01-01T00:00:00";
        var untilStr = "2024-01-01T23:59:59";
        var fromInstant = Instant.parse("2024-01-01T00:00:00Z");
        var untilInstant = Instant.parse("2024-01-01T23:59:59Z");

        when(calendarService.getCalendarByUserId(anyLong())).thenReturn(createTestCalendarDto(10L, userId));
        when(timeUtil.parseIsoStringToInstant(fromStr, "UTC")).thenReturn(fromInstant);
        when(timeUtil.parseIsoStringToInstant(untilStr, "UTC")).thenReturn(untilInstant);
        when(slotRepository.findByCalendarIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        var result = slotService.getSlots(userId, fromStr, untilStr);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(timeUtil, times(1)).parseIsoStringToInstant(fromStr, "UTC");
        verify(timeUtil, times(1)).parseIsoStringToInstant(untilStr, "UTC");
        verify(slotRepository, times(1))
                .findByCalendarIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(anyLong(), any(), any());
        verify(timeUtil, never()).parseInstantToIsoString(any(), anyString());
    }

    @Test
    void updateSlot_Success() {
        Long slotId = 1L;
        Long calendarId = 10L;
        Long meetingId = 20L;
        Long userId = 1L;
        var newStartTimeStr = "2024-01-01T10:00:00";
        var newEndTimeStr = "2024-01-01T11:00:00";
        var newStartTime = Instant.parse("2024-01-01T10:00:00Z");
        var newEndTime = Instant.parse("2024-01-01T11:00:00Z");

        var updateSlotRequest = TestModelFactory.createTestUpdateSlotRequest(
                meetingId,
                newStartTimeStr,
                newEndTimeStr,
                SlotStatus.BUSY,
                ParticipantRole.INVITEE);
        var user = createTestUser(userId);
        var calendar = createTestCalendar(calendarId, user);
        var meeting = createTestMeeting(meetingId, user);
        var existingSlot = createTestSlot(slotId,
                calendar,
                null,
                Instant.parse("2024-01-01T09:00:00Z"),
                Instant.parse("2024-01-01T10:00:00Z"));
        var newSlot = createTestSlot(
                slotId,
                calendar,
                meeting,
                newStartTime,
                newEndTime
        );

        doNothing().when(slotValidator).validateDates(anyString(), anyString());
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(existingSlot));
        when(timeUtil.parseIsoStringToInstant(newStartTimeStr, calendar.getTimezone())).thenReturn(newStartTime);
        when(timeUtil.parseIsoStringToInstant(newEndTimeStr, calendar.getTimezone())).thenReturn(newEndTime);
        when(slotRepository.save(any(Slot.class))).thenReturn(existingSlot);

        slotService.updateSlot(slotId, updateSlotRequest);

        assertEquals(newStartTime, newSlot.getStartTime());
        assertEquals(newEndTime, newSlot.getEndTime());
        assertEquals(SlotStatus.BUSY, newSlot.getStatus());
        assertEquals(ParticipantRole.INVITEE, newSlot.getRole());
        assertNotNull(newSlot.getMeeting());
        assertEquals(meetingId, newSlot.getMeeting().getId());

        verify(slotValidator, times(1)).validateDates(newStartTimeStr, newEndTimeStr);
        verify(slotRepository, times(1)).findById(slotId);
        verify(timeUtil, times(1)).parseIsoStringToInstant(newStartTimeStr, calendar.getTimezone());
        verify(timeUtil, times(1)).parseIsoStringToInstant(newEndTimeStr, calendar.getTimezone());
        verify(slotMapper, times(1)).updateEntityFromDto(any(SlotDto.class), any(Slot.class));
        verify(slotRepository, times(1)).save(any(Slot.class));
    }

    @Test
    void updateSlot_NotFound_ThrowsException() {
        Long slotId = 1L;
        var startTimeStr = "2024-01-01T10:00:00";
        var endTimeStr = "2024-01-01T11:00:00";
        var updateSlotRequest = TestModelFactory.createTestUpdateSlotRequest(
                null,
                startTimeStr,
                endTimeStr,
                SlotStatus.BUSY,
                null);

        doNothing().when(slotValidator).validateDates(anyString(), anyString());
        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        assertThrows(SlotNotFoundException.class, () -> slotService.updateSlot(slotId, updateSlotRequest));

        verify(slotValidator, times(1)).validateDates(startTimeStr, endTimeStr);
        verify(slotRepository, times(1)).findById(slotId);
        verify(timeUtil, never()).parseIsoStringToInstant(anyString(), anyString());
        verify(slotMapper, never()).updateEntityFromDto(any(SlotDto.class), any(Slot.class));
        verify(slotRepository, never()).save(any(Slot.class));
    }

    @Test
    void updateSlot_InvalidDates_ThrowsException() {
        Long slotId = 1L;
        var startTimeStr = "2024-01-01T11:00:00";
        var endTimeStr = "2024-01-01T10:00:00";
        var updateSlotRequest = TestModelFactory.createTestUpdateSlotRequest(
                null,
                startTimeStr,
                endTimeStr,
                SlotStatus.BUSY,
                null);

        doThrow(new IllegalArgumentException("Invalid dates")).when(slotValidator).validateDates(anyString(), anyString());

        assertThrows(IllegalArgumentException.class, () -> slotService.updateSlot(slotId, updateSlotRequest));

        verify(slotValidator, times(1)).validateDates(startTimeStr, endTimeStr);
        verify(slotRepository, never()).findById(anyLong());
        verify(timeUtil, never()).parseIsoStringToInstant(anyString(), anyString());
        verify(slotMapper, never()).updateEntityFromDto(any(SlotDto.class), any(Slot.class));
        verify(slotRepository, never()).save(any(Slot.class));
    }

    @Test
    void deleteSlot_Success() {
        Long slotId = 1L;

        doNothing().when(slotRepository).deleteById(slotId);

        slotService.deleteSlot(slotId);

        verify(slotRepository, times(1)).deleteById(slotId);
    }

    @Test
    void deleteSlot_NotFound_NoException() {
        Long slotId = 1L;

        doNothing().when(slotRepository).deleteById(slotId);

        slotService.deleteSlot(slotId);

        verify(slotRepository, times(1)).deleteById(slotId);
    }
}