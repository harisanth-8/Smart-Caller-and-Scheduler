package com.smartcaller.model;

import java.time.LocalDateTime;

public class VideoCall extends Call {
    private String videoPlatform;

    public VideoCall(String contactName, String phoneNumber, LocalDateTime scheduledTime, String videoPlatform) {
        super(contactName, phoneNumber, scheduledTime);
        this.videoPlatform = videoPlatform;
    }

    public String getVideoPlatform() { return videoPlatform; }
    public void setVideoPlatform(String videoPlatform) { this.videoPlatform = videoPlatform; }

    @Override
    public String getCallType() {
        return "VIDEO_CALL";
    }

    @Override
    public String toString() {
        return super.toString() + String.format(", platform='%s'", videoPlatform);
    }
}