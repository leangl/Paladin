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
import com.nanospark.gard.events.PhraseRecognized;
import com.nanospark.gard.events.RecognizerLifecycle;
import com.nanospark.gard.services.VoiceRecognitionService;
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
    private Button mStart;
    @InjectView(R.id.stop)
    private Button mStop;
    @InjectView(R.id.stopped_content)
    private View mStoppedContent;
    @InjectView(R.id.started_content)
    private View mStartedContent;
    @InjectView(R.id.state)
    private TextView mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        mStart.setOnClickListener(v -> {
            start();
        });
        mStop.setOnClickListener(v -> {
            VoiceRecognitionService.stop();
        });

        mThreshold.setText(DEFAULT_THRESHOLD + "");

        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
    }

    @Subscribe
    public void onStateChange(RecognizerLifecycle.State state) {
        if (state.state == RecognizerLifecycle.State.STARTED) {
            stopLoading();
            mStoppedContent.setVisibility(View.GONE);
            mStartedContent.setVisibility(View.VISIBLE);
        } else if (state.state == RecognizerLifecycle.State.STOPPED || state.state == RecognizerLifecycle.State.ERROR) {
            stopLoading();
            mStoppedContent.setVisibility(View.VISIBLE);
            mStartedContent.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void on(PhraseRecognized event) {
        if (event.phrase.toLowerCase().equals(mOpen.getText().toString().toLowerCase())) {
            mState.setText(getString(R.string.door_open, mClose.getText().toString().toLowerCase()));
        } else {
            mState.setText(getString(R.string.door_closed, mOpen.getText().toString().toLowerCase()));
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

        VoiceRecognitionService.start((float) threshold, mOpen.getText().toString().toLowerCase(), mClose.getText().toString().toLowerCase());
        mState.setText(getString(R.string.door_closed, mOpen.getText().toString().toLowerCase()));
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
