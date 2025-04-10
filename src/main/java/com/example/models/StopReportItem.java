package com.example.models;

public class StopReportItem extends BaseReportItem {

    private long positionId;
    private double latitude;
    private double longitude;
    private String address;
    private long duration;
    private long engineHours; // en milisegundos

    // Getters y setters

    public long getPositionId() {
        return positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getEngineHours() {
        return engineHours;
    }

    public void setEngineHours(long engineHours) {
        this.engineHours = engineHours;
    }
}