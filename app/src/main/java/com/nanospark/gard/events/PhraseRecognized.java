package com.nanospark.gard.events;

import com.nanospark.gard.door.BaseDoor;

/**
 * Created by Leandro on 21/7/2015.
 */
public class PhraseRecognized {

    public final BaseDoor door;
    public final String phrase;

    public PhraseRecognized(BaseDoor door, String phrase) {
        this.door = door;
        this.phrase = phrase;
    }

}
