package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

public class VoiceRecognitionDisabled {
    public final Door door;

    public VoiceRecognitionDisabled(Door door) {
        this.door = door;
    }
}