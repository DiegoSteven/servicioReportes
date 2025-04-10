package com.example.services;

import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.CombinedReportItem;
import com.example.models.ModelosBases.Device;
import com.example.models.ModelosBases.Event;
import com.example.models.ModelosBases.Position;
import com.example.repositories.DeviceRepository;
import com.example.repositories.EventRepository;
import com.example.repositories.PositionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CombinedReportService {

    private static final Set<String> EXCLUDE_TYPES = Set.of("deviceMoving");

    private final ReportUtils reportUtils;
    private final DeviceRepository deviceRepository;
    private final PositionRepository positionRepository;
    private final EventRepository eventRepository;

    public CombinedReportService(
            ReportUtils reportUtils,
            DeviceRepository deviceRepository,
            PositionRepository positionRepository,
            EventRepository eventRepository) {
        this.reportUtils = reportUtils;
        this.deviceRepository = deviceRepository;
        this.positionRepository = positionRepository;
        this.eventRepository = eventRepository;
    }

    public Collection<CombinedReportItem> getReport(
            Long userId,
            Collection<Long> deviceIds,
            Collection<Long> groupIds,
            Date from,
            Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<CombinedReportItem> result = new ArrayList<>();

        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);


        for (Device device : devices) {
            CombinedReportItem item = new CombinedReportItem();
            item.setDeviceId(device.getId());

            List<Position> positions = positionRepository.findByDeviceIdAndFixTimeBetween(
                    device.getId(), from, to);

            item.setRoute(positions.stream()
                    .map(p -> new double[] { p.getLongitude(), p.getLatitude() })
                    .collect(Collectors.toList()));

            List<Event> events = eventRepository.findByDeviceIdAndEventTimeBetweenOrderByEventTime(
                    device.getId(), from, to);

            List<Event> filteredEvents = events.stream()
                    .filter(e -> e.getPositionId() != null && e.getPositionId() > 0)
                    .filter(e -> !EXCLUDE_TYPES.contains(e.getType()))
                    .collect(Collectors.toList());

            item.setEvents(filteredEvents);

            Set<Long> eventPositionIds = filteredEvents.stream()
                    .map(e -> e.getPositionId().longValue())
                    .collect(Collectors.toSet());

            item.setPositions(positionRepository.findByIds(eventPositionIds));

            result.add(item);
        }

        return result;
    }
}