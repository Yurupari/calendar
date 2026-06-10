package com.yurupari.calendar.exception;

public class MeetingNotFoundException extends RuntimeException {

    public MeetingNotFoundException(Long id) {
        super(String.format("Meeting not found: id=%s", id));
    }
}
