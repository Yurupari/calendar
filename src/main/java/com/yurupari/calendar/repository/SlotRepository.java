package com.yurupari.calendar.repository;

import com.yurupari.calendar.model.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    Optional<Slot> findByCalendarIdAndStartTimeAndEndTime(
            Long calendarId,
            Instant startTime,
            Instant endTime);

    List<Slot> findByCalendarIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long calendarId,
            Instant startTime,
            Instant endTime);
}
