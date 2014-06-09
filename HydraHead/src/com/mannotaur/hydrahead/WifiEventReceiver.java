package com.mannotaur.hydrahead;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import static android.net.wifi.WifiManager.*;

public class WifiEventReceiver extends BroadcastReceiver {

    private final String TAG = "ControlSurface";
    private final WifiEventResponse mWifiEventListener;


    public WifiEventReceiver(WifiEventResponse listener) {
        mWifiEventListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        Log.d(TAG, "> "+action);
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d(TAG, ">     "+key+": "+extras.get(key).toString());

            }
        }
        WifiManager wifiManager = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE));

        if (action.equals(WIFI_STATE_CHANGED_ACTION)) {
            if (extras.getInt(EXTRA_WIFI_STATE) == WIFI_STATE_ENABLED)
                wifiManager.startScan();
        }

        if (action.equals(SCAN_RESULTS_AVAILABLE_ACTION)) {

            Log.d(TAG, "> Scan Results:");
            boolean targetNetworkFound = false;
            for (ScanResult scanResult: wifiManager.getScanResults())  {
                String result = "\""+scanResult.SSID+"\"";
                Log.d(TAG, "> Scan Results: " + scanResult.SSID);
                if (result.equals(HydraConfig.ROUTER_SSID)) {
                    mWifiEventListener.connectToTarget();
                    targetNetworkFound = true;
                }
            }

            if (!targetNetworkFound) {
                mWifiEventListener.targetNetworkNotFound();
            }
        }

        if (action.equals(SUPPLICANT_STATE_CHANGED_ACTION)) {
            if (extras.getParcelable(EXTRA_NEW_STATE).equals(SupplicantState.COMPLETED)) {

                String connectedSSID = "\""+wifiManager.getConnectionInfo().getSSID()+"\"";
                Log.d(TAG, ">>>>> Supplicant connected to: " + connectedSSID);
                if(connectedSSID.equals(HydraConfig.ROUTER_SSID)) {
                    mWifiEventListener.connectedToTarget();
                } else {
                    wifiManager.startScan();
                }
            }
        }

    }
}
