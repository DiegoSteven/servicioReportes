package com.example.models.ModelosBases;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.example.Util.AttributesConverter;

@Entity
@Table(name = "tc_users")
public class User {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "login")
    private String login;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "readonly")
    private boolean readonly;

    @Column(name = "administrator")
    private boolean administrator;

    @Column(name = "map")
    private String map;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "zoom")
    private int zoom;

    @Column(name = "coordinateformat")
    private String coordinateFormat;

    @Column(name = "disabled")
    private boolean disabled;

    @Column(name = "expirationtime")
    private Date expirationTime;

    @Column(name = "devicelimit")
    private int deviceLimit;

    @Column(name = "userlimit")
    private int userLimit;

    @Column(name = "devicereadonly")
    private boolean deviceReadonly;

    @Column(name = "limitcommands")
    private boolean limitCommands;

    @Column(name = "disablereports")
    private boolean disableReports;

    @Column(name = "fixedemail")
    private boolean fixedEmail;

    @Column(name = "poilayer")
    private String poiLayer;

    @Column(name = "totpkey")
    private String totpKey;

    @Column(name = "temporary")
    private boolean temporary;

    @Convert(converter = AttributesConverter.class)
    @Column(name = "attributes")
    private Map<String, Object> attributes = new HashMap<>();

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone != null ? phone.trim() : null;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
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

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public String getCoordinateFormat() {
        return coordinateFormat;
    }

    public void setCoordinateFormat(String coordinateFormat) {
        this.coordinateFormat = coordinateFormat;
    }

    public boolean isDisabled() {
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

    public int getDeviceLimit() {
        return deviceLimit;
    }

    public void setDeviceLimit(int deviceLimit) {
        this.deviceLimit = deviceLimit;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public void setUserLimit(int userLimit) {
        this.userLimit = userLimit;
    }

    public boolean isDeviceReadonly() {
        return deviceReadonly;
    }

    public void setDeviceReadonly(boolean deviceReadonly) {
        this.deviceReadonly = deviceReadonly;
    }

    public boolean isLimitCommands() {
        return limitCommands;
    }

    public void setLimitCommands(boolean limitCommands) {
        this.limitCommands = limitCommands;
    }

    public boolean isDisableReports() {
        return disableReports;
    }

    public void setDisableReports(boolean disableReports) {
        this.disableReports = disableReports;
    }

    public boolean isFixedEmail() {
        return fixedEmail;
    }

    public void setFixedEmail(boolean fixedEmail) {
        this.fixedEmail = fixedEmail;
    }

    public String getPoiLayer() {
        return poiLayer;
    }

    public void setPoiLayer(String poiLayer) {
        this.poiLayer = poiLayer;
    }

    public String getTotpKey() {
        return totpKey;
    }

    public void setTotpKey(String totpKey) {
        this.totpKey = totpKey;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
