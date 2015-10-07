package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.model.door.Door;

/**
 * Created by cristian on 23/09/15.
 */
public class MainFragment extends com.nanospark.gard.ui.custom.BaseFragment {

    private CardView mCardViewOneDoor;
    private CardView mCardViewTwoDoor;
    private ImageView mImageViewVoice;

    private boolean mDoorOneSwitchValue;
    private boolean mDoorTwoSwitchValue;

    private Door mDoorOne = Door.getInstance(GarD.DOOR_ONE_ID);
    private Door mDoorTwo = Door.getInstance(GarD.DOOR_TWO_ID);


    public static MainFragment newInstance() {

        Bundle args = new Bundle();
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        showFragment(R.id.door_one_container,DoorOneFragment.newInstance());
        showFragment(R.id.door_two_container,DoorTwoFragment.newInstance());
        return view;
    }

    private void showFragment(int container,Fragment fragment){
        getBaseActivity().getSupportFragmentManager().beginTransaction().replace(container,fragment).commit();

    }
    @Override
    public void onResume() {
        super.onResume();

    }


}
