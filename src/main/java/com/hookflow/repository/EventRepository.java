package com.hookflow.repository;

import com.hookflow.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByUser_Id(Long userId);

    Optional<Event> findByIdAndUser_Id(Long eventId, Long userId);

    long countByUser_Id(Long userId);
}