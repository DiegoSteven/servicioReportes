package com.example.Util;

import java.util.Locale;

import com.example.models.ModelosBases.Position;

public class ReportTemplateUtils {
    public String hyperlink(String url, String text) {
        return String.format(Locale.US, "HYPERLINK(\"%s\", \"%s\")", url, text.replace("\"", "\"\""));
    }

    public String formatUrl(double lat, double lon) {
        return String.format(Locale.US,
            "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=16/%.6f/%.6f",
            lat, lon, lat, lon);
    }

    public String formatCoordinates(double lat, double lon) {
        return String.format(Locale.US, "%.6f°, %.6f°", lat, lon);
    }
    public String formulaHyperlink(Position pos) {
        if (pos == null) return "";
        String url = formatUrl(pos.getLatitude(), pos.getLongitude());
        String text = pos.getAddress() != null
                ? pos.getAddress()
                : formatCoordinates(pos.getLatitude(), pos.getLongitude());
    
        return String.format(Locale.US,
                "=HYPERLINK(\"%s\", \"%s\")", url, text.replace("\"", "\"\""));
    }
    
    
}

    
