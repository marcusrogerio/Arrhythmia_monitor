package com.hrvresearch.lowell.Arrhythmia_monitor;


/**
 * Created by lowell on 6/4/15.
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
