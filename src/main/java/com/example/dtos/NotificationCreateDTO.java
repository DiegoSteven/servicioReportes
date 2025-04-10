package com.example.dtos;

import java.util.Map;

public class NotificationCreateDTO {
    public String type;
    public Boolean always;
    public Boolean web;
    public Boolean mail;
    public Boolean sms;
    public Long calendarId;
    public Long commandId;
    public String description;
    public String notificators;
    public Map<String, Object> attributes;
    public Long userId;
}
