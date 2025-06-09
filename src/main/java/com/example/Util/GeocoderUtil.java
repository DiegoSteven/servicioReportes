package com.example.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class GeocoderUtil {

    public String geocodePosition(double lat, double lon) {
        try {
            String url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon;
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestProperty("User-Agent", "TraccarMicroservice");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            // Si solo quieres coordenadas como texto, ignora la dirección real
            return String.format(Locale.US, "%.6f°, %.6f°", lat, lon);
        } catch (Exception e) {
            return String.format(Locale.US, "%.6f°, %.6f°", lat, lon);
        }
    }

}
