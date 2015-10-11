package com.nanospark.gard.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;

import com.nanospark.gard.R;
import com.nanospark.gard.events.SmsSuspended;
import com.nanospark.gard.sms.SmsManager;
import com.nanospark.gard.ui.custom.BaseActivity;
import com.nanospark.gard.ui.fragments.DialogFragment;
import com.squareup.otto.Subscribe;

/**
 * Created by cristian on 23/09/15.
 */
public class MainActivityNew extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_main_tab;
    }

    @Override
    public boolean containsTab() {
        return true;
    }

    @Subscribe
    public void on(SmsSuspended smsSuspended){
        DialogFragment dialogFragment = DialogFragment.newInstance(getString(R.string.sms_suspend_message),getString(R.string.warning_label),true);
        dialogFragment.setDialogFragmentListener(new DialogFragment.DialogFragmentListener() {
            @Override
            public void onPositiveButton(DialogInterface dialog) {
                dialog.dismiss();
                SmsManager.getInstance().enableSms();
            }

            @Override
            public void onNegativeButton(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        dialogFragment.show(getSupportFragmentManager(),DialogFragment.class.getCanonicalName());
    }



}
