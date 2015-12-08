package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;

import java.util.LinkedList;

public class Logque {
    private LinkedList<String> mydata;

    public Logque ()
    {
        mydata = new LinkedList();

    }

    public void add(String s)
    {
        mydata.add(s);
    }
    public String getnext()
    {

        return    mydata.getFirst();

    }
    public int getsize()
    {

        return mydata.size();

    }


}




