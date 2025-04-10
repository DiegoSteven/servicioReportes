package com.example.models;

import java.util.List;

import com.example.models.ModelosBases.Event;
import com.example.models.ModelosBases.Position;

public class CombinedReportItem {

    private long deviceId;
    private List<double[]> route;
    private List<Event> events;
    private List<Position> positions;

    // Getters y setters

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public List<double[]> getRoute() {
        return route;
    }

    public void setRoute(List<double[]> route) {
        this.route = route;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }
}