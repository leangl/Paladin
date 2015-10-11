package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanospark.gard.R;
import com.nanospark.gard.events.DoorActivated;
import com.nanospark.gard.events.DoorActivationFailed;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.ui.custom.BaseFragment;

/**
 * Created by cristian on 07/10/15.
 */
public abstract class BaseDoorFragment extends BaseFragment {


    private ImageView mImageViewVoice;
    private ImageView mImageViewDoor;
    private TextView mTextviewOpen;
    private SwitchCompat mSwitchCompat;
    private EditText mEditTextOpen;
    private EditText mEditTextClose;
    private boolean mDoorOpened;
    private CardView mCardView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.door_layout, container, false);
        mCardView = (CardView) view.findViewById(R.id.cardview_door);
        init();
        return view;

    }

    private void init() {
        mTextviewOpen = (TextView) mCardView.findViewById(R.id.textview_open);
        mImageViewDoor = (ImageView) mCardView.findViewById(R.id.imageview_icon_door);

        mSwitchCompat = (SwitchCompat) mCardView.findViewById(R.id.switch_open);
        mImageViewVoice = (ImageView) mCardView.findViewById(R.id.imageview_icon_sound);
        mEditTextOpen = (EditText) mCardView.findViewById(R.id.edittext_open_voice);
        mEditTextClose = (EditText) mCardView.findViewById(R.id.edittext_close_voice);

        mImageViewVoice.setOnClickListener(v -> {
            mImageViewVoice = (ImageView) v;
            if (VoiceRecognizer.State.STARTED == VoiceRecognizer.getInstance().getCurrentState()) {
                getDoor().disableVoiceRecognition();
            } else {
                showLoading(false, R.string.start_voice_recognition_label);
                getDoor().enableVoiceRecognition();

            }
        });
        mEditTextOpen.setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            getDoor().setOpenPhrase(editText.getText().toString());
        });
        mEditTextClose.setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            getDoor().setOpenPhrase(editText.getText().toString());
        });

        mSwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showLoading(false, R.string.opened_label);
                getDoor().open(getString(R.string.opened_label), true);
                defaultView(R.string.opened_label, R.drawable.door_open, true);
            } else {
                getDoor().close(getString(R.string.closed_label), true);
                defaultView(R.string.closed_label, R.drawable.door_closed, false);

            }
        });

        if (mDoorOpened) {
            defaultView(R.string.opened_label, R.drawable.door_open, mDoorOpened);
        } else {
            defaultView(R.string.close_label, R.drawable.door_closed, false);

        }
        mEditTextOpen.setText(getDoor().getOpenPhrase());
        mEditTextClose.setText(getDoor().getClosePhrase());



    }

    private void defaultView(int text, int drawable, boolean checked) {
        if (mTextviewOpen != null) {
            mTextviewOpen.setText(text);
            mImageViewDoor.setImageResource(drawable);
            mSwitchCompat.setChecked(checked);
        }
    }


    public void handlerVoiceState(VoiceRecognizer.State state) {
        if ((getDoor() != null && state.door != null) && getDoor().getId() == state.door.getId()) {
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
    }


    public void handlerDoorState(DoorActivated doorActivated) {
        if (getDoor() != null && getDoor().getId() == doorActivated.door.getId()) {
            mDoorOpened = doorActivated.opened;
            stopLoading();
            if (mDoorOpened) {
                defaultView(R.string.opened_label, R.drawable.door_open, true);
            } else {
                defaultView(R.string.closed_label, R.drawable.door_closed, false);
            }
        }
    }

    public void handlerDoorState(DoorActivationFailed doorActivationFailed) {
        if (getDoor() != null && getDoor().getId() == doorActivationFailed.door.getId()) {
            stopLoading();
            mDoorOpened = doorActivationFailed.opened;
            if (mDoorOpened) {
                defaultView(R.string.opened_label, R.drawable.door_open, true);
            } else {
                defaultView(R.string.closed_label, R.drawable.door_closed, false);
            }
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
}
