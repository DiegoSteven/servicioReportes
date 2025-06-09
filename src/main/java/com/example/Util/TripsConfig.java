package com.example.Util;

import com.example.helper.AttributeUtil;

public class TripsConfig {

    private final double minimalTripDistance;
    private final long minimalTripDuration;
    private final long minimalParkingDuration;
    private final long minimalNoDataDuration;
    private final boolean useIgnition;
    private final boolean ignoreOdometer;

    public TripsConfig(AttributeUtil.Provider provider) {
        this.minimalTripDistance = AttributeUtil.lookup(provider, "report.trip.minimalTripDistance", 500L); // ✅ mínimo 500 m
        this.minimalTripDuration = AttributeUtil.lookup(provider, "report.trip.minimalTripDuration", 300L) * 1000; // ✅ mínimo 5 min
        this.minimalParkingDuration = AttributeUtil.lookup(provider, "report.trip.minimalParkingDuration", 300L) * 1000; // ✅ 5 min de inactividad para cortar un viaje
        this.minimalNoDataDuration = AttributeUtil.lookup(provider, "report.trip.minimalNoDataDuration", 0L) * 1000; // ✅ hueco de datos para considerar corte
        this.useIgnition = AttributeUtil.lookup(provider, "report.trip.useIgnition", false); // ❌ no uses ignición si no tienes ese dato
        this.ignoreOdometer = AttributeUtil.lookup(provider, "report.ignoreOdometer", false); // ✅ usa odómetro si lo tienes
    }
    
    public double getMinimalTripDistance() {
        return minimalTripDistance;
    }

    public long getMinimalTripDuration() {
        return minimalTripDuration;
    }

    public long getMinimalParkingDuration() {
        return minimalParkingDuration;
    }

    public long getMinimalNoDataDuration() {
        return minimalNoDataDuration;
    }

    public boolean getUseIgnition() {
        return useIgnition;
    }

    public boolean getIgnoreOdometer() {
        return ignoreOdometer;
    }
}
