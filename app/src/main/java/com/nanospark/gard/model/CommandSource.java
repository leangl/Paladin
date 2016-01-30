package com.nanospark.gard.model;

/**
 * Created by lglossman on 30/1/16.
 */
public interface CommandSource {

    String getSourceDescription();

    CommandSource TOUCH = () -> "Touch";
    CommandSource VOICE = () -> "Voice";
    CommandSource SCHEDULED_ACTION = () -> "Scheduled Action";

}


