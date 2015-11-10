package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
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
import com.nanospark.gard.events.DoorStateChanged;
import com.nanospark.gard.voice.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.log.Log;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.squareup.otto.Subscribe;

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

    @Inject
    private LogManager mLogManager;

    @SaveState
    private Integer mDoorId;

    private Door mDoor;

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
            mDoor.send(new Door.Toggle("Door is in motion", true));
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
            mEditTextOpen.setText(mDoor.getOpenPhrase());
            mEditTextClose.setText(mDoor.getClosePhrase());
            setLastOpened();
            refreshVoiceState();
        }
    }

    public void refreshState(Door door) {
        if (mDoor.getId() == door.getId()) {
            refreshState();
        }
    }

    private void refreshState() {
        mTextviewOpen.setText(mDoor.getState().toString());
        mImageViewDoor.setImageResource(mDoor.isOpen() ? R.drawable.ic_door_opened : R.drawable.ic_door_closed);
    }

    private void setLastOpened() {
        Log mLastLog = mLogManager.getLastLog(mDoor);
        if (mLastLog != null) {
            this.mTextViewLastOpened.setVisibility(View.VISIBLE);
            mLastOpenedlabel.setVisibility(View.VISIBLE);
            this.mTextViewLastOpened.setText(mLastLog.getDateString(true));
        } else {
            this.mTextViewLastOpened.setVisibility(View.INVISIBLE);
            mLastOpenedlabel.setVisibility(View.INVISIBLE);
        }
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
    }

    public Door getDoor() {
        return mDoor;
    }

    @Override
    public boolean showHomeIcon() {
        return false;
    }

    @Subscribe
    public void on(BoardConnected event) {
        this.refreshState(mDoor);
    }

    @Subscribe
    public void on(BoardDisconnected boardDisconnected) {
        this.refreshState(mDoor);
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
    public void on(CommandFailed doorActivationFailed) {
        if (mDoor.getId() == mDoor.getId()) {
            refreshState();
        }
    }
}