package com.yurupari.calendar.service;

import com.yurupari.calendar.model.request.CreateSlotRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.model.response.SlotResponse;

import java.util.List;

public interface SlotService {
    SlotResponse createSlot(CreateSlotRequest createSlotRequest);

    SlotResponse getSlotById(Long id);

    List<SlotResponse> getSlots(Long userId, String from, String until);

    void updateSlot(Long id, UpdateSlotRequest updateSlotRequest);

    void deleteSlot(Long id);
}
