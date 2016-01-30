package com.nanospark.gard.ui.activity;

import android.support.v4.app.Fragment;

import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.fragments.LogFragment;

/**
 * Created by cristian on 10/10/15.
 */
public class LogActivity extends BaseActivity {

    @Override
    public Fragment getFragment() {
        return LogFragment.newInstance();
    }

}
