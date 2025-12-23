package com.smartcaller.model;

import java.time.LocalDateTime;

public class VoiceCall extends Call {
    public VoiceCall(String contactName, String phoneNumber, LocalDateTime scheduledTime) {
        super(contactName, phoneNumber, scheduledTime);
    }

    @Override
    public String getCallType() {
        return "VOICE_CALL";
    }
}