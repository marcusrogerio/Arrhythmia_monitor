package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;


/**
 * Created by Lowell Prange on 6/4/15.
 * For Status messages that should only briefly be showed on the screen, like "Starting"
 *
 *
 */





public class SelfClearingMessage {
    private final MainActivity parent;
    private long myclockstarttime;
    long howlongtoshowmessage = 3000;

    public SelfClearingMessage(MainActivity in) {
        myclockstarttime = System.currentTimeMillis();
        parent = in;

    }


    public void setMessage(String s) {
        parent.setTempText(s);
        myclockstarttime = System.currentTimeMillis();
    }


    public void checkForClear() {
        if (System.currentTimeMillis() > myclockstarttime + howlongtoshowmessage)         parent.setTempText("");
//System.out.println("" + System.currentTimeMillis() + (myclockstarttime + howlongtoshowmessage));


    }
}
