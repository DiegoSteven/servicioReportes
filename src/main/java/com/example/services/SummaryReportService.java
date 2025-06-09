package com.example.services;

import com.example.Util.ReportUtils;
import com.example.models.ModelosBases.Device;
import com.example.models.ModelosBases.Position;
import com.example.models.SummaryReportItem;
import com.example.repositories.DeviceRepository;
import com.example.repositories.PositionRepository;
import org.jxls.util.JxlsHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    public Collection<SummaryReportItem> getObjects(Long userId, Collection<Long> deviceIds,
                                                    Collection<Long> groupIds, Date from, Date to,
                                                    boolean daily) {

        reportUtils.checkPeriodLimit(from, to);
        ZoneId timezone = reportUtils.getUserTimezone(userId);


        List<SummaryReportItem> result = new ArrayList<>();
        List<Device> devices = deviceRepository.findAccessibleByUserId(userId, deviceIds, groupIds);

        for (Device device : devices) {
            ZonedDateTime fromZdt = from.toInstant().atZone(timezone);
            ZonedDateTime toZdt = to.toInstant().atZone(timezone);
            result.addAll(calculateDeviceResults(device, fromZdt, toZdt, daily));
        }

        return result.stream()
                .filter(r -> r.getStartTime() != null && r.getEndTime() != null)
                .collect(Collectors.toList());
    }

    private List<SummaryReportItem> calculateDeviceResults(Device device, ZonedDateTime from,
                                                           ZonedDateTime to, boolean daily) {

        List<SummaryReportItem> results = new ArrayList<>();
        boolean fast = Duration.between(from, to).toSeconds() > 86400;

        if (daily) {
            while (from.truncatedTo(ChronoUnit.DAYS).isBefore(to.truncatedTo(ChronoUnit.DAYS))) {
                ZonedDateTime fromDay = from.truncatedTo(ChronoUnit.DAYS);
                ZonedDateTime nextDay = fromDay.plusDays(1);
                results.addAll(calculateDeviceResult(device,
                        Date.from(fromDay.toInstant()), Date.from(nextDay.toInstant()), fast));
                from = nextDay;
            }
        }

        results.addAll(calculateDeviceResult(device, Date.from(from.toInstant()), Date.from(to.toInstant()), fast));
        return results;
    }

    private List<SummaryReportItem> calculateDeviceResult(Device device, Date from, Date to, boolean fast) {

        List<Position> positions = positionRepository.findByDeviceIdAndFixTimeBetween(device.getId(), from, to);
        if (positions.isEmpty()) return List.of();

        Position first = positions.get(0);
        Position last = positions.get(positions.size() - 1);

        SummaryReportItem summary = new SummaryReportItem();
        summary.setDeviceId(device.getId());
        summary.setDeviceName(device.getName());
        summary.setStartTime(first.getFixTime());
        summary.setEndTime(last.getFixTime());
        summary.setMaxSpeed(positions.stream().mapToDouble(Position::getSpeed).max().orElse(0));

        boolean ignoreOdometer = false;
        summary.setDistance(reportUtils.calculateDistance(first, last, !ignoreOdometer));
        summary.setSpentFuel(reportUtils.calculateFuel(first, last));

        if (first.hasAttribute("hours") && last.hasAttribute("hours")) {
            summary.setStartHours(first.getLong("hours"));
            summary.setEndHours(last.getLong("hours"));
            if (summary.getEngineHours() > 0) {
                summary.setAverageSpeed(reportUtils.calculateAverageSpeed(
                        summary.getDistance(), summary.getEngineHours()));
            }
        }

        summary.setStartOdometer(first.getTotalDistance());
        summary.setEndOdometer(last.getTotalDistance());

        return List.of(summary);
    }

    public void exportToExcel(OutputStream outputStream, Long userId,
                               List<Long> deviceIds, List<Long> groupIds,
                               Date from, Date to, boolean daily) throws IOException {

        Collection<SummaryReportItem> summaries = getObjects(userId, deviceIds, groupIds, from, to, daily);
        InputStream template = new ClassPathResource("templates/export/summary.xlsx").getInputStream();

        var context = reportUtils.initializeContext(userId);
        context.putVar("summaries", summaries);
        context.putVar("from", from);
        context.putVar("to", to);

        JxlsHelper.getInstance().setUseFastFormulaProcessor(false)
                .processTemplate(template, outputStream, context);
    }
}
