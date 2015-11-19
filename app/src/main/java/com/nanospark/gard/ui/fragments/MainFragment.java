package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nanospark.gard.R;

/**
 * Created by cristian on 23/09/15.
 */
public class MainFragment extends com.nanospark.gard.ui.custom.BaseFragment {

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public boolean showHomeIcon() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        showFragment(R.id.door_one_container, DoorCardFragment.newInstance(1));
        showFragment(R.id.door_two_container, DoorCardFragment.newInstance(2));
        showFragment(R.id.weather_container, WeatherCardFragment.newInstance());
        return view;
    }

    private void showFragment(int container, Fragment fragment) {
        getBaseActivity().getSupportFragmentManager().beginTransaction().replace(container, fragment).commit();
    }

}
