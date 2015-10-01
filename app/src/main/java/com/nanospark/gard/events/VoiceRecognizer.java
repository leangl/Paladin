package com.nanospark.gard.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.model.door.Door;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import ioio.lib.spi.Log;
import mobi.tattu.utils.StringUtils;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.image.AsyncTask;
import roboguice.RoboGuice;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by Leandro on 21/7/2015.
 */
@Singleton
public class VoiceRecognizer implements RecognitionListener {

    private static final String TAG = VoiceRecognizer.class.getSimpleName();

    public static final float DEFAULT_THRESHOLD = 1e-40f;

    private static final String KEY_OPEN = "open";
    private static final String KEY_CLOSE = "open";

    private State currentState = State.STOPPED;
    private SpeechRecognizer recognizer;
    private Door door;

    @Inject
    private VoiceRecognizer() {
        Tattu.register(this);
    }

    public static final VoiceRecognizer getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(VoiceRecognizer.class);
    }

    public void setState(State state) {
        this.currentState = state;
        Tattu.post(this.currentState);
    }

    public void start(Door door) {
        this.door = door;
        // Start voice recognition asynchronously
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(GarD.instance);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir, DEFAULT_THRESHOLD);
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                if (e != null) {
                    setCurrentState(VoiceRecognizer.State.ERROR);
                    GarD.instance.toast("Error, unrecognized word entered.");
                } else {
                    switchPhrase();
                    setCurrentState(VoiceRecognizer.State.STARTED);
                }
            }
        }.execute((Void) null);
    }

    public void stop() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
            setCurrentState(VoiceRecognizer.State.STOPPED);
        }
    }

    private void setupRecognizer(File assetsDir, float threshold) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setKeywordThreshold(threshold) // Threshold to tune for keyphrase to balance between false alarms and misses
                .setBoolean("-allphone_ci", true) // Use context-independent phonetic search, context-dependent is too slow for mobile
                .getRecognizer();

        recognizer.addListener(this);

        // Create keyword-activation search.
        if (StringUtils.isNotBlank(door.getOpenPhrase())) {
            recognizer.addKeyphraseSearch(KEY_OPEN + door.getId(), door.getOpenPhrase());
        }
        if (StringUtils.isNotBlank(door.getClosePhrase())) {
            recognizer.addKeyphraseSearch(KEY_CLOSE + door.getId(), door.getClosePhrase());
        }
    }

    private void setCurrentState(VoiceRecognizer.State state) {
        setState(state);
    }

    @Subscribe
    public void on(Door.VoiceRecognitionEnabled event) {
        if (VoiceRecognizer.State.STARTED.equals(getCurrentState())) {
            // TODO
        }
    }

    @Subscribe
    public void on(DoorToggled event) {
        switchPhrase();
    }

    @Subscribe
    public void on(PhraseRecognized event) {
        event.door.toggle("Command heard, door is in motion");
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

    private void showHypothesis(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            if (text.equals(door.getOpenPhrase()) || text.equals(door.getClosePhrase())) {
                Tattu.post(new PhraseRecognized(door, text));
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

    @Override
    public void onError(Exception error) {
        setCurrentState(VoiceRecognizer.State.ERROR);
    }

    @Override
    public void onTimeout() {
    }

    private void switchPhrase() {
        if (recognizer != null) {
            recognizer.stop();
            boolean result;
            if (door.isOpened()) {
                result = recognizer.startListening(KEY_OPEN + door.getId());
            } else {
                result = recognizer.startListening(KEY_CLOSE + door.getId());
            }
            Log.d(TAG, "startListening " + result);
        }
    }

    @Produce
    public State getCurrentState() {
        return this.currentState;
    }

    public enum State {
        STARTED, STOPPED, ERROR
    }

}
