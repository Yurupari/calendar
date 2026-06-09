package com.yurupari.calendar.repository;

import com.yurupari.calendar.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        JOIN Calendar c ON u.id = c.user.id
        JOIN Slot s ON c.id = s.calendar.id
        WHERE s.meeting.id = :meetingId
    """)
    List<User> findParticipantsByMeetingId(@Param("meetingId") Long meetingId);
}
