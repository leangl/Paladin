package com.nanospark.gard.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.nanospark.gard.GarD;
import com.nanospark.gard.door.BaseDoor;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import mobi.tattu.utils.Tattu;
import mobi.tattu.utils.image.AsyncTask;
import roboguice.RoboGuice;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by Leandro on 21/7/2015.
 */
@Singleton
public class VoiceRecognition implements RecognitionListener {

    private static final String KEY_OPEN = "open";
    private static final String KEY_CLOSE = "open";

    private State currentState = State.STOPPED;
    private SpeechRecognizer recognizer;
    private BaseDoor[] mDoors;
    
    @Inject
    private VoiceRecognition() {
        mDoors = BaseDoor.getDoors();
        Tattu.register(this);
    }

    public static final VoiceRecognition getInstance() {
        return RoboGuice.getInjector(GarD.instance).getInstance(VoiceRecognition.class);
    }

    public void setState(State state) {
        this.currentState = state;
        Tattu.post(this.currentState);
    }

    public void start(float threshold) {
        // Start voice recognition asynchronously
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(GarD.instance);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir, threshold);
                    return null;
                } catch (Exception e) {
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                if (e != null) {
                    setCurrentState(VoiceRecognition.State.ERROR);
                    GarD.instance.toast("Error, unrecognized word entered.");
                } else {
                    switchPhrase();
                    setCurrentState(VoiceRecognition.State.STARTED);
                }
            }
        }.execute((Void) null);
    }

    public void stop() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
            setCurrentState(VoiceRecognition.State.STOPPED);
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
        for (BaseDoor door : mDoors) {
            recognizer.addKeyphraseSearch(KEY_OPEN + door.getId(), door.getOpenPhrase());
            recognizer.addKeyphraseSearch(KEY_CLOSE + door.getId(), door.getClosePhrase());
        }
    }

    private void setCurrentState(VoiceRecognition.State state) {
        setState(state);
    }

    @Subscribe
    public void on(BaseDoor.VoiceRecognitionEnabled event) {
        if (VoiceRecognition.State.STARTED.equals(getCurrentState())) {
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
            for (BaseDoor door : mDoors) {
                if (text.equals(door.getOpenPhrase()) || text.equals(door.getClosePhrase())) {
                    Tattu.post(new PhraseRecognized(door, text));
                    switchPhrase();
                }
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
        setCurrentState(VoiceRecognition.State.ERROR);
    }

    @Override
    public void onTimeout() {
    }

    private void switchPhrase() {
        if (recognizer != null) {
            recognizer.stop();
            for (BaseDoor door : mDoors) {
                if (door.isOpened()) {
                    recognizer.startListening(KEY_OPEN + door.getId());
                } else {
                    recognizer.startListening(KEY_CLOSE + door.getId());
                }
            }
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
