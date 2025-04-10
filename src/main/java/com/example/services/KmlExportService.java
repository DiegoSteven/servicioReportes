package com.example.services;

import java.io.IOException;
import java.util.Collection;

import org.springframework.stereotype.Service;

import com.example.models.ModelosBases.Position;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class KmlExportService {

    public void exportToKml(Collection<Position> positions, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.google-earth.kml+xml");
        response.setHeader("Content-Disposition", "attachment; filename=\"route.kml\"");

        var writer = response.getWriter();
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        writer.println("<Document><name>Route</name><Placemark><LineString><coordinates>");

        for (Position position : positions) {
            writer.printf("%f,%f\n", position.getLongitude(), position.getLatitude());
        }

        writer.println("</coordinates></LineString></Placemark></Document></kml>");
    }
}
