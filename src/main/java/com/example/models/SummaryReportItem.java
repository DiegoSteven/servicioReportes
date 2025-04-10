package com.example.models;

public class SummaryReportItem extends BaseReportItem {

    private long startHours; // en milisegundos
    private long endHours;   // en milisegundos

    public long getStartHours() {
        return startHours;
    }

    public void setStartHours(long startHours) {
        this.startHours = startHours;
    }

    public long getEndHours() {
        return endHours;
    }

    public void setEndHours(long endHours) {
        this.endHours = endHours;
    }

    public long getEngineHours() {
        return endHours - startHours;
    }
}