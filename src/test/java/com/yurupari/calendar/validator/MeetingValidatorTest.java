package com.yurupari.calendar.validator;

import com.yurupari.calendar.exception.ParticipantSlotConflictException;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.response.SlotResponse;
import com.yurupari.calendar.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MeetingValidatorTest {

    @InjectMocks
    private MeetingValidator meetingValidator;

    @Test
    void validateParticipantsAvailability_AllAvailable_DoesNotThrowException() {
        var userSlots = Map.of(
                1L, List.of(TestModelFactory.createTestSlotResponse(
                        1L,
                        1L,
                        null,
                        "start",
                        "end",
                        SlotStatus.FREE,
                        null)),
                2L, List.of(TestModelFactory.createTestSlotResponse(
                        2L,
                        1L,
                        null,
                        "start",
                        "end",
                        SlotStatus.FREE,
                        null))
        );

        assertDoesNotThrow(() -> meetingValidator.validateParticipantsAvailability(userSlots));
    }

    @Test
    void validateParticipantsAvailability_OneParticipantNoSlot_ThrowsException() {
        Map<Long, List<SlotResponse>> userSlots = Map.of(
                1L, List.of(TestModelFactory.createTestSlotResponse(
                        1L,
                        1L,
                        null,
                        "start",
                        "end",
                        SlotStatus.FREE,
                        null)),
                2L, List.of()
        );

        var exception = assertThrows(ParticipantSlotConflictException.class, () -> meetingValidator.validateParticipantsAvailability(userSlots));
        assertEquals("Some users have busy slots: users=[2]", exception.getMessage());
    }

    @Test
    void validateParticipantsAvailability_MultipleParticipantsNoSlot_ThrowsException() {
        Map<Long, List<SlotResponse>> userSlots = Map.of(
                1L, List.of(),
                2L, List.of(TestModelFactory.createTestSlotResponse(
                        2L,
                        1L,
                        null,
                        "start",
                        "end",
                        SlotStatus.FREE,
                        null)),
                3L, List.of()
        );

        var exception = assertThrows(ParticipantSlotConflictException.class, () -> meetingValidator.validateParticipantsAvailability(userSlots));
        assertTrue(exception.getMessage().contains("Some users have busy slots: users=["));
    }

    @Test
    void validateParticipantsAvailability_MixedBusyAndNoSlot_ThrowsException() {
        Map<Long, List<SlotResponse>> userSlots = Map.of(
                1L, List.of(TestModelFactory.createTestSlotResponse(
                        1L,
                        1L,
                        null,
                        "start",
                        "end",
                        SlotStatus.BUSY,
                        null)),
                2L, List.of(),
                3L, List.of(TestModelFactory.createTestSlotResponse(
                        3L,
                        1L,
                        null,
                        "start",
                        "end",
                        SlotStatus.FREE,
                        null))
        );

        var exception = assertThrows(ParticipantSlotConflictException.class, () -> meetingValidator.validateParticipantsAvailability(userSlots));
        assertEquals("Some users have busy slots: users=[2]", exception.getMessage());
    }

    @Test
    void validateParticipantsAvailability_EmptyMap_DoesNotThrowException() {
        var userSlots = Map.<Long, List<SlotResponse>>of();

        assertDoesNotThrow(() -> meetingValidator.validateParticipantsAvailability(userSlots));
    }
}