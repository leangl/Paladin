package com.nanospark.gard.events;

import com.nanospark.gard.model.door.Door;

/**
 * Created by Leandro on 21/7/2015.
 */
public class PhraseRecognized {

    public final Door door;
    public final String phrase;

    public PhraseRecognized(Door door, String phrase) {
        this.door = door;
        this.phrase = phrase;
    }

}
