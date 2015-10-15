package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.events.DoorActivationFailed;
import com.nanospark.gard.events.DoorToggled;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.squareup.otto.Subscribe;

/**
 * Created by cristian on 07/10/15.
 */
public class DoorTwoFragment extends BaseDoorFragment {

    public static DoorTwoFragment newInstance() {
        DoorTwoFragment fragment = new DoorTwoFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
//        getCardView().findViewById(R.id.container_center).setBackgroundColor(getColorFromResource(R.color.door_two_background));
//        getCardView().findViewById(R.id.container_switch).setBackgroundColor(getColorFromResource(R.color.door_two_switch_background));
        ((TextView)getCardView().findViewById(R.id.textview_title_door)).setText(R.string.back_door_label);
        return view;
    }

    @Override
    public Door getDoor() {
        return Door.getInstance(GarD.DOOR_TWO_ID);
    }

    @Subscribe
    public void on(VoiceRecognizer.State state) {
        handlerVoiceState(state);
    }
    @Subscribe
    public void on(DoorToggled event) {
        handlerDoorState(event);
    }

    @Subscribe
    public void on(DoorActivationFailed doorActivationFailed) {
        handlerDoorState(doorActivationFailed);
    }

}
