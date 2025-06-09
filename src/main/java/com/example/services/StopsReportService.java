package com.example.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.DeviceReportSection;
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

    public void exportToExcel(
        OutputStream outputStream,
        Long userId,
        List<Long> deviceIds,
        List<Long> groupIds,
        Date from,
        Date to) throws IOException {

    List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);
    List<DeviceReportSection> sections = new ArrayList<>();

    for (Device device : devices) {
        List<StopReportItem> stops = reportUtils.detectTripsAndStops(device, from, to, StopReportItem.class);

        DeviceReportSection section = new DeviceReportSection();
        section.setDeviceName(device.getName());
        section.setObjects(stops);

        if (device.getGroupId() != null && device.getGroupId() > 0) {
            section.setGroupName("Grupo " + device.getGroupId()); // o búscalo si ya tienes GroupRepository
        }

        sections.add(section);
    }

    InputStream template = new ClassPathResource("templates/export/stops.xlsx").getInputStream();

    var context = reportUtils.initializeContext(userId);
    List<String> sheetNames = sections.stream()
            .map(DeviceReportSection::getDeviceName)
            .map(name -> name.replaceAll("[\\\\/:*?\"<>|]", "_"))
            .toList();

    context.putVar("devices", sections);
    context.putVar("sheetNames", sheetNames);
    context.putVar("from", from);
    context.putVar("to", to);

    JxlsHelper.getInstance().setUseFastFormulaProcessor(false)
            .processTemplate(template, outputStream, context);
}

}