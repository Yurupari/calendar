package com.yurupari.calendar.controller.v1;

import com.yurupari.calendar.model.request.CreateMeetingRequest;
import com.yurupari.calendar.model.request.UpdateMeetingRequest;
import com.yurupari.calendar.model.response.MeetingResponse;
import com.yurupari.calendar.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meeting")
public class MeetingControllerV1 {

    @Autowired
    private MeetingService meetingService;

    @Operation(summary = "Create a meeting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully meeting created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/create")
    public ResponseEntity<MeetingResponse> createMeeting(@Valid @RequestBody CreateMeetingRequest createMeetingRequest) {
        var createdMeeting = meetingService.createMeeting(createMeetingRequest);
        return new ResponseEntity<>(createdMeeting, HttpStatus.CREATED);
    }

    @Operation(summary = "Get meeting information by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully meeting retrieved"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeetingById(@PathVariable Long id) {
        var meetingDto = meetingService.getMeetingById(id);
        return ResponseEntity.ok(meetingDto);
    }

    @Operation(summary = "Update a meeting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully meeting updated"),
            @ApiResponse(responseCode = "404", description = "Meeting not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateMeetingRequest updateMeetingRequest
    ) {
        meetingService.updateMeeting(id, updateMeetingRequest);
        return ResponseEntity.ok("Meeting updated successfully");
    }
}
