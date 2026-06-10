package com.yurupari.calendar.exception;

import java.util.List;
import java.util.stream.Collectors;

public class SlotConflictException extends RuntimeException {

    public SlotConflictException(Long id, Long meetingId) {
        super(String.format("Slot have an associated meeting: id=%s, meetingId=%s", id, meetingId));
    }

    public SlotConflictException(Long userId) {
        super(String.format("User does not have an available slot: userId=%s", userId));
    }

    public SlotConflictException(List<Long> users) {
        var usersStr = users.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        super(String.format("Some users have busy slots: users=[%s]", usersStr));
    }
}
