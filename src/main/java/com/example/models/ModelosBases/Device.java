package com.example.models.ModelosBases;

import jakarta.persistence.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "tc_devices")
public class Device {

    public static final String STATUS_UNKNOWN = "unknown";
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "groupid")
    private Long groupId;

    @Column(name = "calendarid")
    private Long calendarId;

    @Column(name = "name")
    private String name;

    @Column(name = "uniqueid")
    private String uniqueId;

    @Column(name = "status")
    private String status;

    @Column(name = "lastupdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @Column(name = "positionid")
    private Long positionId;

    @Column(name = "phone")
    private String phone;

    @Column(name = "model")
    private String model;

    @Column(name = "contact")
    private String contact;

    @Column(name = "category")
    private String category;

    @Column(name = "disabled")
    private boolean disabled;

    @Column(name = "expirationtime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirationTime;

    @Transient
    @JsonIgnore
    private boolean motionStreak;

    @Transient
    @JsonIgnore
    private boolean motionState;

    @Transient
    @JsonIgnore
    private Date motionTime;

    @Transient
    @JsonIgnore
    private double motionDistance;

    @Transient
    @JsonIgnore
    private boolean overspeedState;

    @Transient
    @JsonIgnore
    private Date overspeedTime;

    @Transient
    @JsonIgnore
    private Long overspeedGeofenceId;

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        if (uniqueId != null && uniqueId.contains("..")) {
            throw new IllegalArgumentException("Invalid unique id");
        }
        this.uniqueId = uniqueId != null ? uniqueId.trim() : null;
    }

    public String getStatus() {
        return status != null ? status.trim() : STATUS_OFFLINE;
    }

    public void setStatus(String status) {
        this.status = status != null ? status.trim() : null;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

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

    public boolean getOverspeedState() {
        return overspeedState;
    }

    public void setOverspeedState(boolean overspeedState) {
        this.overspeedState = overspeedState;
    }

    public Date getOverspeedTime() {
        return overspeedTime;
    }

    public void setOverspeedTime(Date overspeedTime) {
        this.overspeedTime = overspeedTime;
    }

    public Long getOverspeedGeofenceId() {
        return overspeedGeofenceId;
    }

    public void setOverspeedGeofenceId(Long overspeedGeofenceId) {
        this.overspeedGeofenceId = overspeedGeofenceId;
    }
}