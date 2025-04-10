package com.example.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.DeviceReportSection;
import com.example.models.TripReportItem;
import com.example.models.ModelosBases.Device;
import com.example.repositories.DeviceRepository;
import com.example.repositories.PositionRepository;

@Service
public class TripsReportService {

    private final DeviceRepository deviceRepository;
    private final PositionRepository positionRepository;
    private final ReportUtils reportUtils;

    public TripsReportService(
            DeviceRepository deviceRepository,
            PositionRepository positionRepository,
            ReportUtils reportUtils) {
        this.deviceRepository = deviceRepository;
        this.positionRepository = positionRepository;
        this.reportUtils = reportUtils;
    }

    public Collection<TripReportItem> getObjects(
            Long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<Device> devices = deviceRepository.findAll(); // podrías filtrar por usuario
        List<TripReportItem> result = new ArrayList<>();

        for (Device device : devices) {
            if (deviceIds == null || deviceIds.contains(device.getId())) {
                List<TripReportItem> trips = reportUtils.detectTripsAndStops(
                        device, from, to, TripReportItem.class);
                result.addAll(trips);
            }
        }

        return result;
    }

    public List<DeviceReportSection> getGroupedByDevice(
            Long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<Device> devices = deviceRepository.findAll(); // podrías filtrar por userId
        List<DeviceReportSection> sections = new ArrayList<>();

        for (Device device : devices) {
            if (deviceIds == null || deviceIds.contains(device.getId())) {
                List<TripReportItem> trips = reportUtils.detectTripsAndStops(
                        device, from, to, TripReportItem.class);

                DeviceReportSection section = new DeviceReportSection();
                section.setDeviceName(device.getName());
                section.setObjects(trips);
                if (device.getGroupId() != null) {
                    section.setGroupName("Grupo " + device.getGroupId()); // opcional: reemplazar por nombre real
                }

                sections.add(section);
            }
        }

        return sections;
    }
}