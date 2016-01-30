package com.nanospark.gard.ui.custom;

import android.os.Bundle;

/**
 * Created by cristian on 23/09/15.
 */
public abstract class BaseFragment extends mobi.tattu.utils.fragments.BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (showHomeIcon()) {
            getBaseActivity().showHomeIcon();
        }
    }

    public int getColorFromResource(int color) {
        return getBaseActivity().getColorFromResource(color);
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    public abstract boolean showHomeIcon();
}
