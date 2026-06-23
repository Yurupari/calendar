package com.yurupari.calendar.service.impl;

import com.yurupari.calendar.exception.UserAlreadyExistsException;
import com.yurupari.calendar.exception.UserNotFoundException;
import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.dto.UserDto;
import com.yurupari.calendar.model.enums.Status;
import com.yurupari.calendar.model.mapper.UserMapper;
import com.yurupari.calendar.model.request.CreateUserRequest;
import com.yurupari.calendar.model.request.UpdateUserRequest;
import com.yurupari.calendar.model.response.UserResponse;
import com.yurupari.calendar.repository.UserRepository;
import com.yurupari.calendar.service.CalendarService;
import com.yurupari.calendar.service.UserService;
import com.yurupari.calendar.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final CalendarService calendarService;

    private final UserRepository userRepository;

    private final UserValidator userValidator;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        log.info("Creating user: request={}", createUserRequest);

        var userDto = UserDto.builder()
                .name(createUserRequest.name())
                .lastName(createUserRequest.lastName())
                .email(createUserRequest.email())
                .build();
        userValidator.validateUser(userDto);

        return userRepository.findByEmail(userDto.email())
                .map(u -> {
                    if (Status.ACTIVE.equals(u.getStatus())) {
                        throw new UserAlreadyExistsException(u.getEmail());
                    }

                    userMapper.updateEntityFromDto(userDto, u);
                    u.setStatus(Status.ACTIVE);

                    var savedUser = userRepository.save(u);

                    var existingCalendarDto = calendarService.activateCalendar(savedUser.getId());

                    return userMapper.toUserResponse(savedUser, existingCalendarDto);
                })
                .orElseGet(() -> {
                    var user = userMapper.toEntity(userDto);
                    var savedUser = userRepository.save(user);

                    var calendarDto = CalendarDto.builder()
                            .timezone(createUserRequest.timezone())
                            .userId(savedUser.getId())
                            .build();

                    var savedCalendarDto = calendarService.createCalendar(calendarDto);

                    return userMapper.toUserResponse(savedUser, savedCalendarDto);
                });
    }

    @Override
    public UserResponse getUserById(Long id) {
        log.info("Getting user: id={}", id);

        return userRepository.findById(id)
                .filter(u -> Status.ACTIVE.equals(u.getStatus()))
                .map(u -> {
                    var calendarDto = calendarService.getCalendarByUserId(u.getId());

                    return userMapper.toUserResponse(u, calendarDto);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.info("Getting user: email={}", email);

        return userRepository.findByEmail(email)
                .filter(u -> Status.ACTIVE.equals(u.getStatus()))
                .map(u -> {
                    var calendarDto = calendarService.getCalendarByUserId(u.getId());

                    return userMapper.toUserResponse(u, calendarDto);
                })
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public List<UserDto> getUsers(List<Long> userIds) {
        log.info("Getting users: ids=[{}]", userIds);

        return userRepository.findAllById(userIds).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public List<UserDto> getUsersByMeetingId(Long meetingId) {
        log.info("Getting users: meetingId={}", meetingId);

        return userRepository.findUsersByMeetingId(meetingId).stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void updateUser(Long id, UpdateUserRequest updateUserRequest) {
        log.info("Updating user: id={}, request={}", id, updateUserRequest);

        var userDto = UserDto.builder()
                .name(updateUserRequest.name())
                .lastName(updateUserRequest.lastName())
                .email(updateUserRequest.email())
                .build();
        userValidator.validateUser(userDto);

        var user = userRepository.findById(id)
                .filter(u -> Status.ACTIVE.equals(u.getStatus()))
                .orElseThrow(() -> new UserNotFoundException(id));

        userMapper.updateEntityFromDto(userDto, user);
        userRepository.save(user);

        calendarService.updateCalendar(user.getId(), updateUserRequest.timezone());
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Inactivating user: id={}", id);

        var user = userRepository.findById(id)
                .filter(u -> Status.ACTIVE.equals(u.getStatus()))
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setStatus(Status.INACTIVE);
        userRepository.save(user);

        calendarService.deleteCalendarByUserId(id);
    }
}
