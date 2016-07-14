package com.nanospark.gard.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.nanospark.gard.events.SmsMessage;

import io.fabric.sdk.android.services.common.Crash;
import mobi.tattu.utils.Tattu;

public class SmsReceiver extends BroadcastReceiver {

    private static final String PDUS_EXTRA_KEY = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        Object[] pdus = (Object[]) extras.get(PDUS_EXTRA_KEY);
        for (Object pdu : pdus) {
            android.telephony.SmsMessage msg = android.telephony.SmsMessage.createFromPdu((byte[]) pdu);

            String body = msg.getMessageBody();
            String from = msg.getOriginatingAddress();

            Crashlytics.log("SMS: " + body + " - from: " + from);

            Tattu.post(new SmsMessage(body, from));
        }
    }
}
