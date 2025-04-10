package com.example.models;

import com.example.models.ModelosBases.*;

public class DeviceReportItem {

    private Device device;
    private Position position;

    public DeviceReportItem(Device device, Position position) {
        this.device = device;
        this.position = position;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}
