package com.nanospark.gard.ui.fragments;

import com.nanospark.gard.GarD;
import com.nanospark.gard.events.DoorActivated;
import com.nanospark.gard.events.DoorActivationFailed;
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
    public void on(VoiceRecognizer.State state) {
        handlerVoiceState(state);

    }

    @Subscribe
    public void on(DoorActivated doorActivated) {
        handlerDoorState(doorActivated);

    }

    @Subscribe
    public void on(DoorActivationFailed doorActivationFailed) {
        handlerDoorState(doorActivationFailed);
    }

}
