package com.nanospark.gard.ui.custom;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.ui.activity.LogActivity;
import com.nanospark.gard.ui.fragments.MainFragment;
import com.nanospark.gard.ui.fragments.SchedulesFragment;
import com.nanospark.gard.ui.fragments.SettingsFragment;
import com.nanospark.gard.ui.fragments.UsersFragment;

import mobi.tattu.utils.ToastManager;
import mobi.tattu.utils.Utils;

/**
 * Created by cristian on 23/09/15.
 */
public abstract class BaseActivity extends mobi.tattu.utils.activities.BaseActivity implements TabLayout.OnTabSelectedListener {

    private ViewPager mViewPager;

    @Inject
    private LogManager mLogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_toolbar);
        toolbar.setSubtitle(R.string.subtile_toolbar);
        toolbar.setSubtitleTextColor(getColorFromResource(R.color.white));
        toolbar.setTitleTextColor(getColorFromResource(R.color.white));
        setSupportActionBar(toolbar);

        if (containsTab()) {
            initTabs();
        }

    }

    public abstract int getLayout();

    public abstract boolean containsTab();

    private void initTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.setTabTextColors(getResources().getColorStateList(R.color.tab_selector));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_main));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_users));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_schedules));


        this.mViewPager = (ViewPager) findViewById(R.id.view_pager);
        BasePagerAdapter basePagerAdapter = new BasePagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        this.mViewPager.setAdapter(basePagerAdapter);
        this.mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        this.mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private class BasePagerAdapter extends FragmentStatePagerAdapter {
        private int mCount;
        private final int MAIN_FRAGMENT = 0;
        private final int USERS_FRAGMENT = 1;
        private final int SCHEDULES_FRAGMENT = 2;
        private final int SETTINGS_FRAGMENT = 3;

        public BasePagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.mCount = count;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case MAIN_FRAGMENT:
                    fragment = MainFragment.newInstance();
                    break;
                case USERS_FRAGMENT:
                    fragment = UsersFragment.newInstance();
                    break;
                case SCHEDULES_FRAGMENT:
                    fragment = SchedulesFragment.newInstance();
                    break;
                case SETTINGS_FRAGMENT:
                    fragment = SettingsFragment.newInstance();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return this.mCount;
        }
    }


    /**
     * @param color del R.color.blue
     * @return color tomado del resources
     */
    public int getColorFromResource(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(color, getTheme());
        } else {
            return getResources().getColor(color);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return createMenu(menu);
    }

    public boolean createMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log:
                if (mLogManager.getLogs().isEmpty()) {
                    ToastManager.get().showToast(R.string.no_record_msg);
                } else {
                    startActivity(new Intent(this, LogActivity.class));
                }
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;

    }

    public void showHomeIcon() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setShowHideAnimationEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(Utils.getDrawableResources(this, R.drawable.ic_action_navigation_arrow_back));

        }

    }
}
