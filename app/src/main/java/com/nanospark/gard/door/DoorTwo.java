package com.nanospark.gard.door;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by cristian on 29/09/15.
 */
@Singleton
public class DoorTwo extends BaseDoor {

    @Inject
    private DoorTwo() {
        super(2, 6, 7);
    }
}
