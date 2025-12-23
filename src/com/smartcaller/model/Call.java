package com.smartcaller.model;

import java.time.LocalDateTime;

public abstract class Call {
    protected int id;
    protected String contactName;
    protected String phoneNumber;
    protected LocalDateTime scheduledTime;
    protected int priority;
    protected CallStatus status;

    public Call(String contactName, String phoneNumber, LocalDateTime scheduledTime) {
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.scheduledTime = scheduledTime;
        this.priority = 1;
        this.status = CallStatus.PENDING;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public CallStatus getStatus() { return status; }
    public void setStatus(CallStatus status) { this.status = status; }

    public abstract String getCallType();

    @Override
    public String toString() {
        return String.format("Call{id=%d, contact='%s', phone='%s', time=%s, type=%s, priority=%d, status=%s}",
                id, contactName, phoneNumber, scheduledTime, getCallType(), priority, status);
    }
}