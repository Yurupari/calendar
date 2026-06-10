package com.yurupari.calendar.service;

import com.yurupari.calendar.model.request.CreateMeetingRequest;
import com.yurupari.calendar.model.request.UpdateMeetingRequest;
import com.yurupari.calendar.model.response.MeetingResponse;
import jakarta.validation.Valid;

public interface MeetingService {
    MeetingResponse createMeeting(@Valid CreateMeetingRequest meetingDto);

    MeetingResponse getMeetingById(Long id);

    void updateMeeting(Long id, UpdateMeetingRequest updateMeetingRequest);
}
