package com.nanospark.gard.ui.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nanospark.gard.R;
import com.nanospark.gard.Utils;
import com.nanospark.gard.model.door.Door;
import com.nanospark.gard.sms.SmsManager;
import com.nanospark.gard.sms.twilio.TwilioAccount;
import com.nanospark.gard.ui.custom.BaseFragment;
import com.nanospark.gard.weather.WeatherManager;

import mobi.tattu.utils.StringUtils;
import roboguice.inject.InjectView;
import rx.Observable;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends BaseFragment {

    @InjectView(R.id.disabled)
    private TextView mSmsledDisabled;
    @InjectView(R.id.routing)
    private TextView mRouting;
    @InjectView(R.id.phone)
    private TextView mPhone;
    @InjectView(R.id.imageview_menu)
    private View mSmsMenu;
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
    @InjectView(R.id.weather)
    private CheckBox mWeather;
    @InjectView(R.id.zipcode)
    private EditText mZipcode;
    @InjectView(R.id.city)
    private TextView mCity;
    @InjectView(R.id.display_celcius)
    private CheckBox mDisplayCelcius;

    @Inject
    private SmsManager mSmsManager;
    @Inject
    private WeatherManager mWeatherManager;

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

        changeTitleActionBar(getString(R.string.settings));

        mSmsMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getBaseActivity(), v);
            popupMenu.inflate(R.menu.autoclose);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        editSms();
                        break;
                    case R.id.action_enable:
                        mSmsManager.enableSms();
                        loadSmsSettings();
                        break;
                    case R.id.action_disable:
                        mSmsManager.disableSms();
                        loadSmsSettings();
                        break;
                }
                return true;
            });
            popupMenu.getMenu().findItem(R.id.action_enable).setVisible(!mSmsManager.isSmsEnabled());
            popupMenu.getMenu().findItem(R.id.action_disable).setVisible(mSmsManager.isSmsEnabled());
            popupMenu.show();
        });

        loadSmsSettings();

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

        mWeather.setChecked(mWeatherManager.isEnabled());
        mZipcode.setText(mWeatherManager.getCity().getZipCode());
        mCity.setText(mWeatherManager.getCity().toString());
        mDisplayCelcius.setChecked(WeatherManager.Unit.METRIC.equals(mWeatherManager.getUnit()));
    }

    @Override
    public boolean showHomeIcon() {
        return true;
    }

    public Observable<Boolean> save() {

        if (StringUtils.isBlank(mName1)) {
            toast("Please enter door 1 name");
            return Observable.just(false);
        }
        if (StringUtils.isBlank(mName2)) {
            toast("Please enter door 2 name");
            return Observable.just(false);
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

        mWeatherManager.setUnit(mDisplayCelcius.isChecked() ? WeatherManager.Unit.METRIC : WeatherManager.Unit.IMPERIAL);

        mWeatherManager.setEnabled(mWeather.isChecked());
        String zipCode = mZipcode.getText().toString();
        if (StringUtils.isBlank(zipCode)) {
            toast("Enter city zipcode");
            return Observable.just(false);
        }
        if (!mWeatherManager.getCity().getZipCode().equals(zipCode)) {
            return mWeatherManager.setCity(zipCode)
                    .map(city -> true)
                    .onErrorReturn(error -> {
                        toast("No city found for that zipcode");
                        return false;
                    });
        } else {
            return Observable.just(true);
        }
    }

    private void loadSmsSettings() {
        if (mSmsManager.isUsingInternet()) {
            mPhone.setText(mSmsManager.getAccount().getPhone());
            mPhone.setVisibility(StringUtils.isNotBlank(mSmsManager.getAccount().getPhone()) ? View.VISIBLE : View.GONE);
            mRouting.setText("Internet");
        } else {
            mPhone.setVisibility(View.GONE);
            mRouting.setText("Phone");
        }
        if (!Utils.hasTelephony()) {
            mRouting.setVisibility(View.GONE);
        }
        mSmsledDisabled.setVisibility(mSmsManager.isSmsEnabled() ? View.INVISIBLE : View.VISIBLE);
    }

    private int touchCount;

    public void editSms() {
        touchCount = 0;

        View content = inflate(R.layout.sms_edit, null, false);

        TextView cautionSms = (TextView) content.findViewById(R.id.caution_sms);
        cautionSms.setText(Html.fromHtml(getString(R.string.caution_sms)));

        if (!Utils.hasTelephony()) { // hide Phone/Internet radios if the device is is not a phone
            content.findViewById(R.id.sms_route_container).setVisibility(View.GONE);
        }

        RadioGroup radioGroup = (RadioGroup) content.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.telephony) { // hide/show twilio data if phone is checked
                content.findViewById(R.id.phone_container).setVisibility(View.GONE);
            } else { // internet
                content.findViewById(R.id.phone_container).setVisibility(View.VISIBLE);
            }
        });
        radioGroup.check(mSmsManager.isUsingInternet() ? R.id.internet : R.id.telephony);

        EditText phoneView = (EditText) content.findViewById(R.id.phone);
        EditText sidView = (EditText) content.findViewById(R.id.twilio_sid);
        EditText tokenView = (EditText) content.findViewById(R.id.twilio_token);
        TwilioAccount account = mSmsManager.getAccount();
        phoneView.setText(account.getPhone());
        sidView.setText(account.getSid());
        tokenView.setText(account.getToken());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("SMS Phone Number")
                .setPositiveButton(R.string.accept, (dialog, whichButton) -> {
                })
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
                })
                .setView(content);

        AlertDialog dialog = builder.show();
        dialog.findViewById(android.support.v7.appcompat.R.id.alertTitle).setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId() == R.id.internet) { // do not open advanced settings if phone is checked
                touchCount++;
                if (touchCount == 4) {
                    touchCount = 0;
                    content.findViewById(R.id.advanced).setVisibility(View.VISIBLE);
                }
            }
        });

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId() == R.id.internet) {
                if (StringUtils.isBlank(phoneView.getText())) {
                    toast("Please enter a valid phone number");
                    return;
                }
                if (StringUtils.isBlank(sidView.getText())) {
                    toast("Please enter a valid Twilio SID");
                    return;
                }
                if (StringUtils.isBlank(tokenView.getText())) {
                    toast("Please enter a valid Twilio Token");
                    return;
                }
            }

            account.setPhone(phoneView.getText().toString());
            account.setSid(sidView.getText().toString());
            account.setToken(tokenView.getText().toString());

            mSmsManager.setTwilioAccount(account);
            mSmsManager.setUseInternet(radioGroup.getCheckedRadioButtonId() == R.id.internet);

            loadSmsSettings();

            dialog.dismiss();
        });
    }

}
