package com.yurupari.calendar.controller.v1;

import com.yurupari.calendar.model.enums.SlotStatus;
import com.yurupari.calendar.model.request.CreateSlotRequest;
import com.yurupari.calendar.model.request.UpdateSlotRequest;
import com.yurupari.calendar.model.response.SlotResponse;
import com.yurupari.calendar.service.SlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/slot")
public class SlotControllerV1 {

    @Autowired
    private SlotService slotService;

    @Operation(summary = "Create a slot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully slot created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/create")
    public ResponseEntity<SlotResponse> createSlot(@Valid @RequestBody CreateSlotRequest createSlotRequest) {
        var createdSlot = slotService.createSlot(createSlotRequest);
        return new ResponseEntity<>(createdSlot, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a slot by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully slot retrieved"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SlotResponse> getSlotById(@PathVariable Long id) {
        var slot = slotService.getSlotById(id);
        return ResponseEntity.ok(slot);
    }

    @Operation(summary = "Get a slots")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully slots retrieved")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SlotResponse>> getSlots(
            @PathVariable Long userId,
            @RequestParam String from,
            @RequestParam String until,
            @RequestParam(required = false) SlotStatus status
    ) {
        var slot = slotService.getSlots(userId, from, until, status);
        return ResponseEntity.ok(slot);
    }

    @Operation(summary = "Update a slot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully slot updated"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> updateSlot(
            @PathVariable Long id,
            @RequestBody UpdateSlotRequest updateSlotRequest
    ) {
        slotService.updateSlot(id, updateSlotRequest);
        return ResponseEntity.ok("Slot updated successfully");
    }

    @Operation(summary = "Delete a slot by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully slot deleted"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }
}
