package com.nanospark.gard.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.nanospark.gard.events.SmsMessage;

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

            Tattu.post(new SmsMessage(body, from));
        }
    }
}
