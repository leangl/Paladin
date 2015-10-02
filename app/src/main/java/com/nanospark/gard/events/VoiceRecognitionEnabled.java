package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

public class VoiceRecognitionEnabled {
    public final Door door;

    public VoiceRecognitionEnabled(Door door) {
        this.door = door;
    }
}