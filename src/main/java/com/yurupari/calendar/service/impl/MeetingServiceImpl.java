package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.dto.MeetingDto;
import com.yurupari.calendar.model.dto.UserDto;
import com.yurupari.calendar.model.entity.Meeting;
import com.yurupari.calendar.model.enums.MeetingStatus;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.mapper.MeetingMapper;
import com.yurupari.calendar.model.request.CreateMeetingRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.model.response.MeetingResponse;
import com.yurupari.calendar.model.response.SlotResponse;
import com.yurupari.calendar.repository.MeetingRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    private final SlotService slotService;

    private final UserService userService;

    private final MeetingValidator meetingValidator;

    private final MeetingRepository meetingRepository;

    private final MeetingMapper meetingMapper;

    @Override
    @Transactional
    public MeetingResponse createMeeting(CreateMeetingRequest createMeetingRequest) {
        log.info("Creating meeting: request={}", createMeetingRequest);

        var hostSlot = slotService.getSlotById(createMeetingRequest.slotId());
        var hostId = createMeetingRequest.hostId();
        var participantsIds = new HashSet<>(createMeetingRequest.participants());

        var participantsSlots = getParticipantSlots(participantsIds, hostSlot, createMeetingRequest);

        var savedMeeting = saveMeeting(createMeetingRequest);
        var meetingId = savedMeeting.getId();

        var participantsSlotIds = participantsSlots.values().stream()
                .filter(slotList -> slotList != null && !slotList.isEmpty())
                .map(slotList -> slotList.stream().findFirst())
                .map(slotResponse -> slotResponse.get().id())
                .collect(Collectors.toSet());
        updateSlots(meetingId, hostSlot.id(), participantsSlotIds, savedMeeting.getStatus());

        var userIds = new ArrayList<>(createMeetingRequest.participants());
        userIds.add(hostId);
        var users = userService.getUsers(userIds);
        var host = getHost(hostId, users);
        var participants = getParticipants(hostId, users);

        return MeetingResponse.builder()
                .id(meetingId)
                .host(host)
                .title(savedMeeting.getTitle())
                .description(savedMeeting.getDescription())
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
        var hostId = meeting.getHost().getId();
        var host = getHost(hostId, users);
        var participants = getParticipants(hostId, users);

        return MeetingResponse.builder()
                .id(meetingId)
                .host(host)
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .participants(participants)
                .build();
    }

    private Map<Long, List<SlotResponse>> getParticipantSlots(Set<Long> participantsIds, SlotResponse slot, CreateMeetingRequest request) {
        var participantsSlots = slotService.getSlots(
                participantsIds,
                slot.startTime(),
                slot.endTime(),
                request.timezone(),
                SlotStatus.FREE);
        meetingValidator.validateParticipantsAvailability(participantsSlots);

        return participantsSlots;
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

        var hostSlotRequest = UpdateSlotRequest.builder()
                .meetingId(meetingId)
                .status(slotStatus)
                .role(hostRole)
                .build();
        slotService.updateSlot(hostSlotId, hostSlotRequest);

        var participantSlotRequest = UpdateSlotRequest.builder()
                .meetingId(meetingId)
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
