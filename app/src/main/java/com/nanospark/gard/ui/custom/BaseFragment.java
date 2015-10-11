package com.nanospark.gard.ui.custom;

import android.os.Bundle;

import mobi.tattu.utils.Tattu;

/**
 * Created by cristian on 23/09/15.
 */
public abstract class BaseFragment extends mobi.tattu.utils.fragments.BaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tattu.bus().register(this);
        if(showHomeIcon()){
            getBaseActivity().showHomeIcon();
        }
    }

    public int getColorFromResource(int color){
        return getBaseActivity().getColorFromResource(color);
    }

    public BaseActivity getBaseActivity(){
        return (BaseActivity)getActivity();
    }



    @Override
    public void onDetach() {
        super.onDetach();
        Tattu.bus().unregister(this);
    }

    public abstract boolean showHomeIcon();
}
