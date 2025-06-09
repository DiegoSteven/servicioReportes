package com.example.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.apache.poi.ss.util.WorkbookUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.Util.GeocoderUtil;
import com.example.Util.ReportTemplateUtils;
import com.example.Util.ReportUtils;
import com.example.models.DeviceReportSection;
import com.example.models.ModelosBases.*;
import com.example.repositories.*;

@Service
public class EventsReportService {

    private final DeviceRepository deviceRepository;
    private final EventRepository eventRepository;
    private final PositionRepository positionRepository;
    private final ReportUtils reportUtils;
    private final GroupRepository groupRepository;
    private final GeocoderUtil geocoderUtil;
    public EventsReportService(
            DeviceRepository deviceRepository,
            EventRepository eventRepository,
            PositionRepository positionRepository,
            ReportUtils reportUtils,
            GroupRepository groupRepository,
            GeocoderUtil geocoderUtil) {
        this.deviceRepository = deviceRepository;
        this.eventRepository = eventRepository;
        this.positionRepository = positionRepository;
        this.reportUtils = reportUtils;
        this.groupRepository = groupRepository;
        this.geocoderUtil = geocoderUtil;
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
        boolean all = types.isEmpty() || types.contains("allEvents");

        for (Device device : devices) {
            List<Event> events = eventRepository.findByDeviceIdAndEventTimeBetweenOrderByEventTime(device.getId(), from,
                    to);
            for (Event event : events) {
                boolean matchType = all || types.contains(event.getType());
                boolean matchAlarm = !"alarm".equals(event.getType()) || alarms.isEmpty()
                        || alarms.contains(String.valueOf(event.getAttributes().get("alarm")));

                if (matchType && matchAlarm) {
                    result.add(event);
                }
            }
        }

        return result;
    }

    public void exportToExcel(
            OutputStream outputStream,
            Long userId,
            List<Long> deviceIds,
            List<Long> groupIds,
            List<String> types,
            List<String> alarms,
            Date from,
            Date to) throws IOException {

        reportUtils.checkPeriodLimit(from, to);

        List<DeviceReportSection> devicesEvents = new ArrayList<>();
        List<String> sheetNames = new ArrayList<>();
        Map<String, String> geofenceNames = new HashMap<>();
        Map<String, String> maintenanceNames = new HashMap<>();

        // Cambiar tipo de clave a Object para aceptar Integer y Long
        Map<Object, Position> positionsMap = new HashMap<>();

        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);
        for (Device device : devices) {
            List<Event> events = eventRepository.findByDeviceIdAndEventTimeBetweenOrderByEventTime(device.getId(), from,
                    to);
            boolean all = types.isEmpty() || types.contains("allEvents");

            Iterator<Event> iterator = events.iterator();
            while (iterator.hasNext()) {
                Event event = iterator.next();
                boolean matchType = all || types.contains(event.getType());
                boolean matchAlarm = !"alarm".equals(event.getType()) || alarms.isEmpty()
                        || alarms.contains(String.valueOf(event.getAttributes().get("alarm")));

                if (!matchType || !matchAlarm) {
                    iterator.remove();
                    continue;
                }

                Integer posId = event.getPositionId();
                if (posId != null && posId > 0 && !positionsMap.containsKey(posId)) {
                    Position pos = positionRepository.findById(posId.longValue()).orElse(null);
                    if (pos != null) {
                        if (pos.getAddress() == null) {
                            String fetchedAddress = geocoderUtil.geocodePosition(pos.getLatitude(), pos.getLongitude());
                            pos.setAddress(fetchedAddress);
                        }
                        System.out.println("\n🔹 Posiciones agregadas:");

                        System.out.println("Agregando posición con ID: " + posId + " y address: " + pos.getAddress());

                        positionsMap.put(posId, pos); // ¡clave como Integer!
                    }
                }
            }

            DeviceReportSection section = new DeviceReportSection();
            section.setDeviceName(device.getName());
            section.setObjects(events);

            if (device.getGroupId() != null && device.getGroupId() > 0) {
                groupRepository.findById(device.getGroupId()).ifPresent(g -> section.setGroupName(g.getName()));
            }

            String name = WorkbookUtil.createSafeSheetName(device.getName());
            sheetNames.add(name);
            devicesEvents.add(section);
        }
        

        InputStream template = new ClassPathResource("templates/export/events.xlsx").getInputStream();

        var context = reportUtils.initializeContext(userId);
        context.putVar("util", new ReportTemplateUtils());
        context.putVar("devices", devicesEvents);
        context.putVar("sheetNames", sheetNames);
        context.putVar("positions", positionsMap); // ¡Ahora con claves compatibles!
        context.putVar("from", from);
        context.putVar("to", to);
        context.putVar("geofenceNames", geofenceNames);
        context.putVar("maintenanceNames", maintenanceNames);
        
        System.out.println("\n🔹  Información completa de cada posición para seguimiento");
        for (Map.Entry<Object, Position> entry : positionsMap.entrySet()) {
            Position pos = entry.getValue();
            double lat = pos.getLatitude();
            double lon = pos.getLongitude();
            String address = pos.getAddress();

            String url = String.format(Locale.US,
                    "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=16/%.6f/%.6f",
                    lat, lon, lat, lon);

            String fallbackAddress = address != null ? address
                    : String.format(Locale.US, "%.6f°, %.6f°", lat, lon);

            String hyperlink = new ReportTemplateUtils().hyperlink(url, fallbackAddress);

            System.out.println("LAT: " + lat + " LON: " + lon);
            System.out.println("ADDRESS: " + address);
            System.out.println("URL: " + url);
            System.out.println("TEXT: " + fallbackAddress);
            System.out.println("HYPERLINK: " + hyperlink);
        }

        reportUtils.processTemplateWithSheets(template, outputStream, context);
    }
}