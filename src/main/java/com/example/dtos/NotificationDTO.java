package com.example.dtos;

import java.util.Map;

public class NotificationDTO {
    public String id;
    public String type;
    public Boolean always;
    public Boolean web;
    public Boolean mail;
    public Boolean sms;
    public Long calendarId;
    public Long commandId;
    public String description;
    public Map<String, Object> attributes;
}
