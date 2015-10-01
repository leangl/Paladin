package com.nanospark.gard.ui;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.twilio.TwilioAccount;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.DoorToggled;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.scheduler.DialogBuilder;
import com.nanospark.gard.model.scheduler.Schedule;
import com.nanospark.gard.model.scheduler.SchedulerWizard;
import com.nanospark.gard.services.GarDService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.inject.InjectView;

/**
 * Created by Leandro on 19/7/2015.
 */
public class MainActivity extends mobi.tattu.utils.activities.BaseActivity implements SchedulerWizard.BuilderWizardScheludedListener {

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
    @InjectView(R.id.twilio_phone)
    private EditText mTwilioPhone;
    @InjectView(R.id.twilio_token)
    private EditText mTwilioToken;
    @InjectView(R.id.twilio_account)
    private EditText mTwilioAccount;
    @InjectView(R.id.twilio_save)
    private View mTwilioSave;

    @Inject
    private Door.One mDoorOne;
    @Inject
    private Door.Two mDoorTwo;

    public static final String SCHEDULE_ONE = "scheduleOne";
    public static final String SCHEDULE_TWO = "scheduleTwo";

    private LinearLayout mScheduleOneContainer;
    private LinearLayout mScheduleTwoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        mDoorToggle.setOnClickListener(v -> {
            mDoorOne.toggle("Door is in motion");
        });
        mToggleVoiceControl.setOnClickListener(v -> {
            if (VoiceRecognizer.State.STARTED == VoiceRecognizer.getInstance().getCurrentState()) {
                GarDService.stopVoiceRecognition();
            } else {
                startVoiceRecognition();
            }
        });

        mOpen.setText(mDoorOne.getOpenPhrase());
        mClose.setText(mDoorOne.getClosePhrase());

        View buttonOne = findViewById(R.id.cardview);
        View buttonTwo = findViewById(R.id.cardview2);
        buttonOne.setOnClickListener(v -> handlerScheduledOne());
        buttonTwo.setOnClickListener(v -> handlerScheduledTwo());

        mScheduleOneContainer = (LinearLayout) findViewById(R.id.container_one);
        mScheduleTwoContainer = (LinearLayout) findViewById(R.id.container_two);

        loadSchedule(SCHEDULE_ONE);
        loadSchedule(SCHEDULE_TWO);

        TwilioAccount a = DataStore.getInstance().getObject(TwilioAccount.class.getSimpleName(), TwilioAccount.class).get();
        if (a != null) {
            mTwilioPhone.setText(a.getPhone());
            mTwilioAccount.setText(a.getSid());
            mTwilioToken.setText(a.getToken());
        }
        mTwilioSave.setOnClickListener(v -> saveTwilio());

        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
    }

    private void saveTwilio() {
        TwilioAccount account = new TwilioAccount();
        account.setPhone(mTwilioPhone.getText().toString());
        account.setSid(mTwilioAccount.getText().toString());
        account.setToken(mTwilioToken.getText().toString());

        DataStore.getInstance().putObject(TwilioAccount.class.getSimpleName(), account);

        if (account.isValid()) {
            toast("Account saved!");
        } else {
            toast("Twilio disabled!");
        }

    }

    private void loadSchedule(String key) {
        Schedule schedule = DataStore.getInstance().getObject(key, Schedule.class).get();
        if (schedule != null) {
            populateListView(schedule.name, schedule);
        }
    }

    private void handlerScheduledOne() {
        SchedulerWizard handlerScheduled = new SchedulerWizard(this, SCHEDULE_ONE);
        handlerScheduled.setListener(this);
        DialogBuilder.buildDesiredActions(this, handlerScheduled);

    }

    private void handlerScheduledTwo() {
        SchedulerWizard handlerScheduled = new SchedulerWizard(this, SCHEDULE_TWO);
        handlerScheduled.setListener(this);
        DialogBuilder.buildDesiredActions(this, handlerScheduled);
    }

    @Subscribe
    public void onStateChange(VoiceRecognizer.State state) {
        if (state == VoiceRecognizer.State.STARTED) {
            stopLoading();
            mToggleVoiceControl.setText("Stop");
            mThreshold.setEnabled(false);
            mOpen.setEnabled(false);
            mClose.setEnabled(false);
        } else if (state == VoiceRecognizer.State.STOPPED || state == VoiceRecognizer.State.ERROR) {
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
        mDoorToggle.setEnabled(true);
    }

    @Subscribe
    public void on(BoardConnected event) {
        mBoardLed.setImageResource(R.drawable.led_on);
    }

    @Subscribe
    public void on(BoardDisconnected event) {
        mBoardLed.setImageResource(R.drawable.led_off);
    }

    private void startVoiceRecognition() {
        showLoading(false, "Starting voice recognition...");
        int level = -40;
        try {
            level = Integer.parseInt(mThreshold.getText().toString());
        } catch (NumberFormatException e) {
            mThreshold.setText(level + "");
            toast("Wrong threshold format! Using default value: -40");
        }
        String openPhrase = mOpen.getText().toString().toLowerCase();
        String closePhrase = mClose.getText().toString().toLowerCase();

        mDoorOne.setOpenPhrase(openPhrase);
        mDoorOne.setClosePhrase(closePhrase);

        double threshold = Math.pow(10, level);
        GarDService.startVoiceRecognition((float) threshold);
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

    @Override
    public void onSuccess(String id, Schedule scheduled) {
        populateListView(id, scheduled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GarDService.start(); // restart ioio
    }

    private void populateListView(String id, Schedule scheduled) {
        List<String> list = new ArrayList<>();
        String[] desiredActions = getResources().getStringArray(R.array.desiredActions);
        list.add(scheduled.action.contains("open") ? desiredActions[0] : desiredActions[1]);
        list.add(formattedHour(String.valueOf(scheduled.hourOfDay)) + ":" + formattedHour(String.valueOf(scheduled.minute)));
        list.add(scheduled.dayNameSelecteds.toString());
        switch (id) {
            case SCHEDULE_ONE:
                addTextViewContainer(list, mScheduleOneContainer);
                break;
            case SCHEDULE_TWO:
                addTextViewContainer(list, mScheduleTwoContainer);
                break;
        }
    }

    private void addTextViewContainer(List<String> list, LinearLayout container) {
        container.removeAllViews();
        for (String text : list) {
            container.addView(createTextView(text));
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        return textView;
    }

    private String formattedHour(String value) {
        if (value.length() == 1) {
            return "0" + value;
        }
        return value;
    }

}
