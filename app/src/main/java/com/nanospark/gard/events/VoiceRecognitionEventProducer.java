package com.nanospark.gard.events;

import com.squareup.otto.Produce;

import mobi.tattu.utils.Tattu;

/**
 * Created by Leandro on 21/7/2015.
 */
public class VoiceRecognitionEventProducer {

    private static VoiceRecognitionEventProducer instance;
    private State currentState = new State(State.STOPPED);

    private VoiceRecognitionEventProducer() {
        Tattu.register(this);
    }

    public static final VoiceRecognitionEventProducer getInstance() {
        if (instance == null) {
            instance = new VoiceRecognitionEventProducer();
        }
        return instance;
    }

    public void setState(int state) {
        this.currentState = new State(state);
        Tattu.post(this.currentState);
    }

    @Produce
    public State getCurrentState() {
        return this.currentState;
    }

    public static class State {
        public static final int STARTED = 1;
        public static final int STOPPED = 0;
        public static final int ERROR = -1;

        public final int state;

        public State(int state) {
            this.state = state;
        }
    }

}
