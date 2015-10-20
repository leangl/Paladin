package com.nanospark.gard.ui.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.ui.activity.CreateUserActivity;
import com.nanospark.gard.ui.custom.BaseFragment;

import java.util.Calendar;
import java.util.List;

import mobi.tattu.utils.persistance.datastore.DataStore;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends BaseFragment {

    @Inject
    private UserManager mUserManager;
    private GridLayout mGridLayout;

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
        this.mGridLayout = (GridLayout) view.findViewById(R.id.gridlayout);
        this.mGridLayout.setUseDefaultMargins(true);
        view.findViewById(R.id.fb_add_user).setOnClickListener(v -> {
            showCreateUser();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        List<User> userList = mUserManager.getAll();
        int size = userList.size();
        this.mGridLayout.removeAllViews();
        for (int i = 0 ; i < size ; i++){
            View userView = LayoutInflater.from(getBaseActivity()).inflate(R.layout.user_layout, null, false);
            User user = userList.get(i);

            TextView name  = (TextView) userView.findViewById(R.id.textview_user_name);
            TextView phone = (TextView) userView.findViewById(R.id.textview_phone);
            TextView timeLimits = (TextView) userView.findViewById(R.id.textview_time_limits);
            ImageView receiveAlerts = (ImageView) userView.findViewById(R.id.imageview_receive_alerts);
            userView.findViewById(R.id.imageview_menu).setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(getBaseActivity(),v);
                popupMenu.inflate(R.menu.actions);
                popupMenu.setOnMenuItemClickListener(item -> {
                    handlerPopMenu(item, user);
                    return true;
                });
                popupMenu.show();
            });
            if(user.getNotify() != null){
                receiveAlerts.setImageResource(R.drawable.ic_alert_enabled);
            }
            String startTime = Utils.getHour(getCalendarHour(user.getSchedule().getStartHour(), user.getSchedule().getStartMinute()));
            String endTime = Utils.getHour(getCalendarHour(user.getSchedule().getEndHour(),user.getSchedule().getEndMinute()));
            timeLimits.setText(startTime +Utils.SPACE+ endTime);
            populateUserView(user, name, phone);

            addUserViewToGrid(this.mGridLayout, userView);

        }
    }

    private void handlerPopMenu(MenuItem item,User user) {
        switch (item.getItemId()){
            case R.id.action_delete:
                DataStore.getInstance().delete(User.class,user.getName());
                loadUsers();
                break;
            case R.id.action_edit:
                Intent intent = new Intent(getBaseActivity(),CreateUserActivity.class);
                intent.putExtra(CreateUserFragment.ARG_ID_USER,user.getName());
                startActivity(intent);
                break;
        }
    }

    private Calendar getCalendarHour(int hour,int minute){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        return calendar;
    }
    private void showCreateUser(){
        startActivity(new Intent(getBaseActivity(), CreateUserActivity.class));
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
