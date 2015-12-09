package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import zephyr.android.HxMBT.*;

public class NewConnectedListener extends ConnectListenerImpl
{
    private Handler _OldHandler;
    private Handler _aNewHandler;
    private int GP_MSG_ID = 0x20;
    private int GP_HANDLER_ID = 0x20;
    private int HR_SPD_DIST_PACKET =0x26;

    //private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;

    public static final String HEART_BEAT_NUMBER = "HEART_BEAT_NUMBER";
    public static final String TIMESTAMPS = "TIMESTAMPS";
    public static final String TIME = "TIME";
    public static final String DISTANCE = "DISTANCE";
    public static final String SPEED = "SPEED";
    public static final String STRIDES = "STRIDES";
    public static final String BATTERY_CHARGE = "BATTERY_CHARGE";

    public static final String HEART_RATE = "HEART_RATE";


    private HRSpeedDistPacketInfo HRSpeedDistPacket = new HRSpeedDistPacketInfo();
    public NewConnectedListener(Handler handler,Handler _NewHandler) {
        super(handler, null);
        _OldHandler= handler;
        _aNewHandler = _NewHandler;



    }
    public void Connected(ConnectedEvent<BTClient> eventArgs) {
        System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));
        //Creates a new ZephyrProtocol object and passes it the BTComms object
        ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
        //ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
        _protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
            public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
                ZephyrPacketArgs msg = eventArgs.getPacket();
                byte CRCFailStatus;
                byte RcvdBytes;
                CRCFailStatus = msg.getCRCStatus();
                RcvdBytes = msg.getNumRvcdBytes() ;
                if (HR_SPD_DIST_PACKET==msg.getMsgID())
                {
                    byte [] DataArray = msg.getBytes();

                    //***************Displaying the Heart Rate********************************
                    //				int HRate =  HRSpeedDistPacket.GetHeartRate(DataArray);
                    int[] timestamps = HRSpeedDistPacket.GetHeartBeatTS(DataArray);
                    //			Message text1 = _aNewHandler.obtainMessage(HEART_RATE);

                    byte[] chargebyte;

//                    HRSpeedDistPacket.GetBatteryChargeInd(DataArray);
                    //                  HRSpeedDistPacket.GetHeartBeatNum(DataArray);
                    Bundle b1 = new Bundle();
                    b1.putIntArray("timestamps", timestamps);
                    int battery = HRSpeedDistPacket.GetBatteryChargeInd(DataArray);
                    int heartrate = HRSpeedDistPacket.GetHeartRate(DataArray);
                    int heartnumber = HRSpeedDistPacket.GetHeartBeatNum(DataArray);



                    b1.putInt(BATTERY_CHARGE, battery);
                    b1.putInt(HEART_RATE, heartrate);
                    b1.putInt(HEART_BEAT_NUMBER, heartnumber);






                    Message theMessage = new Message();
                    theMessage.setData(b1);
                    _aNewHandler.sendMessage(theMessage);
//					text1 = _aNewHandler.obtainMessage(INSTANT_SPEED);
//					b1.putString("InstantSpeed", String.valueOf(InstantSpeed));
//					text1.setData(b1);

                    //***************Displaying the Instant Speed********************************
//					double InstantSpeed = HRSpeedDistPacket.GetInstantSpeed(DataArray);

//					text1 = _aNewHandler.obtainMessage(INSTANT_SPEED);
//					b1.putString("InstantSpeed", String.valueOf(InstantSpeed));
//					text1.setData(b1);
//					_aNewHandler.sendMessage(text1);
//					System.out.println("Instant Speed is "+ InstantSpeed);

                }
            }
        });
    }

}