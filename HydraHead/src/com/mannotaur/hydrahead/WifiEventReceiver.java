package com.mannotaur.hydrahead;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

/**
 * User: jason
 * Date: 28/01/14
 * Time: 2:11 AM
 */
public class WifiEventReceiver extends BroadcastReceiver {

    private final WifiManager wifiManager;
    private final WifiHandler handler;
    private final String MIDICLIENT = "WifiAssociate";

    public WifiEventReceiver(WifiManager systemService, WifiHandler handler) {
        this.wifiManager = systemService;
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        Log.d(MIDICLIENT, ">>> Recieved broadcast: " + action);
        if(extras != null) {
            for(String extra : extras.keySet()) Log.d(MIDICLIENT, ">>>    [" + extra + "]: " + extras.get(extra));
        }
        if(action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            if(extras.getParcelable(WifiManager.EXTRA_NEW_STATE).equals(SupplicantState.COMPLETED)) {
                String connectedBSSID = null;
                while(connectedBSSID == null) {
                    connectedBSSID = wifiManager.getConnectionInfo().getBSSID();
                    SystemClock.sleep(50);
                }
                Log.d(MIDICLIENT, ">>>   CurrentBSSID: " + connectedBSSID);
                if(!connectedBSSID.equals(HydraConfig.ROUTER_BSSID)) handler.connectToNetwork();
                else Log.d(MIDICLIENT, "CONNECTED!!!!!");
            }
        }
        // We HAVE CONNECTED
        //      searching = false


        // ACCESS POINT SCAN IS COMPLETE
        //      if searching = true
        //          scan access points - if not found set searching to true break
        //          try connecting
        /*
        if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && searching && !connecting) {
            connectToNetwork();
        }

        // We HAVE DISCONNECTED
        //      set searching = true
        //      scan access points - if not found set searching to true break
        //      try connecting
        if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) && extras.get(WifiManager.EXTRA_NEW_STATE).equals("DISCONNECTED")) {
            searching = true;
            connectToNetwork();
        }
        */
    }
}
