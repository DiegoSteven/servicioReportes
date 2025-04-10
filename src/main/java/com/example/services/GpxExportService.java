package com.example.services;

import java.io.IOException;
import java.util.Collection;

import org.springframework.stereotype.Service;

import com.example.models.ModelosBases.Position;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class GpxExportService {

    public void exportToGpx(Collection<Position> positions, HttpServletResponse response) throws IOException {
        response.setContentType("application/gpx+xml");
        response.setHeader("Content-Disposition", "attachment; filename=\"route.gpx\"");

        // Versión inicial: solo encabezado de GPX
        var writer = response.getWriter();
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<gpx version=\"1.1\" creator=\"YourApp\">");
        writer.println("<trk><name>Route</name><trkseg>");

        // Por ahora, solo plantilla
        for (Position position : positions) {
            writer.printf("<trkpt lat=\"%f\" lon=\"%f\"></trkpt>\n", position.getLatitude(), position.getLongitude());
        }

        writer.println("</trkseg></trk>");
        writer.println("</gpx>");
    }
}