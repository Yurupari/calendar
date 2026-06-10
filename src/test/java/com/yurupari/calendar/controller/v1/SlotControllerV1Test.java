package com.yurupari.calendar.controller.v1;

import com.yurupari.calendar.exception.SlotNotFoundException;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.request.CreateSlotRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.service.SlotService;
import com.yurupari.calendar.utils.TestModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotControllerV1Test {

    @InjectMocks
    private SlotControllerV1 slotController;

    @Mock
    private SlotService slotService;

    @Test
    void createSlot_Success() {
        var createSlotRequest = TestModelFactory.createTestCreateSlotRequest(
                1L,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusHours(1).toString()
        );
        var slotResponse = TestModelFactory.createTestSlotResponse(
                1L,
                10L,
                null,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusHours(1).toString(),
                SlotStatus.FREE,
                null
        );

        when(slotService.createSlot(any())).thenReturn(slotResponse);

        var responseEntity = slotController.createSlot(createSlotRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(slotResponse, responseEntity.getBody());

        verify(slotService, times(1)).createSlot(createSlotRequest);
    }

    @Test
    void getSlotById_Success() {
        var slotResponse = TestModelFactory.createTestSlotResponse(
                1L,
                10L,
                null,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusHours(1).toString(),
                SlotStatus.FREE,
                null
        );

        when(slotService.getSlotById(anyLong())).thenReturn(slotResponse);

        var responseEntity = slotController.getSlotById(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(slotResponse, responseEntity.getBody());

        verify(slotService, times(1)).getSlotById(1L);
    }

    @Test
    void getSlotById_NotFound_ThrowsException() {
        when(slotService.getSlotById(anyLong())).thenThrow(new SlotNotFoundException(1L));

        assertThrows(SlotNotFoundException.class, () -> slotController.getSlotById(1L));

        verify(slotService, times(1)).getSlotById(1L);
    }

    @Test
    void getSlots_Success() {
        var slotResponse1 = TestModelFactory.createTestSlotResponse(
                1L,
                10L,
                null,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusHours(1).toString(),
                SlotStatus.FREE,
                null
        );
        var slotResponse2 = TestModelFactory.createTestSlotResponse(
                2L,
                10L,
                1L,
                LocalDateTime.now().plusHours(2).toString(),
                LocalDateTime.now().plusHours(3).toString(),
                SlotStatus.BUSY,
                ParticipantRole.HOST
        );
        var slotList = List.of(slotResponse1, slotResponse2);

        when(slotService.getSlots(anyLong(), anyString(), anyString(), any())).thenReturn(slotList);

        var responseEntity = slotController.getSlots(
                1L,
                "2023-01-01T00:00:00",
                "2023-01-01T23:59:59",
                SlotStatus.BUSY);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(slotList, responseEntity.getBody());

        verify(slotService, times(1))
                .getSlots(1L, "2023-01-01T00:00:00", "2023-01-01T23:59:59", SlotStatus.BUSY);
    }

    @Test
    void updateSlot_Success() {
        var updateSlotRequest = TestModelFactory.createTestUpdateSlotRequest(
                1L,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusHours(2).toString(),
                SlotStatus.BUSY,
                ParticipantRole.INVITEE
        );

        doNothing().when(slotService).updateSlot(anyLong(), any(UpdateSlotRequest.class));

        var responseEntity = slotController.updateSlot(1L, updateSlotRequest);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Slot updated successfully", responseEntity.getBody());

        verify(slotService, times(1)).updateSlot(1L, updateSlotRequest);
    }

    @Test
    void updateSlot_NotFound_ThrowsException() {
        var updateSlotRequest = TestModelFactory.createTestUpdateSlotRequest(
                1L,
                LocalDateTime.now().toString(),
                LocalDateTime.now().plusHours(2).toString(),
                SlotStatus.BUSY,
                ParticipantRole.HOST
        );

        doThrow(new SlotNotFoundException(1L)).when(slotService).updateSlot(anyLong(), any(UpdateSlotRequest.class));

        assertThrows(SlotNotFoundException.class, () -> slotController.updateSlot(1L, updateSlotRequest));

        verify(slotService, times(1)).updateSlot(1L, updateSlotRequest);
    }

    @Test
    void deleteSlot_Success() {
        doNothing().when(slotService).deleteSlot(anyLong());

        var responseEntity = slotController.deleteSlot(1L);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());

        verify(slotService, times(1)).deleteSlot(1L);
    }
}
