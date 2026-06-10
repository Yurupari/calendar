package com.yurupari.calendar.utils;

import com.yurupari.calendar.model.entity.Calendar;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.entity.Slot;
import com.yurupari.calendar.model.entity.User;
import com.yurupari.calendar.model.enums.MeetingStatus;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.repository.CalendarRepository;
import com.yurupari.calendar.repository.MeetingRepository;
import com.yurupari.calendar.repository.SlotRepository;
import com.yurupari.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TestEntityCreator {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private SlotRepository slotRepository;

    public User createTestUser() {
        return userRepository.save(User.builder()
                .name("Test")
                .lastName("User")
                .email("test.user@example.com")
                .build());
    }

    public User createTestUser(String name, String lastName) {
        return userRepository.save(User.builder()
                .name(name)
                .lastName(lastName)
                .email(name + "." + lastName + "@example.com")
                .build());
    }

    public Calendar createTestCalendar(User user) {
        return calendarRepository.save(Calendar.builder()
                .timezone("America/New_York")
                .user(user)
                .build());
    }

    public Meeting createTestMeeting(User host) {
        return meetingRepository.save(Meeting.builder()
                .host(host)
                .title("Test Meeting")
                .description("Description for test meeting")
                .status(MeetingStatus.SCHEDULED)
                .build());
    }

    public Slot createTestSlot(Calendar calendar, Meeting meeting) {
        return slotRepository.save(Slot.builder()
                .calendar(calendar)
                .meeting(meeting)
                .startTime(Instant.now().plusSeconds(3600))
                .endTime(Instant.now().plusSeconds(7200))
                .status(meeting == null ? SlotStatus.FREE : SlotStatus.BUSY)
                .build());
    }

    public Slot createTestSlot(Calendar calendar, Meeting meeting, ParticipantRole role) {
        return slotRepository.save(Slot.builder()
                .calendar(calendar)
                .meeting(meeting)
                .startTime(Instant.now().plusSeconds(3600))
                .endTime(Instant.now().plusSeconds(7200))
                .status(SlotStatus.BUSY)
                .role(role)
                .build());
    }

    public Slot createTestSlot(
            Calendar calendar,
            Meeting meeting,
            Instant startTime,
            Instant endTime,
            SlotStatus status,
            ParticipantRole role
    ) {
        return slotRepository.save(Slot.builder()
                .calendar(calendar)
                .meeting(meeting)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .role(role)
                .build());
    }
}
