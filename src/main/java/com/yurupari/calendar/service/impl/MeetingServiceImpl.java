package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.MeetingNotFoundException;
import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.dto.MeetingDto;
import com.yurupari.calendar.model.dto.UserDto;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        var slotDto = slotService.getSlotById(createMeetingRequest.slotId());
        var hostId = createMeetingRequest.hostId();

        var participantsSlots = Optional.ofNullable(createMeetingRequest.participants())
                .map(participantList -> participantList.stream()
                        .collect(Collectors.toMap(
                                userId -> userId,
                                userId -> slotService.getSlots(userId, slotDto.startTime(), slotDto.endTime()).stream()
                                        .findFirst()
                        )))
                .orElseGet(Map::of);

        meetingValidator.validateParticipantsAvailability(participantsSlots);

        var meetingDto = MeetingDto.builder()
                .hostId(hostId)
                .title(createMeetingRequest.title())
                .description(createMeetingRequest.description())
                .build();
        var savedMeeting = meetingRepository.save(meetingMapper.toEntity(meetingDto));
        var meetingId = savedMeeting.getId();

        var hostSlotRequest = UpdateSlotRequest.builder()
                .meetingId(meetingId)
                .status(SlotStatus.BUSY)
                .role(ParticipantRole.HOST)
                .build();
        slotService.updateSlot(slotDto.id(), hostSlotRequest);

        var participantSlotRequest = UpdateSlotRequest.builder()
                .meetingId(meetingId)
                .status(SlotStatus.BUSY)
                .role(ParticipantRole.INVITEE)
                .build();
        var slotIds = participantsSlots.values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(SlotResponse::id)
                .toList();
        slotService.updateSlots(slotIds, participantSlotRequest);

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
