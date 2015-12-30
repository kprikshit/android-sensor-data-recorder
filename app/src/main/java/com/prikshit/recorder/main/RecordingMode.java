package com.prikshit.recorder.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.prikshit.recorder.constants.Constants;


/**
 * A broadcast receiver which listen to battery and boot related broadcasts.
 *
 * Created on: 08-02-2015 by
 * Prikshit Kumar
 * kprikshit@iitrpr.ac.in
 */
public class RecordingMode extends BroadcastReceiver{
    private static int currentMode = Constants.FAST_RECORDING_MODE;
    private int batteryPercentage = 1;
    private boolean isCharging = false;
    IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

    @Override
    public void onReceive(Context context, Intent intent) {
        // boot completed
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            // starting uploader alarm
            AlarmManagers.startUploaderAlarm(context);
            // starting auto start alarm on boot up
            AlarmManagers.startAlarm(context,Constants.AUTO_START_RECORDING_CLASS);
            TmpData.startAlarmRunning = true;
        }
        // check for battery level and set the recording mode accordingly
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING);

        Intent batteryStatus = context.registerReceiver(null, batteryFilter);
        int currentBatteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryPercentage = (int) (((float) currentBatteryLevel / (float) batteryScale) * 100);

        if (batteryPercentage <= Constants.BATTERY_SAVER_LEVEL && !isCharging) {

            currentMode = Constants.BATTERY_SAVER_RECORDING_MODE;
            // stop recording as battery level is low than defined threshold
            context.stopService(new Intent(context, Constants.RECORDING_CLASS));
            TmpData.recordingOn = false;
            //sending back to main activity
            Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", false);
            context.sendBroadcast(newIntent);
        } else {
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
