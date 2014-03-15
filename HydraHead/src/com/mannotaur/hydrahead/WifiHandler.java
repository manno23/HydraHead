package com.mannotaur.hydrahead;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

public class WifiHandler {

    private final WifiManager wifiManager;
    private int _ASUS_network_id;

    private final String MIDICLIENT = "WifiAssociate";

    public WifiHandler(WifiManager wifiManager) {
        this.wifiManager = wifiManager;

        Log.d(MIDICLIENT, "Creating asus-config");
        // Create ASUS Network configuration
        WifiConfiguration Asus_config = new WifiConfiguration();
        Asus_config.SSID = HydraConfig.ROUTER_SSID;
        Asus_config.BSSID = HydraConfig.ROUTER_BSSID;
        Asus_config.status = WifiConfiguration.Status.DISABLED;
        Asus_config.priority = 40;
        // Setup security
        Asus_config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        Asus_config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        Asus_config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        Asus_config.preSharedKey = HydraConfig.ROUTER_SHARED_KEY;
        // Add the ASUS configuration to the system
        _ASUS_network_id = -1;
        _ASUS_network_id = wifiManager.addNetwork(Asus_config);
        while(_ASUS_network_id < 0) {
            Log.d(MIDICLIENT, "addNetwork(Asus_config) failed");
            SystemClock.sleep(50);
            _ASUS_network_id = wifiManager.addNetwork(Asus_config);
        }
        Log.d(MIDICLIENT, "asusconfig added");
    }


    public void connectToNetwork() {

        Log.d(MIDICLIENT, "connectToNetwork()");
        if(!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true); //turn on wifi if not already
        Log.d(MIDICLIENT, "setWifiEnabled()");
        String currentBSSID = wifiManager.getConnectionInfo().getBSSID();
        Log.d(MIDICLIENT, "wifiManager.getConnectionInfo().getBSSID");
        if (currentBSSID != null) {
            Log.d(MIDICLIENT, "currentBSSID != null");
            if (!currentBSSID.equals(HydraConfig.ROUTER_BSSID)) { // IF NOT currently connected to ASUS
                Log.d(MIDICLIENT, "currentbssis != asus bssis  &&  connecting = false");
                List<WifiConfiguration> wifiResultList = null;
                while (wifiResultList == null) {    // may require a few times to return, though should always return
                    wifiResultList = wifiManager.getConfiguredNetworks();
                    SystemClock.sleep(50);
                }
                int ASUScfgID = -1;
                for (WifiConfiguration wifiConfig : wifiResultList) {
                    Log.d(MIDICLIENT, "Found Router configuration: " + wifiConfig.SSID + " ID: " + wifiConfig.networkId);
                    if (wifiConfig.BSSID != null && wifiConfig.BSSID.equals(HydraConfig.ROUTER_BSSID)) {
                        ASUScfgID = wifiConfig.networkId;
                        Log.d(MIDICLIENT, "netID(returned from creation): " + _ASUS_network_id + " netID(found in cfglist): " + ASUScfgID);
                    }
                }
                //Check that the router has been scanned first
                boolean wifiInRange = false;
                Log.d(MIDICLIENT, "Checking latest scan");
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult wifiAccessPoint : scanResults) {
                    Log.d(MIDICLIENT, "Scanned: " + wifiAccessPoint.SSID);
                    if(wifiAccessPoint.BSSID.equals(HydraConfig.ROUTER_BSSID)) {
                        wifiInRange = true;
                        Log.d(MIDICLIENT, "Router is in range");
                    }
                }
                if (ASUScfgID >= 0 && wifiInRange) {
                    Log.d(MIDICLIENT, "Targeted router is found");
                    Log.d(MIDICLIENT, "disconnect()");
                    wifiManager.disconnect();
                    Log.d(MIDICLIENT, "enableNetwork(target_router, true)");
                    if (!wifiManager.enableNetwork(ASUScfgID, true)) {
                        Log.d(MIDICLIENT, "We are unsuccessful in connecting with target router");
                    } else {
                        Log.d(MIDICLIENT, "Reconnecting");
                        wifiManager.reconnect();
                    }
                } else {
                    Log.d(MIDICLIENT, "Did not find our target router");
                    // In this case we rely on NetworkChangeReceiver to tell us when the router can be found to try and reconnect
                    // Display on screen "Searching"
                }
            } else {
                Log.d(MIDICLIENT, "Already connected to the target router");
                // In this case we rely on NetworkChangeReceiver to tell us when the router can be found to try and reconnect
                // Display on screen "Searching"
            }
        }
    }

}
