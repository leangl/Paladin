package com.nanospark.gard.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.BuildConfig;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.SmsSuspended;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.services.GarDService;
import com.nanospark.gard.sms.SmsManager;
import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.custom.DialogFragment;
import com.nanospark.gard.ui.fragments.MainFragment;
import com.nanospark.gard.ui.fragments.SchedulesFragment;
import com.nanospark.gard.ui.fragments.UsersFragment;
import com.squareup.otto.Subscribe;

import mobi.tattu.utils.DialogUtils;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by cristian on 23/09/15.
 */
@ContentView(R.layout.activity_main_tab)
public class MainActivityNew extends BaseActivity implements TabLayout.OnTabSelectedListener {

    @InjectView(R.id.view_pager)
    private ViewPager mViewPager;
    @InjectView(R.id.logo_text)
    private ImageView mLogoText;
    @Inject
    private LogManager mLogManager;
    @Inject
    private SmsManager mSmsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mUsbDetachReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
        getSupportActionBar().setIcon(R.drawable.logo_white);
        initTabs();

        mLogoText.setOnClickListener(v -> {
            if (BuildConfig.DEBUG) fakeSms();
            Utils.saveLogcat();
        });

        GarDService.start(); // start ioio
    }

    private void fakeSms() {
        DialogUtils.ask(this, "Fake SMS", fakeSmsText -> {
            mSmsManager.fakeSms(fakeSmsText);
            return true;
        });
    }

    @Subscribe
    public void on(SmsSuspended smsSuspended) {
        DialogFragment dialogFragment = DialogFragment.newInstance(getString(R.string.sms_suspend_msg), getString(R.string.warning_label), true);
        dialogFragment.setDialogFragmentListener(new DialogFragment.DialogFragmentListener() {
            @Override
            public void onPositiveButton(DialogInterface dialog) {
                dialog.dismiss();
                SmsManager.getInstance().resumeSms();
            }

            @Override
            public void onNegativeButton(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialogFragment.show(getSupportFragmentManager(), DialogFragment.class.getCanonicalName());
    }

    @Subscribe
    public void on(BoardConnected event) {
        getSupportActionBar().setIcon(R.drawable.logo_white);
    }

    @Subscribe
    public void on(BoardDisconnected event) {
        getSupportActionBar().setIcon(R.drawable.logo_grey);
    }

    private void checkBoardConnected(Intent i) {
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(i.getAction())) {
            Tattu.post(new BoardConnected());
            //ToastManager.show(R.string.board_connected_msg);
        }
    }

    BroadcastReceiver mUsbDetachReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Tattu.post(new BoardDisconnected());
            //ToastManager.show(R.string.board_disconnected_msg);
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkBoardConnected(intent);
        GarDService.restart(); // restart ioio
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBoardConnected(getIntent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbDetachReceiver);
    }

    private void initTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.setTabTextColors(getResources().getColorStateList(R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_main));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_users));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.title_schedules));

        BasePagerAdapter basePagerAdapter = new BasePagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        mViewPager.setAdapter(basePagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
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

        public BasePagerAdapter(FragmentManager fm, int count) {
            super(fm);
            mCount = count;
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
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return mCount;
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
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.about:
                showAbout();
                break;
            case R.id.action_log:
                if (mLogManager.getLogs().isEmpty()) {
                    ToastManager.show(R.string.no_record_msg);
                } else {
                    startActivity(new Intent(this, LogActivity.class));
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showAbout() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(getLayoutInflater().inflate(R.layout.about, null))
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .show();

        TextView versionText = (TextView) dialog.findViewById(R.id.version);
        versionText.setText(getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

}
