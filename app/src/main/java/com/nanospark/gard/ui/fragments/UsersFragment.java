package com.nanospark.gard.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nanospark.gard.R;
import com.nanospark.gard.ui.custom.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends BaseFragment {


    public static UsersFragment newInstance() {
        
        Bundle args = new Bundle();
        
        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        GridLayout  gridLayout = (GridLayout) view.findViewById(R.id.gridlayout);
        gridLayout.setUseDefaultMargins(true);
        int size = 2;
        for (int i = 0 ; i < size ; i++){
            View userView1 = inflater.inflate(R.layout.user_layout,null,false);
            GridLayout.LayoutParams  layoutParams = new GridLayout.LayoutParams();
            layoutParams.rowSpec = GridLayout.spec(1,Float.valueOf("1.0"));
            layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED,Float.valueOf("1.0"));
            userView1.setLayoutParams(layoutParams);
            gridLayout.addView(userView1);

        }
        return view;
    }


    @Override
    public boolean showHomeIcon() {
        return false;
    }
}
