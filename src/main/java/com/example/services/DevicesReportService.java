package com.example.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.DeviceReportItem;
import com.example.models.ModelosBases.*;
import com.example.repositories.DeviceRepository;
import com.example.repositories.PositionRepository;

@Service
public class DevicesReportService {

    private final DeviceRepository deviceRepository;
    private final PositionRepository positionRepository;
    private final ReportUtils reportUtils;

    public DevicesReportService(
            DeviceRepository deviceRepository,
            PositionRepository positionRepository,
            ReportUtils reportUtils) {
        this.deviceRepository = deviceRepository;
        this.positionRepository = positionRepository;
        this.reportUtils = reportUtils;
    }

    public Collection<DeviceReportItem> getObjects(Long userId) {

        // Simula última posición por dispositivo (acá podrías filtrar si tuvieras timestamp)
        List<Position> latestPositions = positionRepository.findAll();

        // Agrupar por deviceId (y usar la última si hay varias)
        Map<Long, Position> positionsMap = latestPositions.stream()
                .collect(Collectors.toMap(Position::getDeviceId, p -> p, (a, b) -> b));

        List<Device> devices = deviceRepository.findAll(); // Podrías filtrar por userId si aplicás permisos

        return devices.stream()
                .map(device -> new DeviceReportItem(device, positionsMap.get(device.getId())))
                .collect(Collectors.toList());
    }
}