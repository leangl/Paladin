package com.nanospark.gard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.DoorState;
import com.nanospark.gard.events.DoorToggled;
import com.nanospark.gard.events.RecognizerLifecycle;
import com.nanospark.gard.services.GarDService;
import com.squareup.otto.Subscribe;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.activities.BaseActivity;
import roboguice.inject.InjectView;

/**
 * Created by Leandro on 19/7/2015.
 */
public class MainActivity extends BaseActivity {

    public static final int DEFAULT_THRESHOLD = -40;

    @InjectView(R.id.board_led)
    private ImageView mBoardLed;
    @InjectView(R.id.threshold)
    private EditText mThreshold;
    @InjectView(R.id.open)
    private EditText mOpen;
    @InjectView(R.id.close)
    private EditText mClose;
    @InjectView(R.id.start)
    private Button mToggleVoiceControl;
    @InjectView(R.id.door_state)
    private TextView mDoorState;
    @InjectView(R.id.toggle)
    private View mDoorToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        mDoorToggle.setOnClickListener(v -> {
            DoorState.getInstance().toggle();
        });
        mToggleVoiceControl.setOnClickListener(v -> {
            if (RecognizerLifecycle.State.STARTED == RecognizerLifecycle.getInstance().getCurrentState().state) {
                GarDService.stopVoiceRecognition();
            } else {
                start();
            }
        });

        mThreshold.setText(DEFAULT_THRESHOLD + "");

        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
    }

    @Subscribe
    public void onStateChange(RecognizerLifecycle.State state) {
        if (state.state == RecognizerLifecycle.State.STARTED) {
            stopLoading();
            mToggleVoiceControl.setText("Stop");
            mThreshold.setEnabled(false);
            mOpen.setEnabled(false);
            mClose.setEnabled(false);
        } else if (state.state == RecognizerLifecycle.State.STOPPED || state.state == RecognizerLifecycle.State.ERROR) {
            stopLoading();
            mToggleVoiceControl.setText("Start");
            mThreshold.setEnabled(true);
            mOpen.setEnabled(true);
            mClose.setEnabled(true);
        }
    }

    @Subscribe
    public void on(DoorToggled event) {
        if (event.opened) {
            mDoorState.setText("OPEN");
        } else {
            mDoorState.setText("CLOSED");
        }
    }

    @Subscribe
    public void on(BoardConnected event) {
        mBoardLed.setImageResource(R.drawable.led_green);
    }

    @Subscribe
    public void on(BoardDisconnected event) {
        mBoardLed.setImageResource(R.drawable.led_off);
    }

    public void start() {
        showLoading(false, "Starting voice recognition...");
        int power = DEFAULT_THRESHOLD;
        try {
            power = Integer.parseInt(mThreshold.getText().toString());
        } catch (NumberFormatException e) {
            toast("Wrong threshold format! Using default value: 40");
        }
        double threshold = Math.pow(10, power);

        GarDService.startVoiceRecognition((float) threshold, mOpen.getText().toString().toLowerCase(), mClose.getText().toString().toLowerCase());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBoardConnected(getIntent());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkBoardConnected(intent);
        setIntent(intent);
    }

    public void toast(String message) {
        Tattu.runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Tattu.post(new BoardDisconnected());
        }
    };

    private void checkBoardConnected(Intent i) {
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(i.getAction())) {
            Tattu.post(new BoardConnected());
        }
    }

}
