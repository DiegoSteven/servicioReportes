package com.example.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Util.ReportMailer;
import com.example.Util.SessionUtil;
import com.example.dtos.ReportCreationRequest;
import com.example.models.CombinedReportItem;
import com.example.models.StopReportItem;
import com.example.models.SummaryReportItem;
import com.example.models.TripReportItem;
import com.example.models.ModelosBases.Event;
import com.example.models.ModelosBases.Position;
import com.example.models.ModelosBases.Report;
import com.example.services.CombinedReportService;
import com.example.services.DevicesReportService;
import com.example.services.EventsReportService;
import com.example.services.ReportService;
import com.example.services.RouteReportService;
import com.example.services.StopsReportService;
import com.example.services.SummaryReportService;
import com.example.services.TripsReportService;

import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final CombinedReportService combinedReportService;
    private final EventsReportService eventsReportService;
    private final RouteReportService routeReportService;
    private final StopsReportService stopsReportService;
    private final SummaryReportService summaryReportService;
    private final TripsReportService tripsReportService;
    private final DevicesReportService devicesReportService;
    private final ReportMailer reportMailer;
    private final SessionUtil sessionUtil;
    private final ReportService reportService;

    // inyectalo en el constructor también
    public ReportController(
            CombinedReportService combinedReportService,
            EventsReportService eventsReportService,
            RouteReportService routeReportService,
            StopsReportService stopsReportService,
            SummaryReportService summaryReportService,
            TripsReportService tripsReportService,
            DevicesReportService devicesReportService,
            ReportMailer reportMailer,
            SessionUtil sessionUtil,
            ReportService reportService) {
        this.combinedReportService = combinedReportService;
        this.eventsReportService = eventsReportService;
        this.routeReportService = routeReportService;
        this.stopsReportService = stopsReportService;
        this.summaryReportService = summaryReportService;
        this.tripsReportService = tripsReportService;
        this.devicesReportService = devicesReportService;
        this.reportMailer = reportMailer;
        this.sessionUtil = sessionUtil;
        this.reportService = reportService;
    }

    @GetMapping
    public List<Report> getScheduledReports(HttpServletRequest request) {
        Long userId = sessionUtil.extractUserIdFromSession(request);
        return reportService.getReportsByUser(userId);
    }

    @PostMapping
    public ResponseEntity<Report> createReport(
            HttpServletRequest request,
            @RequestBody ReportCreationRequest requestBody) {

        Long userId = sessionUtil.extractUserIdFromSession(request);

        Report report = new Report();
        report.setCalendarId(requestBody.getCalendarId());
        report.setType(requestBody.getType());
        report.setDescription(requestBody.getDescription());
        report.setAttributes(requestBody.getAttributes());

        Report created = reportService.createReport(report, userId, requestBody.getDeviceIds());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScheduledReport(HttpServletRequest request, @PathVariable Long id) {
        Long userId = sessionUtil.extractUserIdFromSession(request);
        boolean deleted = reportService.deleteReportIfOwnedByUser(userId, id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Reporte no autorizado o no existe.");
        }
    }

    @GetMapping("/combined")
    public Collection<CombinedReportItem> getCombined(
            HttpServletRequest request,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) {

        Long userId = sessionUtil.extractUserIdFromSession(request);
        return combinedReportService.getReport(userId, deviceIds, groupIds, from, to);
    }

    @GetMapping("/route")
    public Collection<Position> getRoute(
            HttpServletRequest request,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) {

        Long userId = sessionUtil.extractUserIdFromSession(request);
        return routeReportService.getObjects(userId, deviceIds, groupIds, from, to);
    }

    @GetMapping(value = "/route/{type:xlsx|mail}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<?> getRouteExcelOrMail(
            HttpServletRequest request,
            @PathVariable("type") String type,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) throws IOException {

        Long userId = sessionUtil.extractUserIdFromSession(request);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        routeReportService.exportToExcel(stream, userId, deviceIds, groupIds, from, to);
        byte[] bytes = stream.toByteArray();

        if ("xlsx".equalsIgnoreCase(type)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);

        } else if ("mail".equalsIgnoreCase(type)) {
            String emailToSend = sessionUtil.extractUserEmailFromSession(request);
            try {
                reportMailer.send(emailToSend, "Reporte de Ruta", "Adjunto encontrarás el reporte de ruta.", bytes,
                        "report.xlsx");
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException("Error al enviar el correo", e);
            }

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.badRequest().body("Tipo de exportación no soportado");
    }

    @GetMapping("/events")
    public Collection<Event> getEvents(
            HttpServletRequest request,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam(name = "type", required = false, defaultValue = "") List<String> types,
            @RequestParam(name = "alarm", required = false, defaultValue = "") List<String> alarms,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) {

        Long userId = sessionUtil.extractUserIdFromSession(request);
        return eventsReportService.getObjects(userId, deviceIds, groupIds, types, alarms, from, to);
    }

    @GetMapping(value = "/events/{type:xlsx|mail}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<?> getEventsExcelOrMail(
            HttpServletRequest request,
            @PathVariable("type") String type,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam(name = "type", required = false, defaultValue = "") List<String> types,
            @RequestParam(name = "alarm", required = false, defaultValue = "") List<String> alarms,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) throws IOException {

        Long userId = sessionUtil.extractUserIdFromSession(request);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        eventsReportService.exportToExcel(stream, userId, deviceIds, groupIds, types, alarms, from, to);
        byte[] bytes = stream.toByteArray();

        if ("xlsx".equalsIgnoreCase(type)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);

        } else if ("mail".equalsIgnoreCase(type)) {
            String emailToSend = sessionUtil.extractUserEmailFromSession(request);
            try {
                reportMailer.send(emailToSend, "Reporte de Eventos", "Adjunto encontrarás el reporte de eventos.",
                        bytes, "report.xlsx");
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException("Error al enviar el correo", e);
            }

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.badRequest().body("Tipo de exportación no soportado");
    }

    @GetMapping("/summary")
    public Collection<SummaryReportItem> getSummary(
            HttpServletRequest request,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to,
            @RequestParam(name = "daily", defaultValue = "false") boolean daily) {

        Long userId = sessionUtil.extractUserIdFromSession(request);
        return summaryReportService.getObjects(userId, deviceIds, groupIds, from, to, daily);
    }

    @GetMapping(value = "/summary/{type:xlsx|mail}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<?> getSummaryExcelOrMail(
            HttpServletRequest request,
            @PathVariable("type") String type,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to,
            @RequestParam(name = "daily", required = false, defaultValue = "false") boolean daily) throws IOException {

        Long userId = sessionUtil.extractUserIdFromSession(request);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        summaryReportService.exportToExcel(stream, userId, deviceIds, groupIds, from, to, daily);
        byte[] bytes = stream.toByteArray();

        if ("xlsx".equalsIgnoreCase(type)) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=summary-report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);

        } else if ("mail".equalsIgnoreCase(type)) {
            String emailToSend = sessionUtil.extractUserEmailFromSession(request);
            try {
                reportMailer.send(emailToSend, "Reporte Resumen", "Adjunto encontrarás el reporte resumen.",
                        bytes, "summary-report.xlsx");
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException("Error al enviar el correo", e);
            }

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.badRequest().body("Tipo de exportación no soportado");
    }

    @GetMapping("/trips")
    public Collection<TripReportItem> getTrips(
            HttpServletRequest request,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) {

        Long userId = sessionUtil.extractUserIdFromSession(request);
        return tripsReportService.getObjects(userId, deviceIds, groupIds, from, to);
    }

    @GetMapping(value = "/trips/{type:xlsx|mail}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
public ResponseEntity<?> getTripsExcelOrMail(
        HttpServletRequest request,
        @PathVariable("type") String type,
        @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
        @RequestParam(name = "groupId", required = false) List<Long> groupIds,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to
) throws IOException {

    Long userId = sessionUtil.extractUserIdFromSession(request);

    // 🧠 Exporta a Excel
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    tripsReportService.exportToExcel(stream, userId, deviceIds, groupIds, from, to);
    byte[] bytes = stream.toByteArray();

    if ("xlsx".equalsIgnoreCase(type)) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=trips-report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);

    } else if ("mail".equalsIgnoreCase(type)) {
        String emailToSend = sessionUtil.extractUserEmailFromSession(request);
        try {
            reportMailer.send(emailToSend, "Reporte de viajes", "Adjunto encontrarás el reporte de viajes.",
                    bytes, "trips-report.xlsx");
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }

        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.badRequest().body("Tipo de exportación no soportado");
}

    @GetMapping("/stops")
    public Collection<StopReportItem> getStops(
            HttpServletRequest request,
            @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
            @RequestParam(name = "groupId", required = false) List<Long> groupIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to) {

        Long userId = sessionUtil.extractUserIdFromSession(request);
        return stopsReportService.getObjects(userId, deviceIds, groupIds, from, to);
    }

    @GetMapping(value = "/stops/{type:xlsx|mail}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
public ResponseEntity<?> getStopsExcelOrMail(
        HttpServletRequest request,
        @PathVariable("type") String type,
        @RequestParam(name = "deviceId", required = false) List<Long> deviceIds,
        @RequestParam(name = "groupId", required = false) List<Long> groupIds,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date to
) throws IOException {

    Long userId = sessionUtil.extractUserIdFromSession(request);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stopsReportService.exportToExcel(stream, userId, deviceIds, groupIds, from, to);
    byte[] bytes = stream.toByteArray();

    if ("xlsx".equalsIgnoreCase(type)) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stops-report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);

    } else if ("mail".equalsIgnoreCase(type)) {
        String emailToSend = sessionUtil.extractUserEmailFromSession(request);
        try {
            reportMailer.send(emailToSend, "Reporte de Detenciones", "Adjunto encontrarás el reporte de detenciones.",
                    bytes, "stops-report.xlsx");
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }

        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.badRequest().body("Tipo de exportación no soportado");
}


@GetMapping(value = "/devices/{type:xlsx|mail}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
public ResponseEntity<?> getDevicesExcelOrMail(
        HttpServletRequest request,
        @PathVariable("type") String type) throws IOException {

    Long userId = sessionUtil.extractUserIdFromSession(request);

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    devicesReportService.exportToExcel(stream, userId);
    byte[] bytes = stream.toByteArray();

    if ("xlsx".equalsIgnoreCase(type)) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=devices-report.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);

    } else if ("mail".equalsIgnoreCase(type)) {
        String emailToSend = sessionUtil.extractUserEmailFromSession(request);
        try {
            reportMailer.send(emailToSend, "Reporte de Dispositivos", "Adjunto encontrarás el reporte de dispositivos.",
                    bytes, "devices-report.xlsx");
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Error al enviar el correo", e);
        }

        return ResponseEntity.noContent().build();
    }

    return ResponseEntity.badRequest().body("Tipo de exportación no soportado");
}



}
