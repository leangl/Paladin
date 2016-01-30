package com.nanospark.gard.model.user;

import mobi.tattu.utils.ResourceUtils;

/**
 * Created by Leandro on 31/10/2015.
 */
public enum Limit {
    FOREVER, DATE, EVENTS;

    @Override
    public String toString() {
        return ResourceUtils.stringByName("limit." + name().toLowerCase());
    }
}
