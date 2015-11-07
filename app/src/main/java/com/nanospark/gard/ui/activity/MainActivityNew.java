package com.nanospark.gard.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nanospark.gard.R;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.SmsSuspended;
import com.nanospark.gard.services.GarDService;
import com.nanospark.gard.sms.SmsManager;
import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.custom.DialogFragment;
import com.squareup.otto.Subscribe;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.ToastManager;

/**
 * Created by cristian on 23/09/15.
 */
public class MainActivityNew extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
    }

    @Override
    public int getLayout() {
        return R.layout.activity_main_tab;
    }

    @Override
    public boolean containsTab() {
        return true;
    }

    @Override
    public Fragment getFragment() {
        return null;
    }

    @Subscribe
    public void on(SmsSuspended smsSuspended) {
        DialogFragment dialogFragment = DialogFragment.newInstance(getString(R.string.sms_suspend_msg), getString(R.string.warning_label), true);
        dialogFragment.setDialogFragmentListener(new DialogFragment.DialogFragmentListener() {
            @Override
            public void onPositiveButton(DialogInterface dialog) {
                dialog.dismiss();
                SmsManager.getInstance().enableSms();
            }

            @Override
            public void onNegativeButton(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialogFragment.show(getSupportFragmentManager(), DialogFragment.class.getCanonicalName());
    }

    private void checkBoardConnected(Intent i) {
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(i.getAction())) {
            Tattu.post(new BoardConnected());
            ToastManager.show(R.string.board_connected_msg);
        }
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Tattu.post(new BoardDisconnected());
            ToastManager.show(R.string.board_disconnected_msg);
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkBoardConnected(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBoardConnected(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        GarDService.start(); // restart ioio
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }
}
