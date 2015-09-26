package com.hrvresearch.lowell.Arrhythmia_monitor;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.text.SimpleDateFormat;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);

        dbg("Testing");

        long x= System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        dbg(format.format(x));
    }

    private void dbg(String s) {

        System.out.println("LP_" + s);

    }

}