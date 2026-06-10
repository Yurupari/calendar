package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.ParticipantSlotConflictException;
import com.yurupari.calendar.model.response.SlotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MeetingValidator {

    public void validateParticipantsAvailability(Map<Long, List<SlotResponse>> userSlots) {
        var busyParticipants = userSlots.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();

        if (!busyParticipants.isEmpty()) {
            throw new ParticipantSlotConflictException(busyParticipants);
        }
    }
}
