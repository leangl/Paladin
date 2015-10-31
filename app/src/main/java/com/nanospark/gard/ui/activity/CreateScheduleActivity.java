package com.nanospark.gard.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.nanospark.gard.R;
import com.nanospark.gard.model.scheduler.Schedule;
import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.fragments.CreateScheduleFragment;

/**
 * Created by cristian on 16/10/15.
 */
public class CreateScheduleActivity extends BaseActivity {

    public static final String ARG_SCHEDULE = "schedule";

    private CreateUserListener mListener;

    public static void start(Activity ctx) {
        start(ctx, null);
    }

    public static void start(Activity ctx, Schedule schedule) {
        Intent i = new Intent(ctx, CreateScheduleActivity.class);
        i.putExtra(ARG_SCHEDULE, schedule);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbar().setTitle(R.string.new_schedule_label);
        TextView textView = (TextView) getViewInToolbar(R.id.textview_menu_overflow);
        textView.setVisibility(View.VISIBLE);
        textView.setText(R.string.save_label);
        textView.setOnClickListener(v -> {
            if (mListener != null && mListener.save()) {
                finish();
            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    public boolean containsTab() {
        return false;
    }

    @Override
    public Fragment getFragment() {
        Schedule schedule = (Schedule) getIntent().getSerializableExtra(ARG_SCHEDULE);
        return CreateScheduleFragment.newInstance(schedule);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public interface CreateUserListener {
        boolean save();
    }

    public void setListener(CreateUserListener listener) {
        this.mListener = listener;
    }
}
