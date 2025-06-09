package com.example.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.jxls.area.Area;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.formula.StandardFormulaProcessor;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.TransformerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.jxls.common.Context;

import com.example.helper.AttributeUtil;
import com.example.models.BaseReportItem;
import com.example.models.StopReportItem;
import com.example.models.TripReportItem;
import com.example.models.ModelosBases.Device;
import com.example.models.ModelosBases.Event;
import com.example.models.ModelosBases.Position;
import com.example.models.motion.MotionProcessor;
import com.example.models.motion.MotionState;
import com.example.repositories.EventRepository;
import com.example.repositories.PositionRepository;
import com.example.repositories.UserRepository;

@Component
public class ReportUtils {

    @Value("${report.period.limit.seconds:86400}")
    private long reportPeriodLimitSeconds;

    @Value("${report.fastThreshold:86400}")
    private long reportFastThreshold;

    private final PositionRepository positionRepository;
    private final VelocityEngine velocityEngine;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ReportUtils(PositionRepository positionRepository, UserRepository userRepository,
            EventRepository eventRepository) {
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.velocityEngine = new VelocityEngine();
        this.velocityEngine.init();
    }

    public void checkPeriodLimit(Date from, Date to) {
        long limit = reportPeriodLimitSeconds * 1000;
        if (limit > 0 && (to.getTime() - from.getTime()) > limit) {
            throw new IllegalArgumentException("Time period exceeds the limit");
        }
    }

    public ZoneId getUserTimezone(Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    Object tz = user.getAttributes().get("timezone");
                    return tz != null ? ZoneId.of(tz.toString()) : ZoneId.systemDefault();
                })
                .orElse(ZoneId.systemDefault());
    }

    public double calculateFuel(Position first, Position last) {
        Double fStart = first.getAttributes().containsKey("fuelUsed") ? (Double) first.getAttributes().get("fuelUsed")
                : null;
        Double fEnd = last.getAttributes().containsKey("fuelUsed") ? (Double) last.getAttributes().get("fuelUsed")
                : null;

        if (fStart != null && fEnd != null) {
            return fEnd - fStart;
        }
        return 0;
    }

    public double calculateDistance(Position first, Position last, boolean useOdometer) {
        if (useOdometer) {
            Double odoStart = first.getAttributes().containsKey("odometer")
                    ? (Double) first.getAttributes().get("odometer")
                    : null;
            Double odoEnd = last.getAttributes().containsKey("odometer") ? (Double) last.getAttributes().get("odometer")
                    : null;

            if (odoStart != null && odoEnd != null) {
                return odoEnd - odoStart;
            }
        }
        // fallback a totalDistance
        return last.getTotalDistance() - first.getTotalDistance();
    }

    public double calculateAverageSpeed(double distanceKm, long durationMillis) {
        double durationHours = durationMillis / 3600000.0;
        return durationHours > 0 ? distanceKm / durationHours : 0;
    }

    public double kmhToKnots(double kmh) {
        return kmh * 0.539957;
    }

    public Context initializeContext(long userId) {
        Context context = PoiTransformer.createInitialContext();
        context.putVar("distanceUnit", "km");
        context.putVar("speedUnit", "km/h");
        context.putVar("volumeUnit", "L");
        context.putVar("webUrl", "http://localhost:8081");


        context.putVar("dateTool", new DateTool()); // ✅ esta línea es clave
        context.putVar("locale", Locale.US);
        context.putVar("timezone", TimeZone.getDefault());        context.putVar("numberTool", new Object()); // puede dejarse así si no se usa
        context.putVar("bracketsRegex", "[\\{\\}\"]");
        context.putVar("util", new ReportTemplateUtils());
        return context;
    }

    public void processTemplateWithSheets(InputStream templateStream, OutputStream outputStream, Context context)
            throws IOException {
        var transformer = TransformerFactory.createTransformer(templateStream, outputStream);
        List<Area> xlsAreas = new XlsCommentAreaBuilder(transformer).build();
        for (Area area : xlsAreas) {
            area.applyAt(new CellRef(area.getStartCellRef().getCellName()), context);
            area.setFormulaProcessor(new StandardFormulaProcessor());
            area.processFormulas();
        }
        transformer.deleteSheet(xlsAreas.get(0).getStartCellRef().getSheetName());
        transformer.write();
    }

    private boolean isMoving(List<Position> positions, int index, TripsConfig config) {
        if (config.getMinimalNoDataDuration() > 0) {
            boolean beforeGap = index < positions.size() - 1
                    && positions.get(index + 1).getFixTime().getTime()
                            - positions.get(index).getFixTime().getTime() >= config.getMinimalNoDataDuration();
            boolean afterGap = index > 0
                    && positions.get(index).getFixTime().getTime()
                            - positions.get(index - 1).getFixTime().getTime() >= config.getMinimalNoDataDuration();
            if (beforeGap || afterGap) {
                return false;
            }
        }
        return positions.get(index).getAttributes().getOrDefault("motion", false).equals(true);
    }

    public <T extends BaseReportItem> List<T> detectTripsAndStops(Device device, Date from, Date to, Class<T> clazz) {
        return slowTripsAndStops(device, from, to, clazz);
    }
    
    

    @SuppressWarnings("unchecked")
    private <T extends BaseReportItem> T calculateTripOrStop(Device device, Position start, Position end,
            double maxSpeed, boolean ignoreOdometer, Class<T> clazz) {
        if (clazz.equals(TripReportItem.class)) {
            return (T) calculateTrip(device, start, end, maxSpeed, ignoreOdometer);
        } else {
            return (T) calculateStop(device, start, end, ignoreOdometer);
        }
    }

    private TripReportItem calculateTrip(Device device, Position start, Position end, double maxSpeed,
            boolean ignoreOdometer) {
        TripReportItem trip = new TripReportItem();

        trip.setDeviceId(device.getId());
        trip.setDeviceName(device.getName());

        trip.setStartPositionId(start.getId());
        trip.setStartLat(start.getLatitude());
        trip.setStartLon(start.getLongitude());
        trip.setStartTime(start.getFixTime());
        trip.setStartAddress(start.getAddress());

        trip.setEndPositionId(end.getId());
        trip.setEndLat(end.getLatitude());
        trip.setEndLon(end.getLongitude());
        trip.setEndTime(end.getFixTime());
        trip.setEndAddress(end.getAddress());

        trip.setDuration(end.getFixTime().getTime() - start.getFixTime().getTime());
        trip.setMaxSpeed(maxSpeed);
        trip.setDistance(calculateDistance(start, end, !ignoreOdometer));
        trip.setSpentFuel(calculateFuel(start, end));

        if (!ignoreOdometer
                && start.getAttributes().get("odometer") != null
                && end.getAttributes().get("odometer") != null) {
            trip.setStartOdometer((Double) start.getAttributes().get("odometer"));
            trip.setEndOdometer((Double) end.getAttributes().get("odometer"));
        } else {
            trip.setStartOdometer(start.getTotalDistance());
            trip.setEndOdometer(end.getTotalDistance());
        }

        trip.setAverageSpeed(kmhToKnots(calculateAverageSpeed(trip.getDistance(), trip.getDuration())) / 1000.0);

        trip.setDriverUniqueId((String) start.getAttributes().getOrDefault("driverUniqueId", ""));
        trip.setDriverName(""); // puedes implementar búsqueda por ID si lo deseas

        return trip;
    }

    private StopReportItem calculateStop(Device device, Position start, Position end, boolean ignoreOdometer) {
        StopReportItem stop = new StopReportItem();

        stop.setDeviceId(device.getId());
        stop.setDeviceName(device.getName());

        stop.setPositionId(start.getId());
        stop.setLatitude(start.getLatitude());
        stop.setLongitude(start.getLongitude());
        stop.setStartTime(start.getFixTime());
        stop.setEndTime(end.getFixTime());
        stop.setAddress(start.getAddress());

        stop.setDuration(end.getFixTime().getTime() - start.getFixTime().getTime());
        stop.setSpentFuel(calculateFuel(start, end));

        if (!ignoreOdometer
                && start.getAttributes().get("odometer") != null
                && end.getAttributes().get("odometer") != null) {
            stop.setStartOdometer((Double) start.getAttributes().get("odometer"));
            stop.setEndOdometer((Double) end.getAttributes().get("odometer"));
        } else {
            stop.setStartOdometer(start.getTotalDistance());
            stop.setEndOdometer(end.getTotalDistance());
        }

        return stop;
    }

    public List<Position> getPositions(Device device, Date from, Date to) {
        return positionRepository.findByDeviceIdAndFixTimeBetween(device.getId(), from, to);
    }

    public <T extends BaseReportItem> List<T> slowTripsAndStops(Device device, Date from, Date to, Class<T> reportClass) {
        List<T> result = new ArrayList<>();
        List<Position> positions = getPositions(device, from, to);
        TripsConfig config = new TripsConfig(new AttributeUtil.DeviceProvider(device));
    
        boolean trips = reportClass.equals(TripReportItem.class);
        boolean ignoreOdometer = config.getIgnoreOdometer();
    
        if (!positions.isEmpty()) {
            MotionState motionState = new MotionState();
            boolean currentMotion = isMoving(positions, 0, config);
            motionState.setMotionStreak(currentMotion);
            motionState.setMotionState(currentMotion);
    
            boolean detected = trips == motionState.getMotionState();
            double maxSpeed = 0;
            int startEventIndex = detected ? 0 : -1;
            int startNoEventIndex = -1;
    
            for (int i = 0; i < positions.size(); i++) {
                Position pos = positions.get(i);
                boolean motion = isMoving(positions, i, config);
    
                if (motionState.getMotionState() != motion) {
                    if (motion == trips) {
                        if (!detected) {
                            startEventIndex = i;
                            maxSpeed = pos.getSpeed();
                        }
                        startNoEventIndex = -1;
                    } else {
                        startNoEventIndex = i;
                    }
                } else {
                    maxSpeed = Math.max(maxSpeed, pos.getSpeed());
                }
    
                MotionProcessor.updateState(motionState, pos, motion, config);
    
                if (motionState.getEvent() != null) {
                    if (motion == trips) {
                        detected = true;
                        startNoEventIndex = -1;
                    } else if (startEventIndex >= 0 && startNoEventIndex >= 0) {
                        Position start = positions.get(startEventIndex);
                        Position end = positions.get(startNoEventIndex);
                        result.add(calculateTripOrStop(device, start, end, maxSpeed, ignoreOdometer, reportClass));
    
                        detected = false;
                        startEventIndex = -1;
                        startNoEventIndex = -1;
                        maxSpeed = 0;
                    }
                }
            }
    
            // cierre de trayecto si no se cerró
            if (detected && startEventIndex >= 0 && startEventIndex < positions.size() - 1) {
                int endIndex = startNoEventIndex >= 0 ? startNoEventIndex : positions.size() - 1;
                Position start = positions.get(startEventIndex);
                Position end = positions.get(endIndex);
                result.add(calculateTripOrStop(device, start, end, maxSpeed, ignoreOdometer, reportClass));
            }
        }
    
        return result;
    }
    

    public <T extends BaseReportItem> List<T> fastTripsAndStops(Device device, Date from, Date to,
            Class<T> reportClass) {
        List<T> result = new ArrayList<>();
        TripsConfig config = new TripsConfig(new AttributeUtil.DeviceProvider(device));
        boolean ignoreOdometer = config.getIgnoreOdometer();
        boolean trips = reportClass.equals(TripReportItem.class);

        List<Event> events = eventRepository.findByDeviceIdAndEventTimeBetweenOrderByEventTime(device.getId(), from,
                to);

        MotionState state = new MotionState();
        Position start = null;
        double maxSpeed = 0;

        for (Event event : events) {
            Position position = positionRepository.findById(event.getPositionId().longValue()).orElse(null);
            if (position == null)
                continue;

            boolean motion = "deviceMoving".equals(event.getType());

            // iniciar nuevo trayecto
            if (start == null && motion == trips) {
                state.setMotionState(motion);
                state.setMotionStreak(motion);
                state.setMotionTime(position.getFixTime());
                state.setMotionDistance(position.getTotalDistance());
                start = position;
                maxSpeed = position.getSpeed();
                continue;
            }

            if (start != null) {
                maxSpeed = Math.max(maxSpeed, position.getSpeed());
            }

            MotionProcessor.updateState(state, position, motion, config);

            // terminar trayecto
            if (state.getEvent() != null && (!motion || !"deviceMoving".equals(state.getEvent()))) {

                result.add(calculateTripOrStop(device, start, position, maxSpeed, ignoreOdometer, reportClass));
                state.setMotionTime(null);
                state.setMotionDistance(0);

            }
        }

        // 🛑 Si el viaje quedó abierto al final, ciérralo con el último evento
        if (start != null && !events.isEmpty()) {
            Event lastEvent = events.get(events.size() - 1);
            Position end = positionRepository.findById(lastEvent.getPositionId().longValue()).orElse(null);
            if (end != null && !end.getFixTime().equals(start.getFixTime())) {
                result.add(calculateTripOrStop(device, start, end, maxSpeed, ignoreOdometer, reportClass));
            }
        }

        return result;
    }

}
