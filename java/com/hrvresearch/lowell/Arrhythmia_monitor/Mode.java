package com.hrvresearch.lowell.Arrhythmia_monitor;

/**
 * Created by Lowell Prange on 8/6/15.
 */
public enum Mode {

    SINGLENUMBER,TICKERTAPE,GRAPH;

     private static Mode[] vals = values();
    public Mode next()
    {
        return vals[(this.ordinal()+1) % vals.length];
    }



}
