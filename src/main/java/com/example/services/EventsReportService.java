package com.example.services;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.ModelosBases.*;
import com.example.repositories.*;

@Service
public class EventsReportService {

    private final DeviceRepository deviceRepository;
    private final EventRepository eventRepository;
    private final PositionRepository positionRepository;
    private final ReportUtils reportUtils;

    public EventsReportService(
            DeviceRepository deviceRepository,
            EventRepository eventRepository,
            PositionRepository positionRepository,
            ReportUtils reportUtils) {
        this.deviceRepository = deviceRepository;
        this.eventRepository = eventRepository;
        this.positionRepository = positionRepository;
        this.reportUtils = reportUtils;
    }

    public Collection<Event> getObjects(
            Long userId,
            Collection<Long> deviceIds,
            Collection<Long> groupIds,
            Collection<String> types,
            Collection<String> alarms,
            Date from,
            Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<Event> result = new ArrayList<>();
        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);

        boolean all = types.isEmpty() || types.contains("all");

        for (Device device : devices) {
            List<Event> events = eventRepository.findByDeviceIdAndEventTimeBetweenOrderByEventTime(
        device.getId(), from, to);

            for (Event event : events) {
                boolean matchType = all || types.contains(event.getType());
                boolean matchAlarm = !event.getType().equals("alarm") || alarms.isEmpty() ||
                        alarms.contains(event.getAttributes().get("alarm")); // asume Map<String, Object>

                if (matchType && matchAlarm) {
                    result.add(event);
                }
            }
        }

        return result;
    }

    
}