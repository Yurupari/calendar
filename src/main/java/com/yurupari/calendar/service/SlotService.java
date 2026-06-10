package com.yurupari.calendar.service;

import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.request.CreateSlotRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.model.response.SlotResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SlotService {
    SlotResponse createSlot(CreateSlotRequest createSlotRequest);

    SlotResponse getSlotById(Long id);

    List<SlotResponse> getSlots(Long userId, String from, String until, SlotStatus status);

    Map<Long, List<SlotResponse>> getSlots(Set<Long> userIds, String from, String until, String timezone, SlotStatus status);

    void updateSlot(Long id, UpdateSlotRequest updateSlotRequest);

    void updateSlots(Set<Long> ids, UpdateSlotRequest updateSlotRequest);

    void deleteSlot(Long id);
}
