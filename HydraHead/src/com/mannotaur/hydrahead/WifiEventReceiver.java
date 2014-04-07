package com.mannotaur.hydrahead;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;


public class WifiEventReceiver extends BroadcastReceiver {

    private final WifiManager wifiManager;
    private final WifiHandler handler;

    private final String MIDICLIENT = "MidiClient";

    public WifiEventReceiver(WifiManager systemService, WifiHandler handler) {
        this.wifiManager = systemService;
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();

        if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            if (extras != null) {
                if(extras.getParcelable(WifiManager.EXTRA_NEW_STATE).equals(SupplicantState.COMPLETED)) {
                    String connectedBSSID = null;
                    while(connectedBSSID == null) {
                        connectedBSSID = wifiManager.getConnectionInfo().getBSSID();
                        HydraConfig.IDENTIFIER = wifiManager.getDhcpInfo().ipAddress >> 24;
                        SystemClock.sleep(50);
                    }
                    if(!connectedBSSID.equals(HydraConfig.ROUTER_BSSID)) {
                        handler.connectToNetwork();
                    }
                    else {
                        Log.d(MIDICLIENT, "We are connected... TO THE HYDRA");
                        // Initiate callbacks to begin networking
                        MidiClientActivity m = (MidiClientActivity)context;
                        m.mNetworkInterface.init();
                        if(m.mNetworkInterface.establishConnection()) {
                            //Signal to UI that we are connected
                            m.mNetworkInterface.beginReceiving();
                        }
                    }
                }
            }
        }
    }
}
