package com.nanospark.gard.events;

import com.squareup.otto.Produce;

import mobi.tattu.utils.Tattu;

/**
 * Created by Leandro on 21/7/2015.
 */
public class RecognizerLifecycle {

    private static RecognizerLifecycle instance;
    private State currentState = new State(State.STOPPED);

    private RecognizerLifecycle() {
        Tattu.register(this);
    }

    public static final RecognizerLifecycle getInstance() {
        if (instance == null) {
            instance = new RecognizerLifecycle();
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
