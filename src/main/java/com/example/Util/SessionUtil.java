package com.example.Util;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

@Component
public class SessionUtil {

    private final RestTemplate restTemplate;

    public SessionUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Long extractUserIdFromSession(HttpServletRequest request) {
        return Long.valueOf(getSessionData(request).get("id").toString());
    }

    public String extractUserEmailFromSession(HttpServletRequest request) {
        Object email = getSessionData(request).get("email");
        return email != null ? email.toString() : null;
    }

    private Map<String, Object> getSessionData(HttpServletRequest request) {
        String sessionCookie = Arrays.stream(request.getCookies())
                .filter(c -> "JSESSIONID".equals(c.getName()))
                .findFirst()
                .map(c -> "JSESSIONID=" + c.getValue())
                .orElseThrow(() -> new RuntimeException("Sesión no encontrada"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", sessionCookie);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "http://localhost:8082/api/session", HttpMethod.GET, entity, Map.class);

        return response.getBody();
    }
}
