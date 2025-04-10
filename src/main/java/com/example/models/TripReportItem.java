package com.example.models;
public class TripReportItem extends BaseReportItem {

    private long startPositionId;
    private long endPositionId;
    private double startLat;
    private double startLon;
    private double endLat;
    private double endLon;
    private String startAddress;
    private String endAddress;
    private long duration;
    private String driverUniqueId;
    private String driverName;

    // Getters y setters

    public long getStartPositionId() {
        return startPositionId;
    }

    public void setStartPositionId(long startPositionId) {
        this.startPositionId = startPositionId;
    }

    public long getEndPositionId() {
        return endPositionId;
    }

    public void setEndPositionId(long endPositionId) {
        this.endPositionId = endPositionId;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLon() {
        return startLon;
    }

    public void setStartLon(double startLon) {
        this.startLon = startLon;
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getEndLon() {
        return endLon;
    }

    public void setEndLon(double endLon) {
        this.endLon = endLon;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDriverUniqueId() {
        return driverUniqueId;
    }

    public void setDriverUniqueId(String driverUniqueId) {
        this.driverUniqueId = driverUniqueId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}
