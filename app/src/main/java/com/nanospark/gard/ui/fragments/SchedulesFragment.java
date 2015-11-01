package com.nanospark.gard.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.scheduler.Schedule;
import com.nanospark.gard.model.scheduler.ScheduleManager;
import com.nanospark.gard.ui.activity.CreateScheduleActivity;
import com.nanospark.gard.ui.custom.BaseFragment;

import java.util.List;

import mobi.tattu.utils.ResourceUtils;
import roboguice.inject.InjectView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SchedulesFragment extends BaseFragment {

    private List<Schedule> mSchedules;

    @InjectView(R.id.grid)
    private GridLayout mGridLayout;
    @InjectView(R.id.add_schedule)
    private View mAddSchedule;

    @Inject
    private ScheduleManager mManager;

    public static SchedulesFragment newInstance() {
        return new SchedulesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedules, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAddSchedule.setOnClickListener(v -> CreateScheduleActivity.start(getActivity()));
        loadSchedules();
    }

    private void loadSchedules() {
        this.mGridLayout.removeAllViews();

        View autoCloseCard = inflate(R.layout.autoclose_card_layout, mGridLayout, false);
        ((TextView) autoCloseCard.findViewById(R.id.enabled)).setText(Door.isAutoCloseEnabled() ? R.string.enabled : R.string.disabled);
        ((TextView) autoCloseCard.findViewById(R.id.after_open)).setText(Door.getAutoCloseValue() + " " + ResourceUtils.toString(Door.getAutoCloseUnit()).toLowerCase());
        autoCloseCard.findViewById(R.id.imageview_menu).setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getBaseActivity(), v);
            popupMenu.inflate(R.menu.autoclose);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        toast("EDIT");
                        break;
                    case R.id.action_enable:
                        Door.enableAutoClose();
                        loadSchedules();
                        break;
                    case R.id.action_disable:
                        Door.disableAutoClose();
                        loadSchedules();
                        break;
                }
                return true;
            });
            popupMenu.getMenu().findItem(R.id.action_enable).setVisible(!Door.isAutoCloseEnabled());
            popupMenu.getMenu().findItem(R.id.action_disable).setVisible(Door.isAutoCloseEnabled());
            popupMenu.show();
        });
        addViewToGrid(this.mGridLayout, autoCloseCard);

        this.mSchedules = mManager.getAll();
        if (!mSchedules.isEmpty()) {
            for (Schedule schedule : mSchedules) {
                View cardView = inflate(R.layout.schedule_card_layout, mGridLayout, false);

                TextView name = (TextView) cardView.findViewById(R.id.name);
                TextView door = (TextView) cardView.findViewById(R.id.door);
                TextView openAt = (TextView) cardView.findViewById(R.id.open_at);
                TextView closeAt = (TextView) cardView.findViewById(R.id.close_at);
                TextView repeat = (TextView) cardView.findViewById(R.id.repeat);

                name.setText(schedule.getName());
                StringBuilder sb = new StringBuilder();
                for (Integer doorId : schedule.getDoors()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(Door.getInstance(doorId).getName());
                }
                door.setText(sb.toString());

                repeat.setText(schedule.getRepeat().toString());

                cardView.findViewById(R.id.imageview_menu).setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(getBaseActivity(), v);
                    popupMenu.inflate(R.menu.actions);
                    popupMenu.setOnMenuItemClickListener(item -> {
                        handlePopMenu(item, schedule);
                        return true;
                    });
                    popupMenu.show();
                });
                addViewToGrid(this.mGridLayout, cardView);
            }
        } else {
            View emptyView = LayoutInflater.from(getBaseActivity()).inflate(R.layout.empty_layout, null, false);
            TextView textView = (TextView) emptyView.findViewById(R.id.textview_empty);
            textView.setText(R.string.empty_schedules);
            addViewToGrid(mGridLayout, emptyView);
        }
    }

    private void handlePopMenu(MenuItem item, Schedule schedule) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mManager.delete(schedule);
                loadSchedules();
                break;
            case R.id.action_edit:
                CreateScheduleActivity.start(getActivity(), schedule);
                break;
        }
    }

    private void addViewToGrid(GridLayout gridLayout, View view) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.rowSpec = GridLayout.spec(1, Float.valueOf("1.0"));
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, Float.valueOf("1.0"));
        view.setLayoutParams(layoutParams);
        gridLayout.addView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchedules();
    }

    @Override
    public boolean showHomeIcon() {
        return false;
    }
}
