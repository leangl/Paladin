package com.nanospark.gard.voice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.events.DoorStateChanged;
import com.nanospark.gard.events.PhraseRecognized;
import com.nanospark.gard.events.VoiceRecognitionDisabled;
import com.nanospark.gard.events.VoiceRecognitionEnabled;
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
        stop(true);
    }

    private void stop(boolean sendEvent) {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
            if (sendEvent) setCurrentState(VoiceRecognizer.State.STOPPED);
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
            recognizer.addKeyphraseSearch(Door.State.OPEN.name() + door.getId(), door.getOpenPhrase().toLowerCase());
        }
        if (StringUtils.isNotBlank(door.getClosePhrase())) {
            recognizer.addKeyphraseSearch(Door.State.CLOSED.name() + door.getId(), door.getClosePhrase().toLowerCase());
        }
    }

    private void setCurrentState(VoiceRecognizer.State state) {
        this.currentState = state;
        Tattu.post(new StateChanged(this.door, this.currentState));
    }

    @Subscribe
    public void on(VoiceRecognitionEnabled event) {
        stop(false);
        start(event.door);
    }

    @Subscribe
    public void on(VoiceRecognitionDisabled event) {
        stop();
    }

    @Subscribe
    public void on(DoorStateChanged event) {
        switchPhrase();
    }

    @Subscribe
    public void on(PhraseRecognized event) {
        event.door.send(new Door.Toggle("Command heard, door is in motion", true));
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
     * This callback is called when we stopChecking the recognizer.
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
     * We stopChecking recognizer here to get a final result
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
            if (door.getState() != Door.State.UNKNOWN) {
                boolean result = recognizer.startListening(door.getState().name() + door.getId());
                Log.d(TAG, "startListening " + result);
            }
        }
    }

    @Produce
    public State getCurrentState() {
        return this.currentState;
    }

    public class StateChanged {
        public final Door door;
        public final State state;

        public StateChanged(Door door, State state) {
            this.door = door;
            this.state = state;
        }
    }

    public enum State {
        STARTED, STOPPED, ERROR;
    }

}
