package com.mannotaur.hydrahead;

import android.content.Context;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import com.mannotaur.hydrahead.messages.RotationMessage;

public class ControlSurface extends GLSurfaceView implements SensorEventListener, Handler.Callback, WifiEventResponse {

    private static final String TAG = "ControlSurface";
    private Context context;
    private HydraRenderer hydraRenderer;

    private WifiManager wifiManager;
    private IntentFilter filter;
    private WifiEventReceiver receiver;
    private WifiConfiguration wifiConfig;

    public Networking mNetworkInterface;

    private SensorManager mSensorManager;
    private Sensor mRotationVector;

    private boolean handlerIsRegistered;



    /* Lifecycle events */

    public ControlSurface(Context context) {
        super(context);
        this.context = context;

        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        receiver = new WifiEventReceiver(this);
        filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);

        mNetworkInterface = new Networking(this);
        handlerIsRegistered = false;

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        hydraRenderer = new HydraRenderer(context, mNetworkInterface);
        setRenderer(hydraRenderer);


        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hydraRenderer.onTouch(event);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_UI); //   50 Updates/s
        beginNetworkDiscovery();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");
        mSensorManager.unregisterListener(this);
        unregisterReceiverIdempotent();
    }



    /* Sensor Event Listeners */

    @Override
    public void onSensorChanged(SensorEvent event) {
        hydraRenderer.onSensorChanged(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    /**
     * Because ControlSurface implements Handler.Callback it handles all <br>
     * messages that the Networking class receives. The messages are filtered <br>
     * and directed to where they will be used here, as this is the first point <br>
     * of call on the UI thread. <br>
     *
     * @param msg Handler.Message contains the type of message in its msg.what <br>
     *            attribute. The bundle of data contains a byte array with key "data".
     * @return true if message is handled, false otherwise.
     */
    @Override
    public boolean handleMessage(Message msg) {
        Bundle packet = msg.getData();
        byte[] data = packet.getByteArray("data");
        int sceneID;
        switch (msg.what) {

            case Networking.CONNECTION_VERIFICATION:
                sceneID = data[1];
                hydraRenderer.connected(sceneID, data);
                mNetworkInterface.initialise();
                break;

            case Networking.CHANGE_SCENE:
                break;

            case Networking.SCENE_UPDATE:
                hydraRenderer.handleMessage(data);
                break;

            case Networking.ACTIVATE_INSTRUMENT:
                sceneID = data[1];
                hydraRenderer.activateInstrument(sceneID);
                break;

            case Networking.DETECTING_WIFI_TIMED_OUT:
                hydraRenderer.connecitonTimedOut();
                Toast toast = Toast.makeText(
                        context,
                        "Server is not running, should be soon.. ",
                        Toast.LENGTH_LONG
                );
                toast.show();

        }
        return true;
    }



    /* Wifi Event Listeners */

    @Override
    public void checkForTarget() {
        Log.d(TAG, "Checking for target network...");
    }

    @Override
    public void connectToTarget() {
        Log.d(TAG, "Network found. Attempting to connect...");
    }

    @Override
    public void connectedToTarget() {
        Log.d(TAG, "Connected to target router!");
        HydraConfig.LOCAL_IP = mNetworkInterface.getLocalAddress(wifiManager.getDhcpInfo().ipAddress);
        unregisterReceiverIdempotent();
        mNetworkInterface.connect();
    }

    @Override
    public void targetNetworkNotFound() {
        Log.d(TAG, "Network Not Found");
        unregisterReceiverIdempotent();
    }



    /* Member functions */

    private void beginNetworkDiscovery() {

        /* Create ASUS Network configuration */
        wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = HydraConfig.ROUTER_SSID;
        wifiConfig.status = WifiConfiguration.Status.DISABLED;
        wifiConfig.priority = 40;

        /* Setup security */
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.preSharedKey = HydraConfig.ROUTER_SHARED_KEY;

        /* Turn on wifi if not already */
        if(wifiManager.isWifiEnabled()) {
            /* If it is on we can attempt to connect */
            checkForTarget();
        } else {
            /* If not we will have to enable it and wait for the signal that it has connected to come back */
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            wifiManager.setWifiEnabled(true);
        }

        registerReceiverIdempotent();
    }

    private void unregisterReceiverIdempotent() {
        if(handlerIsRegistered) {
            context.unregisterReceiver(receiver);
            handlerIsRegistered = false;
            mNetworkInterface.close();
            Log.d(TAG, ">>>>> Unregistered receiver.");
        }
    }

    private void registerReceiverIdempotent() {
        if(!handlerIsRegistered) {
            context.registerReceiver(receiver, filter);
            handlerIsRegistered = true;
            Log.d(TAG, ">>>>> Registered receiver.");
        }
    }

}

