package com.example.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeviceReportSection {

    private String deviceName;
    private String groupName = "";
    private List<?> objects;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Collection<?> getObjects() {
        return objects;
    }

    public void setObjects(Collection<?> objects) {
        this.objects = new ArrayList<>(objects);
    }
}
