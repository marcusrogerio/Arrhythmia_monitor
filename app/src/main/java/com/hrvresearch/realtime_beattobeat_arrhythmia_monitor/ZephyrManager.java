package com.hrvresearch.realtime_beattobeat_arrhythmia_monitor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

// library provided by Zephyr  HxM BT Developement kit
// http://www.zephyr-technology.nl/en/article/54/development-tools.html
// some of this classes code is worked from their example app.
import zephyr.android.HxMBT.BTClient;
import zephyr.android.HxMBT.ZephyrProtocol;

/**
 * Created by Lowell Prange on 3/12/2015.
 *
 */
public class ZephyrManager {
    BluetoothAdapter adapter = null;
    BTClient _bt;
    MainActivity parent;
    // set to an invalid number so we know we are starting
    int lastheartbeatnumber = -15;

    ZephyrProtocol _protocol;
    NewConnectedListener _NConnListener;

    public ZephyrManager(MainActivity parent) {
        this.parent = parent;
    }

    private void firstConnect()

    {
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/

        parent.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        parent.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);

        // soon to be replaced by actuall mac address we find connected to the device
        String BhMacID = "00:07:80:9D:8A:E8";
        adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().startsWith("HXM")) {
                    BluetoothDevice btDevice = device;
                    BhMacID = btDevice.getAddress();
                    break;
                }
            }
        }
        //BhMacID = btDevice.getAddress();
        BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
        String DeviceName = Device.getName();
        _bt = new BTClient(adapter, BhMacID);
        _NConnListener = new NewConnectedListener(Newhandler, Newhandler);
        _bt.addConnectedEventListener(_NConnListener);
        if (_bt.IsConnected()) {
            _bt.start();
        }
    }
    private void dbg(String s) {
        System.out.println(s);
    }

    public void disconnect() {
//TODO: decide if this should just be removed
        _bt.Close();

    }

    public void connect() {
            firstConnect();

    }

    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("Bond state", "BOND_STATED = " + device.getBondState());
        }
    }

    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BTIntent", intent.getAction());
            Bundle b = intent.getExtras();
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
            try {
                BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[]{String.class});
                byte[] pin = (byte[]) m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", new Class[]{pin.getClass()});
                Object result = m.invoke(device, pin);
                Log.d("BTTest", result.toString());
            } catch (SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (NoSuchMethodException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    final Handler Newhandler;

    {
        Newhandler = new Handler() {
            public void handleMessage(Message msg) {

                int[] mytimestamps = msg.getData().getIntArray("timestamps");
                boolean checksumPass = msg.getData().getBoolean("       ");
                int batterylevel = msg.getData().getInt("BATTERY_CHARGE");
                int heart_beat_number = msg.getData().getInt("HEART_BEAT_NUMBER");
                int newheartbeats= (heart_beat_number - lastheartbeatnumber);

                // this only should happen at start up.  We found that we don't want the 14 beats waiting on the device because it offsets the
                // time too much.
                if (lastheartbeatnumber ==-1) newheartbeats =0;
                lastheartbeatnumber = heart_beat_number;
                if (newheartbeats<0) newheartbeats = newheartbeats+256;
                //              dbg("new heartbeats:" + newheartbeats);
                if (newheartbeats>13)newheartbeats=13;
                for (int i = newheartbeats-1; i >= 0  ; i--) {
                    int rr = mytimestamps[i] - mytimestamps[i+1];
                    parent.updateChart(rr);

                }
                parent.dbg("timestamps" + mytimestamps[0] + "," + mytimestamps[1]+ "," + mytimestamps[2]+ "," + mytimestamps[3]+ "," + mytimestamps[4]+ "," + mytimestamps[5]+ "," + mytimestamps[6]+ "," + mytimestamps[7]+ "," + mytimestamps[8]+ "," + mytimestamps[9]
                        + "," + mytimestamps[10] + "," + mytimestamps[11] + "," + mytimestamps[12] + "," + mytimestamps[13]);

                parent.displayBattery(batterylevel);

            }
        };


    }

}