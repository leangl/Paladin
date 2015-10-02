package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nanospark.gard.R;

/**
 * Created by cristian on 23/09/15.
 */
public class MainFragment extends com.nanospark.gard.ui.custom.BaseFragment {

    private CardView  mCardViewOneDoor;
    private CardView mCardViewTwoDoor;

    public static MainFragment newInstance() {
        
        Bundle args = new Bundle();
        
        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_main,container,false);
        FrameLayout containerOne = (FrameLayout) view.findViewById(R.id.door_one_container);
        FrameLayout containerTwo = (FrameLayout) view.findViewById(R.id.door_two_container);

        mCardViewOneDoor = (CardView) containerOne.findViewById(R.id.cardview_door);
        mCardViewTwoDoor = (CardView) containerTwo.findViewById(R.id.cardview_door);

        mCardViewTwoDoor.findViewById(R.id.container_center).setBackgroundColor(getColorFromResource(R.color.door_two_background));
        mCardViewTwoDoor.findViewById(R.id.container_switch).setBackgroundColor(getColorFromResource(R.color.door_two_switch_background));
        return  view;
    }



}
