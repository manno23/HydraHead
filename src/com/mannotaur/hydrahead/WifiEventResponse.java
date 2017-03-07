package com.mannotaur.hydrahead;

/**
 * User: Jason Manning
 * Date: 9/06/14
 */
public interface WifiEventResponse {
    /**
     * Called when the wifi has been enabled.
     *
     */
    public void checkForTarget();

    /**
     * Called when a scan has found our target access point.
     *
     */
    public void connectToTarget();

    /**
     * Called when we have successfully connectedToServer to the target access point.
     * May be called more than once.
     *
     */
    public void connectedToTarget();

    /**
     * Called when the target access point was not found in the scan.
     * May be called more than once.
     *
     */
    public void targetNetworkNotFound();
}
