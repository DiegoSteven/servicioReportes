package com.example.services;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.stereotype.Service;

import com.example.Util.ReportUtils;
import com.example.models.SummaryReportItem;
import com.example.models.ModelosBases.Device;
import com.example.models.ModelosBases.Position;
import com.example.repositories.DeviceRepository;
import com.example.repositories.PositionRepository;

@Service
public class SummaryReportService {

    private final DeviceRepository deviceRepository;
    private final PositionRepository positionRepository;
    private final ReportUtils reportUtils;

    public SummaryReportService(
            DeviceRepository deviceRepository,
            PositionRepository positionRepository,
            ReportUtils reportUtils) {
        this.deviceRepository = deviceRepository;
        this.positionRepository = positionRepository;
        this.reportUtils = reportUtils;
    }

    public Collection<SummaryReportItem> getObjects(
            Long userId,
            Collection<Long> deviceIds,
            Collection<Long> groupIds,
            Date from,
            Date to,
            boolean daily) {

        reportUtils.checkPeriodLimit(from, to);

        ZoneId timezone = ZoneId.systemDefault(); // Por ahora, fijo. Luego podemos obtenerlo por usuario.

        List<SummaryReportItem> result = new ArrayList<>();
        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);

        for (Device device : devices) {
            List<SummaryReportItem> deviceResults = calculateDeviceResults(
                    device,
                    from.toInstant().atZone(timezone),
                    to.toInstant().atZone(timezone),
                    daily
            );
            for (SummaryReportItem summary : deviceResults) {
                if (summary.getStartTime() != null && summary.getEndTime() != null) {
                    result.add(summary);
                }
            }
        }

        return result;
    }

    private List<SummaryReportItem> calculateDeviceResults(
            Device device,
            ZonedDateTime from,
            ZonedDateTime to,
            boolean daily) {

        List<SummaryReportItem> results = new ArrayList<>();
        boolean fast = Duration.between(from, to).toSeconds() > 86400;

        if (daily) {
            while (from.truncatedTo(ChronoUnit.DAYS).isBefore(to.truncatedTo(ChronoUnit.DAYS))) {
                ZonedDateTime fromDay = from.truncatedTo(ChronoUnit.DAYS);
                ZonedDateTime nextDay = fromDay.plusDays(1);
                results.addAll(calculateDeviceResult(device, Date.from(fromDay.toInstant()), Date.from(nextDay.toInstant()), fast));
                from = nextDay;
            }
        }

        results.addAll(calculateDeviceResult(device, Date.from(from.toInstant()), Date.from(to.toInstant()), fast));
        return results;
    }

    private List<SummaryReportItem> calculateDeviceResult(Device device, Date from, Date to, boolean fast) {

        SummaryReportItem summary = new SummaryReportItem();
        summary.setDeviceId(device.getId());
        summary.setDeviceName(device.getName());

        //Position first = positionRepository.findFirstByDeviceIdAndFixTimeBetweenOrderByFixTimeAsc(device.getId(), from, to);
        //Position last = positionRepository.findFirstByDeviceIdAndFixTimeBetweenOrderByFixTimeDesc(device.getId(), from, to);

        //if (first != null && last != null) {
            double distance = 0;
            double maxSpeed = 0;

            List<Position> positions = positionRepository.findByDeviceIdAndFixTimeBetween(device.getId(), from, to);
            for (Position pos : positions) {
                if (pos.getSpeed() > maxSpeed) {
                    maxSpeed = pos.getSpeed();
                }
            }

           // distance = last.getTotalDistance() - first.getTotalDistance();

            //summary.setStartTime(first.getFixTime());
           // summary.setEndTime(last.getFixTime());
            summary.setDistance(distance);
            summary.setMaxSpeed(maxSpeed);
            //summary.setStartOdometer(first.getTotalDistance());
            //summary.setEndOdometer(last.getTotalDistance());
            summary.setStartHours(0); // Asignar si manejás KEY_HOURS
            summary.setEndHours(0);
            summary.setSpentFuel(0); // Asignar si manejás KEY_FUEL_USED
            summary.setAverageSpeed(0); // Se puede calcular si querés

            return List.of(summary);
        //}

        //return List.of();
    }
}
