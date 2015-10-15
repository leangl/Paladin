package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.DoorActivationFailed;
import com.nanospark.gard.events.DoorToggled;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.model.log.Log;
import com.nanospark.gard.model.log.LogManager;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.squareup.otto.Subscribe;

import java.util.Calendar;

/**
 * Created by cristian on 07/10/15.
 */
public abstract class BaseDoorFragment extends BaseFragment {

    private ImageView mImageViewVoice;
    private ImageView mImageViewDoor;
    private TextView mTextviewOpen;
    private EditText mEditTextOpen;
    private EditText mEditTextClose;
    private CardView mCardView;
    private TextView mTextViewLastOpened;

    @Inject
    private LogManager mLogManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.door_layout, container, false);
        this.mCardView = (CardView) view.findViewById(R.id.cardview_door);
        init();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDoor().isReady()) {
            this.handlerDoorState(new DoorToggled(getDoor(), getDoor().isOpened()));
        }
    }

    private void init() {
        this.mTextviewOpen = (TextView) mCardView.findViewById(R.id.textview_open);
        this.mImageViewDoor = (ImageView) mCardView.findViewById(R.id.imageview_icon_door);
        this.mTextViewLastOpened = (TextView) mCardView.findViewById(R.id.textview_last_opened);

        this.mImageViewVoice = (ImageView) mCardView.findViewById(R.id.imageview_icon_sound);
        this.mEditTextOpen = (EditText) mCardView.findViewById(R.id.edittext_open_voice);
        this.mEditTextClose = (EditText) mCardView.findViewById(R.id.edittext_close_voice);

        this.mImageViewVoice.setOnClickListener(v -> {
            mImageViewVoice = (ImageView) v;
            if (VoiceRecognizer.State.STARTED == VoiceRecognizer.getInstance().getCurrentState()) {
                getDoor().disableVoiceRecognition();
            } else {
                showLoading(false, R.string.start_voice_recognition_msg);
                getDoor().enableVoiceRecognition();
            }
        });
        this.mEditTextOpen.setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            getDoor().setOpenPhrase(editText.getText().toString());
        });
        this.mEditTextClose.setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            getDoor().setOpenPhrase(editText.getText().toString());
        });
        this.mImageViewDoor.setOnClickListener(v -> {
            getDoor().toggle("Door is in motion", true);
        });

        stateView();
        this.mEditTextOpen.setText(getDoor().getOpenPhrase());
        this.mEditTextClose.setText(getDoor().getClosePhrase());
        setTextViewLastOpened();
    }

    private void stateView() {
        if (getDoor().isReady()) {
            if (getDoor().isOpened()) {
                defaultView(R.string.opened_label, R.drawable.ic_door_opened);
            } else {
                defaultView(R.string.closed_label, R.drawable.ic_door_closed);
            }
        } else {
            defaultView(R.string.unknown_label, R.drawable.ic_door_opened);
        }
    }

    private void defaultView(int text, int drawable) {
        if (mTextviewOpen != null) {
            mTextviewOpen.setText(text);
            mImageViewDoor.setImageResource(drawable);
        }
    }

    private void setTextViewLastOpened() {
        Log mLastLog = mLogManager.getLastLog(getDoor());
        if (mLastLog != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mLastLog.getDate());
            this.mTextViewLastOpened.setText(Html.fromHtml(Utils.getDateLog(calendar, true).toString()));
        } else {
            this.mTextViewLastOpened.setVisibility(View.INVISIBLE);
        }
    }

    public void handlerVoiceState(VoiceRecognizer.State state) {
        if (state == VoiceRecognizer.State.STARTED) {
            mImageViewVoice.setImageResource(R.drawable.ic_sound_);
            stopLoading();
        } else if (state == VoiceRecognizer.State.STOPPED || state == VoiceRecognizer.State.ERROR) {
            if (mImageViewVoice != null) {
                mImageViewVoice.setImageResource(R.drawable.ic_no_sound_);
            }
            stopLoading();
        }
    }

    public void handlerDoorState(DoorToggled event) {
        if (getDoor().getId() == event.door.getId()) {
            stateView();
        }
    }

    public void handlerDoorState(DoorActivationFailed doorActivationFailed) {
        if (getDoor().getId() == doorActivationFailed.door.getId()) {
            stateView();
        }
    }

    public abstract Door getDoor();

    public CardView getCardView() {
        return this.mCardView;
    }

    @Override
    public boolean showHomeIcon() {
        return false;
    }

    @Subscribe
    public void on(BoardConnected boardConnected) {
        this.handlerDoorState(new DoorToggled(getDoor(), getDoor().isOpened()));
    }

    @Subscribe
    public void on(BoardDisconnected boardDisconnected) {
        this.handlerDoorState(new DoorToggled(getDoor(), getDoor().isOpened()));
    }

    @Subscribe
    public void on(VoiceRecognizer.State state) {
        handlerVoiceState(state);
    }

    @Subscribe
    public void on(DoorToggled event) {
        handlerDoorState(event);
    }

    @Subscribe
    public void on(DoorActivationFailed doorActivationFailed) {
        handlerDoorState(doorActivationFailed);
    }
}
