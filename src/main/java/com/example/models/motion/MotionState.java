package com.example.models.motion;

import java.util.Date;

public class MotionState {

    private boolean motionStreak;
    private boolean motionState;
    private Date motionTime;
    private double motionDistance;
    private String event;

    public boolean getMotionStreak() {
        return motionStreak;
    }

    public void setMotionStreak(boolean motionStreak) {
        this.motionStreak = motionStreak;
    }

    public boolean getMotionState() {
        return motionState;
    }

    public void setMotionState(boolean motionState) {
        this.motionState = motionState;
    }

    public Date getMotionTime() {
        return motionTime;
    }

    public void setMotionTime(Date motionTime) {
        this.motionTime = motionTime;
    }

    public double getMotionDistance() {
        return motionDistance;
    }

    public void setMotionDistance(double motionDistance) {
        this.motionDistance = motionDistance;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    
}
