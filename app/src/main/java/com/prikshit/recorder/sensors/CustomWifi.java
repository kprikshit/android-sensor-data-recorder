package com.prikshit.recorder.sensors;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by Pankaj Kumar on 17-01-2015.
 * Modified by Prikshit Kumar <kprikshit@gmail.com/kprikshit@iitrpr.ac.in>
 *
 * A java class implemented to get wifi network id with signal strength
 */
public class CustomWifi {
    private WifiManager wifiManager;
    // Maximum no of WiFi connections to be stored
    private final int maxNumWifiNetworks = 3;
    // time when WiFi was last scanned
    private long lastScanTime;
    // scan WiFi interval in Milliseconds
    private int scanInterval = 10000;
    private boolean wifiEnabled;

    public CustomWifi(Context context){
        wifiManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        /**
         * We should not enable the WiFi, instead the user should do it.
         * this option should be given to user in the main activity.
         */
        wifiEnabled = wifiManager.isWifiEnabled();
    }

    public String getWifiSignals(){
        if(isWifiEnabled()) {
            // Periodic Scan for WiFi
            long currTime = System.currentTimeMillis();
            if (currTime - lastScanTime > scanInterval) {
                wifiManager.startScan();
                lastScanTime = currTime;
            }
            StringBuilder wifiData = new StringBuilder();
            List<ScanResult> results = wifiManager.getScanResults();
            // which connection we are processing right now.
            int currConnNum = 1;
            for (ScanResult result : results) {
                int signalStrength = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), result.level);
                if (currConnNum < maxNumWifiNetworks) {
                    wifiData.append(result.SSID);
                    wifiData.append(":");
                    wifiData.append(signalStrength);
                    if (currConnNum != maxNumWifiNetworks) {
                        wifiData.append("|");
                    }
                    currConnNum++;
                }
            }
            return wifiData.toString();
        }
        else return "-";
    }

    public boolean isWifiEnabled(){
        return this.wifiEnabled;
    }

    public void unregisterReceiver(){
        wifiManager.disconnect();
    }

}
