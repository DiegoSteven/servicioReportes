package com.example.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TripsConfig {

    @Value("${trips.minimal.trip.duration:300000}") // 5 minutos en milisegundos
    private long minimalTripDuration;

    @Value("${trips.minimal.stop.duration:300000}") // 5 minutos en milisegundos
    private long minimalStopDuration;

    @Value("${trips.minimal.no.data.duration:0}")
    private long minimalNoDataDuration;

    @Value("${trips.ignore.odometer:false}")
    private boolean ignoreOdometer;

    public long getMinimalTripDuration() {
        return minimalTripDuration;
    }

    public long getMinimalStopDuration() {
        return minimalStopDuration;
    }

    public long getMinimalNoDataDuration() {
        return minimalNoDataDuration;
    }

    public boolean getIgnoreOdometer() {
        return ignoreOdometer;
    }
}