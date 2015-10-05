package com.nanospark.gard.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.model.door.Door;
import com.squareup.otto.Subscribe;

/**
 * Created by cristian on 23/09/15.
 */
public class MainFragment extends com.nanospark.gard.ui.custom.BaseFragment {

    private CardView mCardViewOneDoor;
    private CardView mCardViewTwoDoor;

    private Door mDoorOne = Door.getInstance(GarD.DOOR_ONE_ID);
    private Door mDoorTwo = Door.getInstance(GarD.DOOR_TWO_ID);


    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        FrameLayout containerOne = (FrameLayout) view.findViewById(R.id.door_one_container);
        FrameLayout containerTwo = (FrameLayout) view.findViewById(R.id.door_two_container);

        mCardViewOneDoor = (CardView) containerOne.findViewById(R.id.cardview_door);
        mCardViewTwoDoor = (CardView) containerTwo.findViewById(R.id.cardview_door);

        mCardViewTwoDoor.findViewById(R.id.container_center).setBackgroundColor(getColorFromResource(R.color.door_two_background));
        mCardViewTwoDoor.findViewById(R.id.container_switch).setBackgroundColor(getColorFromResource(R.color.door_two_switch_background));

        init(mCardViewOneDoor, mDoorOne);
        init(mCardViewTwoDoor, mDoorTwo);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void init(CardView cardView, Door door) {
        TextView textView = (TextView) cardView.findViewById(R.id.textview_open);
        ImageView imageViewDoor = (ImageView) cardView.findViewById(R.id.imageview_icon_door);
        cardView.findViewById(R.id.imageview_icon_sound).setOnClickListener(v -> {
            // TODO: 04/10/15 Preguntar a lea si hay que tenes una validacion que tiene que ingresar una frase para iniciar el reconocimiento de voz
            ImageView imageView = (ImageView) v;
            if (VoiceRecognizer.State.STARTED == VoiceRecognizer.getInstance().getCurrentState()) {
                imageView.setImageResource(R.drawable.ic_no_sound_);
                door.disableVoiceRecognition();
            } else {
                showLoading(false,R.string.start_voice_recognition_label);
                door.enableVoiceRecognition();
                imageView.setImageResource(R.drawable.ic_sound_);
            }
        });
        ((EditText) cardView.findViewById(R.id.edittext_open_voice)).setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            door.setOpenPhrase(editText.getText().toString());
        });
        ((EditText) cardView.findViewById(R.id.edittext_close_voice)).setOnFocusChangeListener((v, hasFocus) -> {
            EditText editText = (EditText) v;
            door.setOpenPhrase(editText.getText().toString());
        });
        ((SwitchCompat)cardView.findViewById(R.id.switch_open)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // TODO: 04/10/15 Preguntar a lea que hay que pasarle
                door.open("Open");
                textView.setText(R.string.opened_label);
                imageViewDoor.setImageResource(R.drawable.door_open);
            } else {
                door.close(getString(R.string.closed_label));
                textView.setText(R.string.closed_label);
                imageViewDoor.setImageResource(R.drawable.door_closed);
            }
        });
    }

    @Subscribe
    public void on(VoiceRecognizer.State state) {
        if (state == VoiceRecognizer.State.STARTED) {
            stopLoading();
       } else if (state == VoiceRecognizer.State.STOPPED || state == VoiceRecognizer.State.ERROR) {
            stopLoading();
        }
    }


}
