package com.nanospark.gard.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.ui.custom.BaseFragment;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends BaseFragment {

    @Inject
    private UserManager mUserManager;

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
        view.findViewById(R.id.fb_add_user).setOnClickListener(v -> {

        });
        List<User> userList = mUserManager.getAll();
        int size = userList.size();
        for (int i = 0 ; i < size ; i++){
            View userView = inflater.inflate(R.layout.user_layout,null,false);
            User user = userList.get(i);

            TextView name  = (TextView) userView.findViewById(R.id.textview_user_name);
            TextView phone = (TextView) userView.findViewById(R.id.textview_phone);
            TextView timeLimits = (TextView) userView.findViewById(R.id.textview_time_limits);

            populateUserView(user, name, phone);

            addUserViewToGrid(gridLayout, userView);

        }
        return view;
    }

    private void createUser(){

    }

    private void populateUserView(User user, TextView name, TextView phone) {
        name.setText(user.getName());
        phone.setText(user.getPhone());
    }

    private void addUserViewToGrid(GridLayout gridLayout, View userView) {
        GridLayout.LayoutParams  layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(1,Float.valueOf("1.0"));
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED,Float.valueOf("1.0"));
        userView.setLayoutParams(layoutParams);
        gridLayout.addView(userView);
    }


    @Override
    public boolean showHomeIcon() {
        return false;
    }
}
