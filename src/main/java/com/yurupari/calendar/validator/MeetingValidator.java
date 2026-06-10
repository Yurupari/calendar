package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.model.request.UpdateMeetingRequest;
import com.yurupari.calendar.model.response.SlotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MeetingValidator {

    public void validateHostSlot(SlotResponse slot) {
        var meetingId = slot.meetingId();
        if (meetingId != null) {
            throw new SlotConflictException(String.format("Slot has already been booked: slotId=%s", meetingId));
        }
    }

    public void validateParticipantsAvailability(Map<Long, List<SlotResponse>> userSlots) {
        if (userSlots.isEmpty()) {
            throw new SlotConflictException("No users available");
        }

        var busyParticipants = userSlots.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();

        if (!busyParticipants.isEmpty()) {
            var usersStr = busyParticipants.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            throw new SlotConflictException(String.format("Some users have busy slots: users=[%s]", usersStr));
        }
    }

    public UpdateMeetingRequest validateRequest(UpdateMeetingRequest request) {
        var timezone = Optional.ofNullable(request.timezone())
                .filter(t -> !t.isBlank())
                .orElseThrow(() -> new InvalidFormatException("Timezone cannot be null or empty"));
        var title = Optional.ofNullable(request.title())
                .filter(t -> !t.isBlank()).orElse(null);
        var description = Optional.ofNullable(request.description())
                .filter(d -> !d.isBlank()).orElse(null);

        return UpdateMeetingRequest.builder()
                .timezone(timezone)
                .title(title)
                .description(description)
                .participants(request.participants())
                .build();
    }
}
