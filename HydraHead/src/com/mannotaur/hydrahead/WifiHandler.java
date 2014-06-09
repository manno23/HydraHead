package com.mannotaur.hydrahead;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import java.util.List;

public class WifiHandler {

    private final WifiManager wifiManager;
    private int connection_attempts;
    private int _ASUS_network_id;

    private final String MIDICLIENT = "WifiAssociate";

    public WifiHandler(Context context) {

        connection_attempts = 0;
        this.wifiManager = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE));

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
        // It can take time to register the new configuration

        if(!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true); //turn on wifi if not already
        // wifi must be on detect this
        while(_ASUS_network_id < 0) {
            SystemClock.sleep(50);
            _ASUS_network_id = wifiManager.addNetwork(Asus_config);
        }
    }


    public void connectToNetwork() {

        connection_attempts++;

        String currentBSSID = wifiManager.getConnectionInfo().getBSSID();
        if (currentBSSID != null) {
            if (!currentBSSID.equals(HydraConfig.ROUTER_BSSID)) { // IF NOT currently connected to ASUS
                List<WifiConfiguration> wifiResultList = null;
                while (wifiResultList == null) {    // may require a few times to return, though should always return
                    wifiResultList = wifiManager.getConfiguredNetworks();
                    SystemClock.sleep(50);
                }
                int ASUScfgID = -1;
                for (WifiConfiguration wifiConfig : wifiResultList) {
                    if (wifiConfig.BSSID != null && wifiConfig.BSSID.equals(HydraConfig.ROUTER_BSSID)) {
                        ASUScfgID = wifiConfig.networkId;
                    }
                }
                //Check that the router has been scanned first
                boolean wifiInRange = false;
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult wifiAccessPoint : scanResults) {
                    if(wifiAccessPoint.BSSID.equals(HydraConfig.ROUTER_BSSID)) {
                        wifiInRange = true;
                    }
                }
                if (ASUScfgID >= 0 && wifiInRange) {
                    wifiManager.disconnect();
                    if (!wifiManager.enableNetwork(ASUScfgID, true)) {
                        /* We are unsuccessful in connecting with target router") */
                        wifiManager.startScan();
                    } else {
                        /* Reconnect now that we have enabled our target AP */
                        wifiManager.reconnect();
                    }
                } else {
                    /* Did not find our target router */
                    wifiManager.startScan();
                }
            } else {
                /* Already connected to our target access point */
            }
        }
    }

}
