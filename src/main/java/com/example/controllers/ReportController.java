package com.example.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Util.ReportMailer;
import com.example.Util.SessionUtil;
import com.example.models.CombinedReportItem;
import com.example.models.ModelosBases.Position;
import com.example.services.CombinedReportService;
import com.example.services.DevicesReportService;
import com.example.services.EventsReportService;
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

    public ReportController(
            CombinedReportService combinedReportService,
            EventsReportService eventsReportService,
            RouteReportService routeReportService,
            StopsReportService stopsReportService,
            SummaryReportService summaryReportService,
            TripsReportService tripsReportService,
            DevicesReportService devicesReportService,
            ReportMailer reportMailer,
            SessionUtil sessionUtil) {
        this.combinedReportService = combinedReportService;
        this.eventsReportService = eventsReportService;
        this.routeReportService = routeReportService;
        this.stopsReportService = stopsReportService;
        this.summaryReportService = summaryReportService;
        this.tripsReportService = tripsReportService;
        this.devicesReportService = devicesReportService;
        this.reportMailer = reportMailer;
        this.sessionUtil = sessionUtil;
    }

    @GetMapping
    public ResponseEntity<String> baseEndpoint() {
        return ResponseEntity.ok("Endpoint base de reports activo.");
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
/* 
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
*/
}
