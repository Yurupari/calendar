package com.yurupari.calendar.exception;

public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(Long id) {
        super(String.format("Slot not found: id=%s", id));
    }

    public SlotNotFoundException(String message) {
        super(message);
    }
}
