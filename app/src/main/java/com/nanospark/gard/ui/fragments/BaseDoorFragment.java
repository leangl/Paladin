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
    private TextView mTextViewLastOpened;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.door_layout, container, false);
        this.mCardView = (CardView) view.findViewById(R.id.cardview_door);
        init();
        return view;

    }

    private void init() {
        this.mTextviewOpen = (TextView) mCardView.findViewById(R.id.textview_open);
        this.mImageViewDoor = (ImageView) mCardView.findViewById(R.id.imageview_icon_door);

        this.mSwitchCompat = (SwitchCompat) mCardView.findViewById(R.id.switch_open);
        this.mImageViewVoice = (ImageView) mCardView.findViewById(R.id.imageview_icon_sound);
        this.mEditTextOpen = (EditText) mCardView.findViewById(R.id.edittext_open_voice);
        this.mEditTextClose = (EditText) mCardView.findViewById(R.id.edittext_close_voice);

        this.mImageViewVoice.setOnClickListener(v -> {
            mImageViewVoice = (ImageView) v;
            if (VoiceRecognizer.State.STARTED == VoiceRecognizer.getInstance().getCurrentState()) {
                getDoor().disableVoiceRecognition();
            } else {
                showLoading(false, R.string.start_voice_recognition_label);
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

        this.mSwitchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showLoading(false, R.string.opened_label);
                getDoor().open(getString(R.string.opened_label), true);
                defaultView(R.string.opened_label, R.drawable.door_open, true);
            } else {
                getDoor().close(getString(R.string.closed_label), true);
                defaultView(R.string.closed_label, R.drawable.door_closed, false);

            }
        });

        if (this.mDoorOpened) {
            defaultView(R.string.opened_label, R.drawable.door_open, mDoorOpened);
        } else {
            defaultView(R.string.close_label, R.drawable.door_closed, false);

        }
        this.mEditTextOpen.setText(getDoor().getOpenPhrase());
        this.mEditTextClose.setText(getDoor().getClosePhrase());
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
