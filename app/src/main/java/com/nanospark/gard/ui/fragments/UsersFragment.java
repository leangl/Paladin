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
import com.nanospark.gard.model.user.User;
import com.nanospark.gard.model.user.UserManager;
import com.nanospark.gard.ui.activity.CreateUserActivity;
import com.nanospark.gard.ui.custom.BaseFragment;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends BaseFragment {

    @Inject
    private UserManager mUserManager;
    private GridLayout mGridLayout;
    private List<User> mUserList;

    public static UsersFragment newInstance() {
        return new UsersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        this.mGridLayout = (GridLayout) view.findViewById(R.id.gridlayout);
        this.mGridLayout.setUseDefaultMargins(true);
        if (!mobi.tattu.utils.Utils.isTablet(getActivity())) {
            mGridLayout.setColumnCount(1);
        }
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
        this.mUserList = mUserManager.getAll();
        int size = mUserList.size();
        if (size > 0) {
            this.mGridLayout.removeAllViews();
            for (int i = 0; i < size; i++) {
                View userView = LayoutInflater.from(getBaseActivity()).inflate(R.layout.user_layout, this.mGridLayout, false);
                User user = mUserList.get(i);

                TextView name = (TextView) userView.findViewById(R.id.textview_user_name);
                TextView phone = (TextView) userView.findViewById(R.id.textview_phone);
                TextView timeLimits1 = (TextView) userView.findViewById(R.id.textview_time_limits1);
                TextView timeLimits2 = (TextView) userView.findViewById(R.id.textview_time_limits2);
                ImageView receiveAlerts = (ImageView) userView.findViewById(R.id.imageview_receive_alerts);
                userView.findViewById(R.id.imageview_menu).setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(getBaseActivity(), v);
                    popupMenu.inflate(R.menu.actions);
                    popupMenu.setOnMenuItemClickListener(item -> {
                        handlePopMenu(item, user);
                        return true;
                    });
                    popupMenu.show();
                });
                if (user.isNotificationEnabled()) {
                    receiveAlerts.setImageResource(R.drawable.ic_alert_enabled);
                }
                timeLimits1.setText(user.getHourRangeString());
                timeLimits2.setText(user.getDayLimitString());
                populateUserView(user, name, phone);

                addViewToGrid(this.mGridLayout, userView);
            }
        } else {
            View emptyView = LayoutInflater.from(getBaseActivity()).inflate(R.layout.empty_layout, null, false);
            TextView textView = (TextView) emptyView.findViewById(R.id.textview_empty);
            textView.setText(R.string.empty_users_msg);
            mGridLayout.removeAllViews();
            addViewToGrid(mGridLayout, emptyView);

        }
    }

    private void handlePopMenu(MenuItem item, User user) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mUserManager.delete(user);
                loadUsers();
                break;
            case R.id.action_edit:
                Intent intent = new Intent(getBaseActivity(), CreateUserActivity.class);
                intent.putExtra(CreateUserFragment.ARG_ID_USER, user.getId());
                startActivity(intent);
                break;
        }
    }

    private void showCreateUser() {
        startActivity(new Intent(getBaseActivity(), CreateUserActivity.class));
    }

    private void populateUserView(User user, TextView name, TextView phone) {
        name.setText(user.getName());
        phone.setText(user.getPhone());
    }

    private void addViewToGrid(GridLayout gridLayout, View view) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(1, Float.valueOf("1.0"));
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, Float.valueOf("1.0"));
        view.setLayoutParams(layoutParams);
        gridLayout.addView(view);
    }

    @Override
    public boolean showHomeIcon() {
        return false;
    }
}
