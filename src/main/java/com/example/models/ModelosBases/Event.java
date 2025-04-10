package com.example.models.ModelosBases;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.example.Util.AttributesConverter;

@Entity
@Table(name = "tc_events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = AttributesConverter.class)
    @Column(name = "attributes")
    private Map<String, Object> attributes = new HashMap<>();

    @Column(name = "deviceid")
    private Integer deviceId;

    private String type;

    @Column(name = "eventtime")
    private Date eventTime;

    @Column(name = "positionid")
    private Integer positionId;

    @Column(name = "geofenceid")
    private Integer geofenceId;

    @Column(name = "maintenanceid")
    private Integer maintenanceId;


    public Event() {
        this.eventTime = new Date();
    }

    public Event(String type, Integer deviceId) {
        this.type = type;
        this.deviceId = deviceId;
        this.eventTime = new Date();
    }

    public static final String TYPE_COMMAND_RESULT = "commandResult";
    public static final String TYPE_DEVICE_ONLINE = "deviceOnline";
    public static final String TYPE_DEVICE_UNKNOWN = "deviceUnknown";
    public static final String TYPE_DEVICE_OFFLINE = "deviceOffline";
    public static final String TYPE_DEVICE_INACTIVE = "deviceInactive";
    public static final String TYPE_QUEUED_COMMAND_SENT = "queuedCommandSent";

    public static final String TYPE_DEVICE_MOVING = "deviceMoving";
    public static final String TYPE_DEVICE_STOPPED = "deviceStopped";

    public static final String TYPE_DEVICE_OVERSPEED = "deviceOverspeed";
    public static final String TYPE_DEVICE_FUEL_DROP = "deviceFuelDrop";
    public static final String TYPE_DEVICE_FUEL_INCREASE = "deviceFuelIncrease";

    public static final String TYPE_GEOFENCE_ENTER = "geofenceEnter";
    public static final String TYPE_GEOFENCE_EXIT = "geofenceExit";

    public static final String TYPE_ALARM = "alarm";
    public static final String TYPE_IGNITION_ON = "ignitionOn";
    public static final String TYPE_IGNITION_OFF = "ignitionOff";

    public static final String TYPE_MAINTENANCE = "maintenance";
    public static final String TYPE_TEXT_MESSAGE = "textMessage";
    public static final String TYPE_DRIVER_CHANGED = "driverChanged";
    public static final String TYPE_MEDIA = "media";

   

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getPositionId() {
        return positionId;
    }

    public void setPositionId(Integer positionId) {
        this.positionId = positionId;
    }

    public Integer getGeofenceId() {
        return geofenceId;
    }

    public void setGeofenceId(Integer geofenceId) {
        this.geofenceId = geofenceId;
    }

    public Long getMaintenanceId() {
        return maintenanceId != null ? maintenanceId : 0L;
    }
    

    public void setMaintenanceId(Integer maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}
