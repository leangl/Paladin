package com.nanospark.gard.ui.fragments;

import com.nanospark.gard.GarD;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.DoorActivationFailed;
import com.nanospark.gard.events.DoorToggled;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.squareup.otto.Subscribe;

/**
 * Created by cristian on 07/10/15.
 */
public class DoorOneFragment extends BaseDoorFragment {

    public static DoorOneFragment newInstance() {
        DoorOneFragment fragment = new DoorOneFragment();
        return fragment;
    }

    @Override
    public Door getDoor() {
        return Door.getInstance(GarD.DOOR_ONE_ID);
    }

    @Subscribe
    public void on(VoiceRecognizer.StateChanged event) {
        super.on(event);
    }

    @Subscribe
    public void on(DoorToggled event) {
        super.on(event);
    }

    @Subscribe
    public void on(DoorActivationFailed doorActivationFailed) {
        super.on(doorActivationFailed);
    }

    @Subscribe
    public void on(BoardConnected event) {
        super.on(event);
    }

    @Subscribe
    public void on(BoardDisconnected event) {
        super.on(event);
    }

}
