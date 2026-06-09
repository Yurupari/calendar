package com.yurupari.calendar.exception;

public class SlotAlreadyExistsException extends RuntimeException {

    public SlotAlreadyExistsException(Long id) {
        super(String.format("Slot already exists: id=%s", id));
    }
}
