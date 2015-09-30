package com.nanospark.gard.door;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Created by cristian on 29/09/15.
 */
@Singleton
public class DoorOne extends BaseDoor {

    @Inject
    private DoorOne() {
        super(1, 4, 5);
   }
}
