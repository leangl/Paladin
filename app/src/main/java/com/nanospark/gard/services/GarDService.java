package com.nanospark.gard.services;

import android.app.Notification;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.inject.Inject;
import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.door.Door;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.VoiceRecognizer;
import com.nanospark.gard.twilio.MessagesClient;
import com.squareup.otto.Subscribe;

import java.util.concurrent.TimeUnit;

import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.services.BaseService;

/**
 * Created by Leandro on 21/7/2015.
 */
public class GarDService extends BaseService implements IOIOLooperProvider {

    public static final String KEY_THRESHOLD = "threshold";
    public static final String START_VOICE_RECOGNITION = "START_VOICE_RECOGNITION";
    public static final String STOP_VOICE_RECOGNITION = "STOP_VOICE_RECOGNITION";

    // service started flag
    private boolean started;

    private VoiceRecognizer mVoiceRecognizer;

    @Inject
    private Door.One mDoorOne;
    @Inject
    private Door.Two mDoorTwo;

    @Inject
    private MessagesClient mClient;
    private Handler smsHandler;

    private IOIOAndroidApplicationHelper ioioHelper;

    private Notification mNotification;

    private static long MESSAGES_CHECK_TIME = TimeUnit.SECONDS.toMillis(5);
    private static long MESSAGES_RETRY_TIME = TimeUnit.SECONDS.toMillis(30);

    /**
     * Periondically checks Twilio Messages Log for new messages
     */
    private Runnable checkMessages = new Runnable() {
        @Override
        public void run() {
            mClient.getNewMessage().subscribe(message -> {
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
                        if (Door.getInstance(doorNumber).isOpened() != isOpenCommand) {
                            mDoorOne.toggle("Message received, door is in motion");
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

                    mClient.sendMessage(replyMessage, from).subscribe(success -> {
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

    @Override
    public void onCreate() {
        super.onCreate();
        smsHandler = new Handler();
        mVoiceRecognizer = VoiceRecognizer.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVoiceRecognizer.stop();

        if (smsHandler != null) {
            smsHandler.removeCallbacksAndMessages(null);
            smsHandler = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!started) {
            started = true;

            mNotification = new NotificationCompat.Builder(GarDService.this)
                    .setContentTitle("GarD is active")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            startForeground(123, mNotification);

            // Start checking for incoming SMS messages
            smsHandler.removeCallbacksAndMessages(null);
            smsHandler.postDelayed(checkMessages, MESSAGES_CHECK_TIME);
        }

        startIOIO();

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_VOICE_RECOGNITION:
                    // Get setup parameters (recognition threshold and open/close phrases)
                    mVoiceRecognizer.start(mDoorOne);
                    break;
                case STOP_VOICE_RECOGNITION:
                    mVoiceRecognizer.stop();
                    break;
            }
        }

        return START_STICKY;
    }

    @Subscribe
    public void on(BoardConnected e) {
        //toast("Board connected!");
        //startIOIO();
    }

    private void startIOIO() {
        stopIOIO();

        ioioHelper = new IOIOAndroidApplicationHelper(this, this);
        ioioHelper.create();
        ioioHelper.start();
    }

    private void stopIOIO() {
        if (ioioHelper != null) {
            ioioHelper.stop();
            ioioHelper.destroy();
            ioioHelper = null;
        }
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return new Looper();
    }

    private class Looper extends BaseIOIOLooper {

        @Override
        protected void setup() throws ConnectionLostException {
            mDoorOne.setup(ioio_);
            mDoorTwo.setup(ioio_);
            Tattu.post(new BoardConnected());
        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            mDoorOne.loop();
            mDoorTwo.loop();
        }

        @Override
        public void disconnected() {
            Tattu.post(new BoardDisconnected());
        }

        @Override
        public void incompatible() {
            GarD.instance.toast("Incompatible firmware version!");
        }
    }

    public static void startVoiceRecognition(float threshold) {
        Intent i = new Intent(GarD.instance, GarDService.class);
        i.setAction(START_VOICE_RECOGNITION);
        i.putExtra(KEY_THRESHOLD, threshold);
        GarD.instance.startService(i);
    }

    public static void stopVoiceRecognition() {
        Intent i = new Intent(GarD.instance, GarDService.class);
        i.setAction(STOP_VOICE_RECOGNITION);
        GarD.instance.startService(i);
    }

    public static void start() {
        Intent i = new Intent(GarD.instance, GarDService.class);
        GarD.instance.startService(i);
    }

    public static void stop() {
        GarD.instance.stopService(new Intent(GarD.instance, GarDService.class));
    }
}
