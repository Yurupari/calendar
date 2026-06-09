package com.yurupari.calendar.exception;

import java.util.List;
import java.util.stream.Collectors;

public class ParticipantSlotConflictException extends RuntimeException {

    public ParticipantSlotConflictException(Long userId) {
        super(String.format("User does not have an available slot: userId=%s", userId));
    }

    public ParticipantSlotConflictException(List<Long> users) {
        var usersStr = users.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        super(String.format("Some users have busy slots: users=[%s]", usersStr));
    }
}
