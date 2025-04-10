package com.example.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.DeviceReportSection;
import com.example.models.ModelosBases.Device;
import com.example.models.ModelosBases.Group;
import com.example.models.ModelosBases.Position;
import com.example.repositories.DeviceRepository;
import com.example.repositories.GroupRepository;
import com.example.repositories.PositionRepository;

@Service
public class RouteReportService {

    private final ReportUtils reportUtils;
    private final DeviceRepository deviceRepository;
    private final PositionRepository positionRepository;
    private final GroupRepository groupRepository;

    public RouteReportService(
            ReportUtils reportUtils,
            DeviceRepository deviceRepository,
            PositionRepository positionRepository,
            GroupRepository groupRepository) {
        this.reportUtils = reportUtils;
        this.deviceRepository = deviceRepository;
        this.positionRepository = positionRepository;
        this.groupRepository = groupRepository;
    }

    public Collection<Position> getObjects(
            Long userId,
            List<Long> deviceIds,
            List<Long> groupIds,
            Date from,
            Date to) {

        reportUtils.checkPeriodLimit(from, to);

        List<Position> result = new ArrayList<>();
        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);

        for (Device device : devices) {
            List<Position> positions = positionRepository.findByDeviceIdAndFixTimeBetween(device.getId(), from, to);
            result.addAll(positions);
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

        reportUtils.checkPeriodLimit(from, to);

        List<DeviceReportSection> devicesRoutes = new ArrayList<>();
        List<String> sheetNames = new ArrayList<>();
        Map<String, Integer> nameCount = new HashMap<>();

        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);
        for (Device device : devices) {
            List<Position> positions = positionRepository.findByDeviceIdAndFixTimeBetween(device.getId(), from, to);

            DeviceReportSection section = new DeviceReportSection();
            section.setDeviceName(device.getName());
            section.setObjects(positions);

            if (device.getGroupId() != null && device.getGroupId() > 0) {
                Optional<Group> group = groupRepository.findById(device.getGroupId());
                group.ifPresent(g -> section.setGroupName(g.getName()));
            }

            String name = WorkbookUtil.createSafeSheetName(device.getName());
            nameCount.put(name, nameCount.getOrDefault(name, 0) + 1);
            if (nameCount.get(name) > 1)
                name += "-" + nameCount.get(name);
            sheetNames.add(name);

            devicesRoutes.add(section);
        }

        InputStream template;
        try {
            template = new ClassPathResource("templates/export/route.xlsx").getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("No se encontró la plantilla route.xlsx en resources", e);
        }

        var context = reportUtils.initializeContext(userId); 
      
        System.out.println("FROM: " + from + " -> " + from.getClass());
        System.out.println("TO: " + to + " -> " + to.getClass());
        

        context.putVar("devices", devicesRoutes);
        context.putVar("sheetNames", sheetNames);
        context.putVar("from", from);
        context.putVar("to", to);

        reportUtils.processTemplateWithSheets(template, outputStream, context);
    }
}
