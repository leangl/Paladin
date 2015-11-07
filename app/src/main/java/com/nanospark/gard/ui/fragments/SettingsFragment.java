package com.nanospark.gard.ui.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.nanospark.gard.R;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.ui.custom.BaseFragment;

import mobi.tattu.utils.StringUtils;
import roboguice.inject.InjectView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends BaseFragment {

    @InjectView(R.id.door1)
    private CheckBox mDoor1;
    @InjectView(R.id.name1)
    private EditText mName1;
    @InjectView(R.id.open1)
    private CheckBox mOpen1;
    @InjectView(R.id.close1)
    private CheckBox mClose1;
    @InjectView(R.id.door2)
    private CheckBox mDoor2;
    @InjectView(R.id.name2)
    private EditText mName2;
    @InjectView(R.id.open2)
    private CheckBox mOpen2;
    @InjectView(R.id.close2)
    private CheckBox mClose2;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Door door1 = Door.getInstance(1);
        mName1.setText(door1.getName());
        mDoor1.setChecked(door1.isEnabled());
        mOpen1.setChecked(door1.isOpenSwitchEnabled());
        mClose1.setChecked(door1.isCloseSwitchEnabled());

        Door door2 = Door.getInstance(2);
        mName2.setText(door2.getName());
        mDoor2.setChecked(door2.isEnabled());
        mOpen2.setChecked(door2.isOpenSwitchEnabled());
        mClose2.setChecked(door2.isCloseSwitchEnabled());
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }

    public boolean save() {

        if (StringUtils.isBlank(mName1)) {
            toast("Please enter door 1 name");
            return false;
        }
        if (StringUtils.isBlank(mName2)) {
            toast("Please enter door 2 name");
            return false;
        }

        Door door1 = Door.getInstance(1);
        door1.setName(mName1.getText().toString());
        door1.setEnabled(mDoor1.isChecked());
        door1.setOpenSwitchEnabled(mOpen1.isChecked());
        door1.setCloseSwitchEnabled(mClose1.isChecked());

        Door door2 = Door.getInstance(2);
        door2.setName(mName2.getText().toString());
        door2.setEnabled(mDoor2.isChecked());
        door2.setOpenSwitchEnabled(mOpen2.isChecked());
        door2.setCloseSwitchEnabled(mClose2.isChecked());

        return true;
    }
}
