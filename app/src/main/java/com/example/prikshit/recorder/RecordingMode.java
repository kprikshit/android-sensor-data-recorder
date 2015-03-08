package com.example.prikshit.recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.Toast;

/**
 * Battery adaptive recording mode for various sensors
 * NOT completely done at the moment.
 *
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-02-2015
 */
public class RecordingMode extends BroadcastReceiver{
    private static int currentMode = Constants.FAST_RECORDING_MODE;
    private int batteryLevel = 1;
    private boolean isCharging = false;
    IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ;

        Intent batteryStatus = context.registerReceiver(null, batteryFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryLevel = (int)( ( (float)level /(float)scale )*100);

        if( batteryLevel <= Constants.BATTERY_SAVER_LEVEL && !isCharging ){
            currentMode = Constants.BATTERY_SAVER_RECORDING_MODE ;
            //Toast.makeText(context, String.format("battery level: %d", batteryLevel),Toast.LENGTH_SHORT).show();
            // stop recording as battery level is low than defined threshold
            Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", false);
            context.sendBroadcast(newIntent);
        }
        else {
            //Toast.makeText(context, String.format("battery level: %d", batteryLevel), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * return the mode based on battery level
     */
    public static int getCurrentMode(){
        return currentMode ;
    }

    public static void setCurrentMode(int newRecordingMode){
        currentMode = newRecordingMode ;
    }

}
