package  com.prikshit.recorder.sensors;

import android.content.Context;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Created by Pankaj Kumar on 18-01-2015.
 */
public class CustomCellular {
    private TelephonyManager telephonyManager;
    private int maxNumCellularNetworks;
    private List<NeighboringCellInfo> neighboringList;

    public CustomCellular(Context context) {
        telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        maxNumCellularNetworks = 3;
    }

    public String getCellularSignals() {
        String cellularData="";
        neighboringList = telephonyManager.getNeighboringCellInfo();
        for (int i = 0; i < neighboringList.size(); i++) {
            if (i < maxNumCellularNetworks) {
                String dBm;
                int rssi = neighboringList.get(i).getRssi();
                if (rssi == NeighboringCellInfo.UNKNOWN_RSSI) {
                    dBm = "Unknown RSSI";
                }
                else {
                    dBm = String.valueOf( -113 + 2 * rssi) + " dBm";
                }
                cellularData = cellularData
                        + String.valueOf(neighboringList.get(i).getLac()) + " : "
                        + String.valueOf(neighboringList.get(i).getCid()) + " : "
                        + dBm;
                if (i != maxNumCellularNetworks - 1) {
                    cellularData += '|';
                }
            }
        }
        return cellularData;
    }
}
