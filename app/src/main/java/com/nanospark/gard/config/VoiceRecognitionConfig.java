package com.nanospark.gard.config;

import com.nanospark.gard.GarD;
import com.nanospark.gard.R;

import java.io.Serializable;

import mobi.tattu.utils.persistance.datastore.DataStore;

/**
 * Created by Leandro on 17/8/2015.
 */
public class VoiceRecognitionConfig implements Serializable {

    public static final float DEFAULT_THRESHOLD = 1e-40f;
    public static final int DEFAULT_LEVEL = -40;

    private int level;
    private String openPhrase;
    private String closePhrase;

    public VoiceRecognitionConfig() {
    }

    public VoiceRecognitionConfig(int level, String openPhrase, String closePhrase) {
        this.level = level;
        this.openPhrase = openPhrase;
        this.closePhrase = closePhrase;
    }

    public String getClosePhrase() {
        return closePhrase;
    }
    public void setClosePhrase(String closePhrase) {
        this.closePhrase = closePhrase;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public String getOpenPhrase() {
        return openPhrase;
    }
    public void setOpenPhrase(String openPhrase) {
        this.openPhrase = openPhrase;
    }

    public static VoiceRecognitionConfig defaultConfig() {
        VoiceRecognitionConfig config = new VoiceRecognitionConfig();
        config.level = DEFAULT_LEVEL;
        config.openPhrase = GarD.instance.getString(R.string.default_open);
        config.closePhrase = GarD.instance.getString(R.string.default_close);
        return config;
    }

    public static VoiceRecognitionConfig getSavedValue() {
        VoiceRecognitionConfig config;
        try {
            config = DataStore.getInstance().getObject(VoiceRecognitionConfig.class.getSimpleName(), VoiceRecognitionConfig.class);
        } catch (DataStore.ObjectNotFoundException e) {
            config = VoiceRecognitionConfig.defaultConfig();
        }
        return config;
    }

    public void save() {
        DataStore.getInstance().putObject(this.getClass().getSimpleName(), this);
    }

}
