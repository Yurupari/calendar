package com.yurupari.calendar.repository;

import com.yurupari.calendar.model.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    Optional<Calendar> findByUserId(Long userId);

    List<Calendar> findByUserIdIn(Set<Long> userIds);
}
