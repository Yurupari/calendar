package com.yurupari.calendar.controller.v1;

import com.yurupari.calendar.model.dto.CalendarDto;
import com.yurupari.calendar.model.request.UpdateCalendarRequest;
import com.yurupari.calendar.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/calendar")
public class CalendarControllerV1 {

    @Autowired
    private CalendarService calendarService;

    @Operation(summary = "Get an calendar by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calendar retrieved"),
            @ApiResponse(responseCode = "404", description = "Calendar not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CalendarDto> getCalendarById(@PathVariable Long id) {
        var userDto = calendarService.getCalendarById(id);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Update calendar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully calendar retrieved"),
            @ApiResponse(responseCode = "404", description = "Calendar not found")
    })
    @PutMapping("/user/{userId}")
    public ResponseEntity<String> updateCalendar(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateCalendarRequest updateCalendarRequest
            ) {
        calendarService.updateCalendar(userId, updateCalendarRequest.timezone());
        return ResponseEntity.ok("Calendar updated successfully");
    }
}
