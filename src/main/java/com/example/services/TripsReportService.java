package com.example.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.DeviceReportSection;
import com.example.models.TripReportItem;
import com.example.models.ModelosBases.Device;
import com.example.repositories.DeviceRepository;
import com.example.repositories.GroupRepository;
import com.example.repositories.PositionRepository;

@Service
public class TripsReportService {

    private final DeviceRepository deviceRepository;
    private final PositionRepository positionRepository;
    private final GroupRepository groupRepository;
    private final ReportUtils reportUtils;

    public TripsReportService(
            DeviceRepository deviceRepository,
            PositionRepository positionRepository,
            GroupRepository groupRepository,
            ReportUtils reportUtils) {
        this.deviceRepository = deviceRepository;
        this.positionRepository = positionRepository;
        this.groupRepository = groupRepository;
        this.reportUtils = reportUtils;
    }

    public Collection<TripReportItem> getObjects(
            Long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);
        List<TripReportItem> result = new ArrayList<>();

        for (Device device : devices) {
            result.addAll(reportUtils.detectTripsAndStops(device, from, to, TripReportItem.class));
        }

        return result;
    }

    public List<DeviceReportSection> getGroupedByDevice(
            Long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);
        List<DeviceReportSection> sections = new ArrayList<>();

        for (Device device : devices) {
            List<TripReportItem> trips = reportUtils.detectTripsAndStops(device, from, to, TripReportItem.class);

            DeviceReportSection section = new DeviceReportSection();
            section.setDeviceName(device.getName());
            section.setObjects(trips);

            if (device.getGroupId() != null && device.getGroupId() > 0) {
                groupRepository.findById(device.getGroupId())
                        .ifPresent(group -> section.setGroupName(group.getName()));
            }

            sections.add(section);
        }

        return sections;
    }

    public void exportToExcel(
            OutputStream outputStream,
            Long userId,
            List<Long> deviceIds,
            List<Long> groupIds,
            Date from,
            Date to) throws IOException {

        List<DeviceReportSection> sections = getGroupedByDevice(userId, deviceIds, groupIds, from, to);

        InputStream template = new ClassPathResource("templates/export/trips.xlsx").getInputStream();

        var context = reportUtils.initializeContext(userId);
        List<String> sheetNames = sections.stream()
                .map(DeviceReportSection::getDeviceName)
                .map(name -> name.replaceAll("[\\\\/:*?\"<>|]", "_")) // sanitiza nombre de hoja
                .toList();

        context.putVar("devices", sections);
        context.putVar("sheetNames", sheetNames);
        context.putVar("from", from);
        context.putVar("to", to);

        JxlsHelper.getInstance().setUseFastFormulaProcessor(false)
                .processTemplate(template, outputStream, context);
    }
}
