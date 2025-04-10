package com.example.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.models.ModelosBases.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByDeviceIdAndEventTimeBetweenOrderByEventTime(Long deviceId, Date from, Date to);
   
}