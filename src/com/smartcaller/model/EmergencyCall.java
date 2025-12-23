package com.smartcaller.model;

import java.time.LocalDateTime;

public class EmergencyCall extends Call {
    private String emergencyType;

    public EmergencyCall(String contactName, String phoneNumber, LocalDateTime scheduledTime, String emergencyType) {
        super(contactName, phoneNumber, scheduledTime);
        this.emergencyType = emergencyType;
        this.priority = 10; // Highest priority
    }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    @Override
    public String getCallType() {
        return "EMERGENCY_CALL";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", emergencyType='%s'", emergencyType);
    }
}