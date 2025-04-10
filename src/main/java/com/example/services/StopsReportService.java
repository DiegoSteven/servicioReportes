package com.example.services;

import java.util.*;

import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.StopReportItem;
import com.example.models.ModelosBases.Device;
import com.example.repositories.DeviceRepository;

@Service
public class StopsReportService {

    private final DeviceRepository deviceRepository;
    private final ReportUtils reportUtils;

    public StopsReportService(
            DeviceRepository deviceRepository,
            ReportUtils reportUtils) {
        this.deviceRepository = deviceRepository;
        this.reportUtils = reportUtils;
    }

    public Collection<StopReportItem> getObjects(
            Long userId,
            Collection<Long> deviceIds,
            Collection<Long> groupIds,
            Date from,
            Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<StopReportItem> result = new ArrayList<>();
        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);

        for (Device device : devices) {
            List<StopReportItem> stops = reportUtils.detectTripsAndStops(device, from, to, StopReportItem.class);
            result.addAll(stops);
        }

        return result;
    }
}