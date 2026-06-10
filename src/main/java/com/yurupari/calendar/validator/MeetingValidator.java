package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.InvalidFormatException;
import com.yurupari.calendar.exception.SlotConflictException;
import com.yurupari.calendar.model.request.UpdateMeetingRequest;
import com.yurupari.calendar.model.response.SlotResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MeetingValidator {

    public void validateParticipantsAvailability(Map<Long, List<SlotResponse>> userSlots) {
        var busyParticipants = userSlots.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();

        if (!busyParticipants.isEmpty()) {
            throw new SlotConflictException(busyParticipants);
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
