package com.yurupari.calendar.repository;

import com.yurupari.calendar.model.entity.Slot;
import com.yurupari.calendar.model.enums.ParticipantRole;
import com.yurupari.calendar.model.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    boolean existsByCalendarIdAndStartTimeAndEndTime(Long calendarId, Instant startTime, Instant endTime);

    Optional<Slot> findByCalendarIdAndStartTimeAndEndTime(
            Long calendarId,
            Instant startTime,
            Instant endTime);

    @Query("""
            SELECT s FROM Slot s
            WHERE s.calendar.id = :calendarId
            AND s.startTime >= :startTime
            AND s.startTime < :endTime
            AND (CAST(:status AS string) IS NULL OR s.status = :status)
            """)
    List<Slot> findSlotsWithOptionalStatus(
            Long calendarId,
            Instant startTime,
            Instant endTime,
            SlotStatus status);

    @Query("""
            SELECT s FROM Slot s
            WHERE s.calendar.id IN :calendarIds
            AND s.startTime >= :startTime
            AND s.startTime < :endTime
            AND (CAST(:status AS string) IS NULL OR s.status = :status)
            """)
    List<Slot> findSlotsWithOptionalStatusInCalendars(
            Set<Long> calendarIds,
            Instant startTime,
            Instant endTime,
            SlotStatus status);

    List<Slot> findByMeetingId(Long meetingId);
}
