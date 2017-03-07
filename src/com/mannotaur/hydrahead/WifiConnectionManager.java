package com.mannotaur.hydrahead;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

public class WifiConnectionManager implements WifiEventResponse {

    private static final String TAG = "WifiConnectionManager";

    private final WifiManager wifiManager;
    private final Context context;
    private final HydraHead hydra;
    private WifiEventReceiver receiver;
    private final WifiConfiguration hydraServerWifiConfig;
    private int hydraServerID;

    private int connection_attempts;

    private IntentFilter filter;

    private boolean receiverIsRegistered;

    public WifiConnectionManager(Context context, HydraHead hydra) {

        this.context = context;
        this.hydra = hydra;
        connection_attempts = 0;

        wifiManager = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE));

        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        receiverIsRegistered = false;

        receiver = new WifiEventReceiver(this);

        // Create ASUS Network configuration
        hydraServerWifiConfig = new WifiConfiguration();
        hydraServerWifiConfig.SSID = HydraConfig.ROUTER_SSID;
        hydraServerWifiConfig.BSSID = HydraConfig.ROUTER_BSSID;
        hydraServerWifiConfig.status = WifiConfiguration.Status.DISABLED;
        hydraServerWifiConfig.priority = 40;
            // Setup security
        hydraServerWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        hydraServerWifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        hydraServerWifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        hydraServerWifiConfig.preSharedKey = HydraConfig.ROUTER_SHARED_KEY;

        //   From Begin network discovery method
        /* Turn on wifi if not already */
        if(wifiManager.isWifiEnabled()) {
            /* If it is on we can attempt to connect */
            checkForTarget();
        } else {
            /* If not we will have to enable it and wait for the signal that it has connectedToServer to come back */
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            wifiManager.setWifiEnabled(true);
        }

        Log.d(TAG, "registerReceiverIdempotent");
        registerReceiverIdempotent();
        // Add the ASUS configuration to the system
        hydraServerID = -1;
        hydraServerID = wifiManager.addNetwork(hydraServerWifiConfig);
        // Wifi must be on detect this
        if(!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true); //turn on wifi if not already
        // It can take time to register the new configuration
        Log.d(TAG, "endless loop");
        /*
        while(hydraServerID < 0) {
            SystemClock.sleep(50);
            hydraServerID = wifiManager.addNetwork(hydraServerWifiConfig);
            Log.d(TAG, "endless loop " + hydraServerID);
        }
        */
        Log.d(TAG, "endless loop over");
    }

    void onResume() {
        if(!receiverIsRegistered) {
            registerReceiverIdempotent();
            receiverIsRegistered = true;
        }
    }

    void onPause() {
        if(receiverIsRegistered) {
            unregisterReceiverIdempotent();
            receiverIsRegistered = false;
        }
    }


    public void connectToNetwork() {

        connection_attempts++;

        String currentBSSID = wifiManager.getConnectionInfo().getBSSID();
        if (currentBSSID != null) {
            if (!currentBSSID.equals(HydraConfig.ROUTER_BSSID)) { // IF NOT currently connectedToServer to ASUS
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
                /* Already connectedToServer to our target access point */
            }
        }
    }

    private void registerReceiverIdempotent() {
        if(!receiverIsRegistered) {
            context.registerReceiver(receiver, filter);
            receiverIsRegistered = true;
            Log.d(TAG, ">>>>> Registered receiver.");
        }
    }

    private void unregisterReceiverIdempotent() {
        if(receiverIsRegistered) {
            context.unregisterReceiver(receiver);
            receiverIsRegistered = false;
            Log.d(TAG, ">>>>> Unregistered receiver.");
        }
    }


    /* Wifi Event Listeners */

    @Override
    public void checkForTarget() {
        Log.d(TAG, "Checking for target network...");
    }

    @Override
    public void connectToTarget() {
        Log.d(TAG, "Network found. Attempting to connect...");
        int networkID = wifiManager.addNetwork(hydraServerWifiConfig);
        wifiManager.enableNetwork(networkID, true);
    }

    @Override
    public void connectedToTarget() {
        Log.d(TAG, "Connected to target router!");

        // Why do I want to unregister the receiver here?
        // I still need to make use of the
        unregisterReceiverIdempotent();

        // get the assigned IP address
        HydraConfig.LOCAL_IP = Networking.getLocalAddress(wifiManager.getConnectionInfo().getIpAddress());
        Log.d(TAG, ""+HydraConfig.LOCAL_IP);

        // send message CONNECTED_TO_NETWORK
        hydra.connectedToNetwork();

    }

    @Override
    public void targetNetworkNotFound() {
        Log.d(TAG, "Network Not Found");
        hydra.networkNotFound();
    }

}
