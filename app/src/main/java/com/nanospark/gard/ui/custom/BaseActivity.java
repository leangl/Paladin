package com.nanospark.gard.ui.custom;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.nanospark.gard.R;

import mobi.tattu.utils.ResourceUtils;
import roboguice.inject.ContentView;

/**
 * Created by cristian on 23/09/15.
 */
@ContentView(R.layout.activity_main)
public abstract class BaseActivity extends mobi.tattu.utils.activities.BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (mobi.tattu.utils.Utils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }*/

        this.mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (this.mToolbar != null) {
            this.mToolbar.setTitleTextColor(getColorFromResource(R.color.white));
            setSupportActionBar(this.mToolbar);
        }

        Fragment f = getFragment();
        if (f != null) {
            start(getFragment(), false);
        }

    }

    public Fragment getFragment() {
        return null;
    }

    public int getColorFromResource(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(color, getTheme());
        } else {
            return getResources().getColor(color);
        }
    }

    public void showHomeIcon() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setShowHideAnimationEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(ResourceUtils.getDrawableResources(this, R.drawable.ic_action_navigation_arrow_back));

        }
    }

    public Toolbar getToolbar() {
        return this.mToolbar;
    }

    public Object getViewInToolbar(int id) {
        return findViewById(id);
    }
}
