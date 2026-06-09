package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.ParticipantSlotConflictException;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.response.SlotResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class MeetingValidator {

    public void validateParticipantsAvailability(Map<Long, Optional<SlotResponse>> userSlots) {
        var busyParticipants = userSlots.entrySet().stream()
                .filter(entry -> {
                    var slot = entry.getValue();

                    if (slot.isEmpty()) {
                        throw new ParticipantSlotConflictException(entry.getKey());
                    }

                    return SlotStatus.BUSY.equals(slot.get().status());
                })
                .map(Map.Entry::getKey)
                .toList();

        if (!busyParticipants.isEmpty()) {
            throw new ParticipantSlotConflictException(busyParticipants);
        }
    }
}
