package com.nanospark.gard.events;

/**
 * Created by lglossman on 12/2/16.
 */
public class SmsMessage {

    public final String body;
    public final String from;
    public final Long timestamp;

    public SmsMessage(String body, String from) {
        this.body = body;
        this.from = from;
        this.timestamp = System.currentTimeMillis();
    }

}
