package com.nanospark.gard.ui.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.CommandFailed;
import com.nanospark.gard.events.CommandProcessed;
import com.nanospark.gard.events.CommandSent;
import com.nanospark.gard.events.DoorStateChanged;
import com.nanospark.gard.model.CommandSource;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.log.Log;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.nanospark.gard.voice.VoiceRecognizer;
import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.Locale;

import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.annotations.SaveState;
import roboguice.inject.InjectView;

/**
 * Created by cristian on 07/10/15.
 */
public class DoorCardFragment extends BaseFragment {

    @InjectView(R.id.door_container)
    private View mContainer;
    @InjectView(R.id.textview_title_door)
    private TextView mDoorName;
    @InjectView(R.id.textview_open)
    private TextView mTextviewOpen;
    @InjectView(R.id.imageview_icon_sound)
    private ImageView mImageViewVoice;
    @InjectView(R.id.imageview_icon_door)
    private ImageView mImageViewDoor;
    @InjectView(R.id.edittext_open_voice)
    private EditText mEditTextOpen;
    @InjectView(R.id.edittext_close_voice)
    private EditText mEditTextClose;
    @InjectView(R.id.textView9)
    private TextView mLastOpenedlabel;
    @InjectView(R.id.textview_last_opened)
    private TextView mTextViewLastOpened;
    @InjectView(R.id.container_center)
    private View mContainerCenter;

    @Inject
    private LogManager mLogManager;

    @SaveState
    private Integer mDoorId;

    private Door mDoor;
    private long animationStarted;
    private Handler mAnimationStopHandler = Tattu.createUiHandler();

    public static DoorCardFragment newInstance(Integer doorId) {
        DoorCardFragment instance = new DoorCardFragment();
        instance.mDoorId = doorId;
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.door_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDoor = Door.getInstance(mDoorId);
        mImageViewVoice.setOnClickListener(v -> {
            mDoor.setOpenPhrase(mEditTextOpen.getText().toString());
            mDoor.setClosePhrase(mEditTextClose.getText().toString());
            if (VoiceRecognizer.State.STARTED.equals(VoiceRecognizer.getInstance().getCurrentState())) {
                mDoor.disableVoiceRecognition();
            } else {
                showLoading(false, R.string.start_voice_recognition_msg);
                mDoor.enableVoiceRecognition();
            }
        });
        mImageViewDoor.setOnClickListener(v -> {
            mDoor.send(new Door.Toggle(CommandSource.TOUCH, "Door is in motion", true));
        });
        refresh();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDoor.isReady()) {
            refreshState(mDoor);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (!mDoor.isEnabled()) {
            mContainer.setVisibility(View.GONE);
        } else {
            mContainer.setVisibility(View.VISIBLE);
            mDoorName.setText(mDoor.getName());
            refreshState();
            refreshVoiceState();
        }
    }

    public void refreshState(Door door) {
        if (door.equals(mDoor)) {
            refreshState();
        }
    }

    private void refreshState() {
        mTextviewOpen.setText(mDoor.getState().toString());
        mTextviewOpen.setTextColor(getResources().getColor(!mDoor.isReady() ? R.color.door_unknown : mDoor.isOpen() ? R.color.door_open : R.color.door_closed));
        if (mDoor.isReady()) {
            refreshDoorImage();
        }
        mContainerCenter.setBackgroundColor(getResources().getColor(!mDoor.isReady() ? R.color.door_unknown : mDoor.isOpen() ? R.color.door_open : R.color.door_closed));

        refreshLastOpened();
    }

    private void refreshDoorImage() {
        long now = System.currentTimeMillis();
        if (now - animationStarted > 5000) {
            setDoorBackground();
        } else {
            stopDoorAnimationIn(now - animationStarted);
        }
    }

    private void setDoorBackground() {
        mImageViewDoor.setBackgroundResource(mDoor.isOpen() ? R.drawable.door_0 : R.drawable.door_4);
    }

    private void startAnimation(int drawableResId) {
        animationStarted = System.currentTimeMillis();
        mImageViewDoor.setBackgroundResource(drawableResId);
        AnimationDrawable animation = (AnimationDrawable) mImageViewDoor.getBackground();
        animation.start();

        stopDoorAnimationIn(30);
    }

    private void stopDoorAnimationIn(long millis) {
        mAnimationStopHandler.removeCallbacks(null);
        mAnimationStopHandler.postDelayed(() -> setDoorBackground(), millis);
    }

    private void refreshLastOpened() {
        Log lastLog = mLogManager.getLastLog(mDoor);
        if (lastLog != null) {
            mTextViewLastOpened.setVisibility(View.VISIBLE);
            mLastOpenedlabel.setVisibility(View.VISIBLE);
            mTextViewLastOpened.setText(getLogString(lastLog));
        } else {
            mTextViewLastOpened.setVisibility(View.INVISIBLE);
            mLastOpenedlabel.setVisibility(View.INVISIBLE);
        }
    }

    public String getLogString(Log log) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(log.getDate());
        String dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        StringBuilder builder = new StringBuilder();

        builder.append(log.getEvent().equals(Door.State.OPEN) ? "Opened" : "Closed");
        builder.append(", ");
        builder.append(dayOfWeek);
        builder.append(" ");
        builder.append(dayOfMonth);
        builder.append("/");
        builder.append(month);
        builder.append(" at ");
        builder.append(com.nanospark.gard.Utils.getHour(calendar));

        return Html.fromHtml(builder.toString()).toString();
    }

    public void refreshVoiceState() {
        stopLoading();
        if (mDoor.isVoiceEnabled()) {
            mImageViewVoice.setImageResource(R.drawable.ic_sound_);
        } else {
            if (mImageViewVoice != null) {
                mImageViewVoice.setImageResource(R.drawable.ic_no_sound_);
            }
        }
        mEditTextOpen.setText(mDoor.getOpenPhrase());
        mEditTextClose.setText(mDoor.getClosePhrase());
    }

    @Override
    public boolean showHomeIcon() {
        return false;
    }

    @Subscribe
    public void on(BoardConnected event) {
        refreshState(mDoor);
    }

    @Subscribe
    public void on(BoardDisconnected boardDisconnected) {
        refreshState(mDoor);
        mImageViewDoor.setBackgroundResource(R.drawable.door_4);
    }

    @Subscribe
    public void on(VoiceRecognizer.StateChanged event) {
        refreshVoiceState();
    }

    @Subscribe
    public void on(CommandProcessed event) {
        refreshState(event.door);
    }

    @Subscribe
    public void on(DoorStateChanged event) {
        refreshState(event.door);
    }

    @Subscribe
    public void on(CommandSent event) {
        if (event.door.equals(mDoor)) {
            if (event.command instanceof Door.Open) {
                startAnimation(R.drawable.door_open_animation);
            } else if (event.command instanceof Door.Close) {
                startAnimation(R.drawable.door_close_animation);
            }
        }
    }

    @Subscribe
    public void on(CommandFailed doorActivationFailed) {
        if (mDoor.equals(doorActivationFailed.door)) {
            refreshState();
        }
    }
}
