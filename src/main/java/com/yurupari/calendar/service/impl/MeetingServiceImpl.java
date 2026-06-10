package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.dto.MeetingDto;
import com.yurupari.calendar.model.dto.SlotInformationDto;
import com.yurupari.calendar.model.dto.UserDto;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.enums.MeetingStatus;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.mapper.MeetingMapper;
import com.yurupari.calendar.model.mapper.UserMapper;
import com.yurupari.calendar.model.request.CreateMeetingRequest;
import com.yurupari.calendar.model.request.UpdateMeetingRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.model.response.MeetingResponse;
import com.yurupari.calendar.model.response.SlotResponse;
import com.yurupari.calendar.repository.MeetingRepository;
import com.yurupari.calendar.service.CalendarService;
import com.yurupari.calendar.service.MeetingService;
import com.yurupari.calendar.service.SlotService;
import com.yurupari.calendar.service.UserService;
import com.yurupari.calendar.validator.MeetingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    private final SlotService slotService;

    private final UserService userService;

    private final CalendarService calendarService;

    private final MeetingValidator meetingValidator;

    private final MeetingRepository meetingRepository;

    private final MeetingMapper meetingMapper;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public MeetingResponse createMeeting(CreateMeetingRequest createMeetingRequest) {
        log.info("Creating meeting: request={}", createMeetingRequest);

        var hostSlot = slotService.getSlotById(createMeetingRequest.slotId());
        meetingValidator.validateHostSlot(hostSlot);

        var hostId = createMeetingRequest.hostId();
        var participantsIds = new HashSet<>(createMeetingRequest.participants());
        var participantsSlotIds = getParticipantSlotsIds(
                participantsIds,
                hostSlot.startTime(),
                hostSlot.endTime(),
                createMeetingRequest.timezone());

        var savedMeeting = saveMeeting(createMeetingRequest);
        var meetingId = savedMeeting.getId();
        updateSlots(meetingId, hostSlot.id(), participantsSlotIds, savedMeeting.getStatus());

        var userIds = new ArrayList<>(createMeetingRequest.participants());
        userIds.add(hostId);
        var users = userService.getUsers(userIds);
        var host = getHost(hostId, users);
        var participants = getParticipants(hostId, users);

        var slotInformation = SlotInformationDto.builder()
                .id(hostSlot.id())
                .calendarId(hostSlot.calendarId())
                .startTime(hostSlot.startTime())
                .endTime(hostSlot.endTime())
                .build();

        return MeetingResponse.builder()
                .id(meetingId)
                .host(host)
                .title(savedMeeting.getTitle())
                .description(savedMeeting.getDescription())
                .slot(slotInformation)
                .participants(participants)
                .build();
    }

    @Override
    public MeetingResponse getMeetingById(Long id) {
        log.info("Getting meeting: id={}", id);

        var meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new MeetingNotFoundException(id));

        var meetingId = meeting.getId();
        var users = userService.getUsersByMeetingId(meetingId);
        var host = userMapper.toDto(meeting.getHost());
        var hostId = host.id();
        var timezone = calendarService.getCalendarByUserId(hostId).timezone();
        var participants = getParticipants(hostId, users);

        return MeetingResponse.builder()
                .id(meetingId)
                .host(host)
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .slot(slotService.getSlotInformation(meetingId, timezone).stream()
                        .filter(slot -> ParticipantRole.HOST.equals(slot.role()))
                        .findFirst()
                        .orElseThrow(() -> new SlotNotFoundException(String.format("Slot not found: meetingId=%s", meetingId))))
                .participants(participants)
                .build();
    }

    @Override
    @Transactional
    public void updateMeeting(Long id, UpdateMeetingRequest updateMeetingRequest) {
        log.info("Updating meeting: id={}, request={}", id, updateMeetingRequest);

        var validatedRequest = meetingValidator.validateRequest(updateMeetingRequest);

        var meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new MeetingNotFoundException(id));

        var meetingDto = MeetingDto.builder()
                .title(validatedRequest.title())
                .description(validatedRequest.description())
                .build();
        meetingMapper.updateEntityFromDto(meetingDto, meeting);
        meetingRepository.save(meeting);

        Optional.ofNullable(validatedRequest.participants()).ifPresent(participants -> {
            var timezone = validatedRequest.timezone();

            var associatedSlots = slotService.getSlotInformation(meeting.getId(), timezone);
            var hostSlot = associatedSlots.stream()
                    .filter(slot -> ParticipantRole.HOST.equals(slot.role()))
                    .findFirst()
                    .orElseThrow(() -> new SlotNotFoundException(String.format("Slot not found: meetingId=%s", meeting.getId())));

            var participantsIds = new HashSet<>(participants);
            var participantsSlotsIds = getParticipantSlotsIds(
                    participantsIds,
                    hostSlot.startTime(),
                    hostSlot.endTime(),
                    timezone);

            var previousParticipantsSlotsIds = associatedSlots.stream()
                    .filter(slot -> ParticipantRole.INVITEE.equals(slot.role()))
                    .map(SlotInformationDto::id)
                    .collect(Collectors.toSet());
            updateSlots(meeting.getId(), hostSlot.id(), previousParticipantsSlotsIds, MeetingStatus.CANCELLED);

            updateSlots(meeting.getId(), hostSlot.id(), participantsSlotsIds, MeetingStatus.SCHEDULED);
        });
    }

    private Set<Long> getParticipantSlotsIds(
            Set<Long> participantsIds,
            String startTime,
            String endTime,
            String timezone) {
        var participantsSlots = slotService.getSlots(
                participantsIds,
                startTime,
                endTime,
                timezone,
                SlotStatus.FREE);
        meetingValidator.validateParticipantsAvailability(participantsSlots);

        return participantsSlots.values().stream()
                .filter(slotList -> slotList != null && !slotList.isEmpty())
                .map(slotList -> slotList.stream().findFirst())
                .map(slotResponse -> slotResponse.get().id())
                .collect(Collectors.toSet());
    }

    private Meeting saveMeeting(CreateMeetingRequest request) {
        var meetingDto = MeetingDto.builder()
                .hostId(request.hostId())
                .title(request.title())
                .description(request.description())
                .build();
        return meetingRepository.save(meetingMapper.toEntity(meetingDto));
    }

    private void updateSlots(Long meetingId, Long hostSlotId, Set<Long> participantSlotIds, MeetingStatus meetingStatus) {
        var slotStatus = MeetingStatus.SCHEDULED.equals(meetingStatus) ? SlotStatus.BUSY : SlotStatus.FREE;
        var hostRole = MeetingStatus.SCHEDULED.equals(meetingStatus) ? ParticipantRole.HOST : null;
        var participantRole = MeetingStatus.SCHEDULED.equals(meetingStatus) ? ParticipantRole.INVITEE : null;
        var validatedMeetingId = MeetingStatus.SCHEDULED.equals(meetingStatus) ? meetingId : null;

        var hostSlotRequest = UpdateSlotRequest.builder()
                .meetingId(validatedMeetingId)
                .status(slotStatus)
                .role(hostRole)
                .build();
        slotService.updateSlot(hostSlotId, hostSlotRequest);

        var participantSlotRequest = UpdateSlotRequest.builder()
                .meetingId(validatedMeetingId)
                .status(slotStatus)
                .role(participantRole)
                .build();
        slotService.updateSlots(participantSlotIds, participantSlotRequest);
    }

    private List<UserDto> getParticipants(Long hostId, List<UserDto> users) {
        return users.stream()
                .filter(u -> !hostId.equals(u.id()))
                .toList();
    }

    private UserDto getHost(Long hostId, List<UserDto> users) {
        return users.stream()
                .filter(u -> hostId.equals(u.id()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(hostId));
    }
}
