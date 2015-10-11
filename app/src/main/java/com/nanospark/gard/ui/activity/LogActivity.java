package com.nanospark.gard.ui.activity;

import android.os.Bundle;
import android.view.Menu;

import com.nanospark.gard.R;
import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.fragments.LogFragment;

/**
 * Created by cristian on 10/10/15.
 */
public class LogActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start(LogFragment.newInstance(),false);
        showHomeIcon();
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
    public boolean createMenu(Menu menu) {
        return true;
    }
}
