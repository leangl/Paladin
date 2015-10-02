package com.nanospark.gard.sms;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Singleton;
import com.nanospark.gard.model.door.Door;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import mobi.tattu.utils.persistance.datastore.DataStore;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;

import static com.nanospark.gard.sms.TwilioApi.DATE_FORMAT;

/**
 * Created by Leandro on 26/7/2015.
 */
@Singleton
public class MessagesClient {

    private static long MESSAGES_CHECK_TIME = TimeUnit.SECONDS.toMillis(5);
    private static long MESSAGES_RETRY_TIME = TimeUnit.SECONDS.toMillis(30);

    private TwilioApi mApi;
    private Handler smsHandler;

    public MessagesClient() {
        Gson gson = new GsonBuilder()
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.twilio.com")
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(mBasicAuthInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(msg -> Log.i("retrofit", msg))
                .build();

        mApi = restAdapter.create(TwilioApi.class);
        smsHandler = new Handler();
    }

    public void start() {
        // Start checking for incoming SMS messages
        smsHandler.removeCallbacksAndMessages(null);
        smsHandler.postDelayed(checkMessages, MESSAGES_CHECK_TIME);
    }

    public void stop() {
        if (smsHandler != null) {
            smsHandler.removeCallbacksAndMessages(null);
        }
    }

    private RequestInterceptor mBasicAuthInterceptor = request -> {
        TwilioAccount account = DataStore.getInstance().getObject(TwilioAccount.class.getSimpleName(), TwilioAccount.class).get();
        if (account != null) {
            String credentials = account.getSid() + ":" + account.getToken();
            String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            request.addHeader("Authorization", string);
        }
    };

    public Observable<Void> sendMessage(String message, String to) {
        TwilioAccount account = DataStore.getInstance().getObject(TwilioAccount.class.getSimpleName(), TwilioAccount.class).get();
        if (account != null) {
            if (!account.isValid()) {
                Log.e("TWILIO", "Account not set!");
                return Observable.error(new Exception());
            }

            return mApi.sendMessage(account.getSid(), message, account.getPhone(), to).map(result -> {
                Log.i("TWILIO", "Messages sent: " + message + " to " + to);
                return null;
            });
        } else {
            Log.e("TWILIO", "Account not set!");
            return Observable.error(new Exception());
        }
    }

    /**
     * Returns the last new message received since the last check.
     * If more than one message was received, then only the newest is returned.
     * If no new messages, then null is returned.
     *
     * @return
     */
    public Observable<JsonObject> getNewMessage() {
        TwilioAccount account = DataStore.getInstance().getObject(TwilioAccount.class.getSimpleName(), TwilioAccount.class).get();
        if (account != null) {
            if (!account.isValid()) {
                Log.e("TWILIO", "Account not set!");
                return Observable.error(new Exception());
            }

            return mApi.getMessages(account.getSid(), account.getPhone()).map(response -> {
                JsonArray messages = response.getAsJsonObject().getAsJsonArray("messages");
                if (messages.size() > 0) {
                    for (JsonElement message : messages) {
                        JsonObject messageObj = message.getAsJsonObject();
                        String direction = messageObj.get("direction").getAsString();
                        if ("inbound".equals(direction)) {
                            try {
                                Date timestamp = DATE_FORMAT.parse(messageObj.get("date_sent").getAsString());
                                boolean after = true;
                                Calendar lastCal = Calendar.getInstance();
                                Date lastTimestamp = DataStore.getInstance().getObject("LAST_TIMESTAMP", Date.class).get();
                                if (lastTimestamp != null) {
                                    lastCal.setTime(lastTimestamp);

                                    Calendar messageCal = Calendar.getInstance();
                                    messageCal.setTime(timestamp);

                                    after = messageCal.after(lastCal);
                                }
                                if (after) {
                                    DataStore.getInstance().putObject("LAST_TIMESTAMP", timestamp);
                                    return messageObj;
                                }
                            } catch (ParseException e) {
                                System.out.println(e.getMessage());
                            }
                            break;
                        }
                    }
                }
                return null;
            });
        } else {
            Log.e("TWILIO", "Account not set!");
            return Observable.error(new Exception());
        }
    }

    /**
     * Periondically checks Twilio Messages Log for new messages
     */
    private Runnable checkMessages = new Runnable() {
        @Override
        public void run() {
            getNewMessage().subscribe(message -> {
                // if new message is received
                if (message != null) {
                    // check if the body matches the current door status
                    String body = message.get("body").getAsString();
                    String from = message.get("from").getAsString();
                    String replyMessage = null;

                    try {
                        String[] bodyParts = body.split(" ");
                        int doorNumber = Integer.parseInt(bodyParts[0]);
                        String command = bodyParts[1];

                        boolean isOpenCommand = "open".equalsIgnoreCase(command);
                        Door door = Door.getInstance(doorNumber);
                        if (door.isOpened() != isOpenCommand) {
                            door.toggle("Message received, door is in motion");
                            if (isOpenCommand) {
                                replyMessage = "Open door command received.";
                            } else {
                                replyMessage = "Close door command received.";
                            }
                        } else {
                            if (isOpenCommand) {
                                replyMessage = "The door is already open.";
                            } else {
                                replyMessage = "The door is already closed.";
                            }
                        }
                    } catch (Exception e) {
                        Log.e("TWILIO", "Invalid command: " + body);
                        replyMessage = "Invalid command. Format has to be: {door} {command}";
                    }

                    sendMessage(replyMessage, from).subscribe(success -> {
                        Log.i("TWILIO", "Reply sent successfully");
                    }, error -> {
                        Log.e("TWILIO", "Error sending reply.", error);
                    });
                }
                // Reschedule message log check
                if (smsHandler != null) {
                    smsHandler.removeCallbacksAndMessages(null);
                    smsHandler.postDelayed(checkMessages, MESSAGES_CHECK_TIME);
                }
            }, error -> {
                smsHandler.removeCallbacksAndMessages(null);
                smsHandler.postDelayed(checkMessages, MESSAGES_RETRY_TIME);
            });
        }
    };

}
