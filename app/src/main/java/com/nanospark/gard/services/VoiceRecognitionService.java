package com.nanospark.gard.services;

import android.app.Notification;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.google.inject.Inject;
import com.nanospark.gard.GarD;
import com.nanospark.gard.R;
import com.nanospark.gard.events.BoardConnected;
import com.nanospark.gard.events.BoardDisconnected;
import com.nanospark.gard.events.PhraseRecognized;
import com.nanospark.gard.events.RecognizerLifecycle;
import com.nanospark.gard.twilio.MessagesClient;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.IOIOLooperProvider;
import ioio.lib.util.android.IOIOAndroidApplicationHelper;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.image.AsyncTask;
import mobi.tattu.utils.services.BaseService;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by Leandro on 21/7/2015.
 */
public class VoiceRecognitionService extends BaseService implements RecognitionListener, IOIOLooperProvider {

    public static final String KEY_OPEN = "open";
    public static final String KEY_CLOSE = "close";

    public static final String DEFAULT_PHRASE_OPEN = "open the door";
    public static final String DEFAULT_PHRASE_CLOSE = "close the door";

    public static final String KEY_THRESHOLD = "threshold";
    public static final float DEFAULT_THRESHOLD = 1e-40f;

    // current key = [KEY_OPEN|KEY_CLOSE]
    private String currentKey;
    private String currentPhrase;

    // service started flag
    private boolean started;
    // ioio connection started flag
    private boolean ioioStarted = false;

    private SpeechRecognizer recognizer;
    private float threshold;
    private String openPhrase;
    private String closePhrase;

    @Inject
    private MessagesClient mClient;

    private Handler handler;

    private DigitalOutput pin;
    private boolean activatePin;

    private IOIOAndroidApplicationHelper helper_;

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
                    if (currentKey.equals(message.get("body").getAsString())) {
                        // publish event so that other components update accordingly
                        Tattu.post(new PhraseRecognized(currentPhrase));
                        // and switch the current phrase
                        switchPhrase();
                    }
                }
                // Reschedule message log check
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(checkMessages, MESSAGES_CHECK_TIME);
                }
            }, error -> {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(checkMessages, MESSAGES_RETRY_TIME);
            });
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        helper_ = new IOIOAndroidApplicationHelper(this, this);
        helper_.create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
        setCurrentState(RecognizerLifecycle.State.STOPPED);
        handler.removeCallbacksAndMessages(null);
        handler = null;
        helper_.stop();
        helper_.destroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!started) {
            started = true;
            helper_.start();

            // Get setup parameters (recognition threshold and open/close phrases)
            threshold = intent.getFloatExtra(KEY_THRESHOLD, DEFAULT_THRESHOLD);
            openPhrase = intent.getStringExtra(KEY_OPEN);
            if (openPhrase == null || openPhrase.trim().length() == 0) {
                openPhrase = DEFAULT_PHRASE_OPEN;
            }
            closePhrase = intent.getStringExtra(KEY_CLOSE);
            if (closePhrase == null || closePhrase.trim().length() == 0) {
                closePhrase = DEFAULT_PHRASE_CLOSE;
            }

            // Start voice recognition asynchronously
            new AsyncTask<Void, Void, Exception>() {
                @Override
                protected Exception doInBackground(Void... params) {
                    try {
                        Assets assets = new Assets(VoiceRecognitionService.this);
                        File assetDir = assets.syncAssets();
                        setupRecognizer(assetDir);
                        return null;
                    } catch (Exception e) {
                        return e;
                    }
                }

                @Override
                protected void onPostExecute(Exception e) {
                    if (e != null) {
                        setCurrentState(RecognizerLifecycle.State.ERROR);
                        stopSelf();
                    } else {
                        switchPhrase();
                        setCurrentState(RecognizerLifecycle.State.STARTED);

                        Notification n = new NotificationCompat.Builder(VoiceRecognitionService.this)
                                .setTicker("Listening...")
                                .setContentTitle("GarD")
                                .setContentText("Voice recognition is active")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .build();
                        startForeground(123, n);

                        // Start checking for incoming SMS messages
                        handler.removeCallbacksAndMessages(null);
                        handler.postDelayed(checkMessages, MESSAGES_CHECK_TIME);
                    }
                }
            }.execute((Void) null);
        }

        return START_STICKY;
    }

    @Subscribe
    public void on(BoardConnected e) {
        if (!ioioStarted) {
            helper_.start();
            ioioStarted = true;
        } else {
            helper_.restart();
        }
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return new Looper();
    }

    private class Looper extends BaseIOIOLooper {

        @Override
        protected void setup() throws ConnectionLostException {
            pin = ioio_.openDigitalOutput(41, false);
        }

        @Override
        public void loop() throws ConnectionLostException, InterruptedException {
            if (activatePin) {
                activatePin = false;

                // high for 2 seconds and then low again
                pin.write(true);
                //toast("High!");
                Thread.sleep(2000);
                pin.write(false);
                //toast("Low!");
                //Thread.sleep(2000);
            } else {
                //toast("Waiting");
                Thread.sleep(200);
            }
        }

        @Override
        public void disconnected() {
            Tattu.post(new BoardDisconnected());
        }

        @Override
        public void incompatible() {
            //toast("Incompatible firmware version!");
        }
    }

    public static void start(float threshold, String openPhrase, String closePhrase) {
        Intent i = new Intent(GarD.instance, VoiceRecognitionService.class);
        i.putExtra(KEY_THRESHOLD, threshold);
        i.putExtra(KEY_OPEN, openPhrase);
        i.putExtra(KEY_CLOSE, closePhrase);
        GarD.instance.startService(i);
    }

    public static void stop() {
        GarD.instance.stopService(new Intent(GarD.instance, VoiceRecognitionService.class));
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                        //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setKeywordThreshold(threshold) // Threshold to tune for keyphrase to balance between false alarms and misses
                .setBoolean("-allphone_ci", true) // Use context-independent phonetic search, context-dependent is too slow for mobile
                .getRecognizer();

        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KEY_OPEN, openPhrase);
        recognizer.addKeyphraseSearch(KEY_CLOSE, closePhrase);
    }

    private void setCurrentState(int state) {
        RecognizerLifecycle.getInstance().setState(state);
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        showHypothesis(hypothesis);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
    }

    public void showHypothesis(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if (text.equals(openPhrase) || text.equals(closePhrase)) {
                Tattu.post(new PhraseRecognized(text));
                switchPhrase();
            }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
    }

    private void switchPhrase() {
        recognizer.stop();
        if (KEY_OPEN.equals(currentKey)) {
            currentKey = KEY_CLOSE;
            currentPhrase = closePhrase;
        } else {
            currentKey = KEY_OPEN;
            currentPhrase = openPhrase;
        }
        recognizer.startListening(currentKey);
    }

    @Override
    public void onError(Exception error) {
        setCurrentState(RecognizerLifecycle.State.ERROR);
    }

    @Override
    public void onTimeout() {
    }

    @Subscribe
    public void on(PhraseRecognized event) {
        activatePin = true;
    }

    private Toast toast;

    public void toast(String message) {
        Tattu.runOnUiThread(() -> {
            if (toast != null) toast.cancel();
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            toast.show();
        });
    }
}
