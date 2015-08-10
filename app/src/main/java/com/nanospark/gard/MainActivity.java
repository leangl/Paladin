package com.nanospark.gard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.DoorState;
import com.nanospark.gard.events.DoorToggled;
import com.nanospark.gard.events.RecognizerLifecycle;
import com.nanospark.gard.scheluded.AlarmCloseReceiver;
import com.nanospark.gard.scheluded.AlarmOpenReceiver;
import com.nanospark.gard.scheluded.BaseAlarmReceiver;
import com.nanospark.gard.scheluded.BuilderWizardScheluded;
import com.nanospark.gard.scheluded.BuilderDialogs;
import com.nanospark.gard.scheluded.Scheluded;
import com.nanospark.gard.services.GarDService;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.activities.BaseActivity;
import mobi.tattu.utils.persistance.datastore.DataStore;
import roboguice.inject.InjectView;

/**
 * Created by Leandro on 19/7/2015.
 */
public class MainActivity extends BaseActivity implements BuilderWizardScheluded.BuilderWizardScheludedListener {

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

    public static final String SCHELUDED_ONE = "scheludedOne";
    public static final String SCHELUDED_TWO = "scheludedTwo";

    private AlarmOpenReceiver mAlarmOpenReceiver;
    private AlarmCloseReceiver mAlarmCloseReceiver;
    private ListView mScheludedOneListView;
    private ListView mScheludedTwoListView;

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

        mAlarmOpenReceiver = new AlarmOpenReceiver();
        mAlarmCloseReceiver = new AlarmCloseReceiver();
        registerReceiver(mAlarmOpenReceiver, new IntentFilter(BaseAlarmReceiver.ACTION_OPEN));
        registerReceiver(mAlarmCloseReceiver,new IntentFilter(BaseAlarmReceiver.ACTION_CLOSE));

        mThreshold.setText(DEFAULT_THRESHOLD + "");
        registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED));
        View buttonOne = findViewById(R.id.cardview);
        View buttonTwo = findViewById(R.id.cardview2);
        buttonOne.setOnClickListener(v -> handlerScheludedOne());
        buttonTwo.setOnClickListener(v -> handlerScheludedTwo());

        mScheludedOneListView = (ListView) findViewById(R.id.listView_one);
        mScheludedTwoListView = (ListView) findViewById(R.id.listView2_two);

        loadScheluded(BuilderWizardScheluded.ACTION_OPEN_DOOR);
        loadScheluded(BuilderWizardScheluded.ACTION_CLOSE_DOOR);
    }

    private void loadScheluded(String key) {
        try {
            Scheluded scheluded = DataStore.getInstance().getObject(key, Scheluded.class);
            populateListView(scheluded.name, scheluded);
        } catch (DataStore.ObjectNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    private void handlerScheludedOne() {
        BuilderWizardScheluded handlerScheluded = new BuilderWizardScheluded(this, SCHELUDED_ONE);
        handlerScheluded.setListener(this);
        BuilderDialogs.builderDesiredAction(this, handlerScheluded);

    }

    private void handlerScheludedTwo() {
        BuilderWizardScheluded handlerScheluded = new BuilderWizardScheluded(this, SCHELUDED_TWO);
        handlerScheluded.setListener(this);
        BuilderDialogs.builderDesiredAction(this, handlerScheluded);
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
        unregisterReceiver(mAlarmOpenReceiver);
        unregisterReceiver(mAlarmCloseReceiver);
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
    public void onSuccess(String id, Scheluded scheluded) {
        populateListView(id, scheluded);
    }

    private void populateListView(String id, Scheluded scheluded) {
        List<String> list = new ArrayList<>();
        String[] desiredActions = getResources().getStringArray(R.array.desiredActions);
        list.add(scheluded.action.contains("open") ? desiredActions[0] : desiredActions[1]);
        list.add(formattedHour(String.valueOf(scheluded.hourOfDay)) + ":" + formattedHour(String.valueOf(scheluded.minute)));
        list.add(scheluded.dayNameSelecteds.toString());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        ListView listView = null;
        switch (id) {
            case SCHELUDED_ONE:
                mScheludedOneListView.setAdapter(arrayAdapter);
                listView = mScheludedOneListView;
                break;
            case SCHELUDED_TWO:
                mScheludedTwoListView.setAdapter(arrayAdapter);
                listView = mScheludedTwoListView;
                break;

        }
        arrayAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listView);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private String formattedHour(String value) {
        if (value.length() == 1) {
            return "0" + value;
        }
        return value;
    }


}
