package com.yurupari.calendar.utils;

import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.dto.UserDto;
import com.yurupari.calendar.model.entity.Calendar;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.entity.Slot;
import com.yurupari.calendar.model.entity.User;
import com.yurupari.calendar.model.enums.MeetingStatus;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.request.CreateSlotRequest;
import com.yurupari.calendar.model.request.CreateUserRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.model.request.UpdateUserRequest;
import com.yurupari.calendar.model.response.SlotResponse;
import com.yurupari.calendar.model.response.UserResponse;

import java.time.Instant;
import java.time.LocalDateTime;

public class TestModelFactory {

    public static User createTestUser(Long id, String email, Status status) {
        return User.builder()
                .id(id)
                .name("John")
                .lastName("Doe")
                .email(email)
                .status(status)
                .build();
    }

    public static Calendar createTestCalendar(Long id, String timezone, User user, Status status) {
        return Calendar.builder()
                .id(id)
                .timezone(timezone)
                .user(user)
                .status(status)
                .build();
    }

    public static Meeting createTestMeeting(Long id, String title, String description, User host, MeetingStatus status) {
        return Meeting.builder()
                .id(id)
                .title(title)
                .description(description)
                .host(host)
                .status(status)
                .build();
    }

    public static Slot createTestSlot(Long id, Calendar calendar, Meeting meeting, Instant startTime, Instant endTime, SlotStatus status, ParticipantRole role) {
        return Slot.builder()
                .id(id)
                .calendar(calendar)
                .meeting(meeting)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .role(role)
                .build();
    }

    public static UserDto createTestUserDto(Long id, String name, String lastName, String email) {
        return UserDto.builder()
                .id(id)
                .name(name)
                .lastName(lastName)
                .email(email)
                .build();
    }

    public static CalendarDto createTestCalendarDto(Long id, String timezone, Long userId) {
        return CalendarDto.builder()
                .id(id)
                .timezone(timezone)
                .userId(userId)
                .build();
    }

    public static CreateUserRequest createTestCreateUserRequest(String name, String lastName, String email, String timezone) {
        return CreateUserRequest.builder()
                .name(name)
                .lastName(lastName)
                .email(email)
                .timezone(timezone)
                .build();
    }

    public static UpdateUserRequest createTestUpdateUserRequest(String name, String lastName, String email, String timezone) {
        return UpdateUserRequest.builder()
                .name(name)
                .lastName(lastName)
                .email(email)
                .timezone(timezone)
                .build();
    }

    public static UserResponse createTestUserResponse(Long id, String name, String lastName, String email, Long calendarId) {
        return UserResponse.builder()
                .id(id)
                .name(name)
                .lastName(lastName)
                .email(email)
                .calendarId(calendarId)
                .build();
    }

    public static CreateSlotRequest createTestCreateSlotRequest(Long userId, String startTime, String endTime) {
        return CreateSlotRequest.builder()
                .userId(userId)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    public static UpdateSlotRequest createTestUpdateSlotRequest(Long meetingId, String startTime, String endTime, SlotStatus status, ParticipantRole role) {
        return UpdateSlotRequest.builder()
                .meetingId(meetingId)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .role(role)
                .build();
    }

    public static SlotResponse createTestSlotResponse(Long id, Long calendarId, Long meetingId, String startTime, String endTime, SlotStatus status, ParticipantRole role) {
        return SlotResponse.builder()
                .id(id)
                .calendarId(calendarId)
                .meetingId(meetingId)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .role(role)
                .build();
    }
}
