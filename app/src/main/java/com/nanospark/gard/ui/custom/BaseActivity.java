package com.nanospark.gard.ui.custom;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.nanospark.gard.R;
import com.nanospark.gard.ui.fragments.MainFragment;
import com.nanospark.gard.ui.fragments.SchedulesFragment;
import com.nanospark.gard.ui.fragments.SettingsFragment;
import com.nanospark.gard.ui.fragments.UsersFragment;

/**
 * Created by cristian on 23/09/15.
 */
public abstract class BaseActivity extends mobi.tattu.utils.activities.BaseActivity implements TabLayout.OnTabSelectedListener {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_main));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_users));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_schedules));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_settings));

        this.mViewPager = (ViewPager) findViewById(R.id.view_pager);
        BasePagerAdapter basePagerAdapter = new BasePagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
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
            return  fragment;
        }

        @Override
        public int getCount() {
            return this.mCount;
        }
    }
}
