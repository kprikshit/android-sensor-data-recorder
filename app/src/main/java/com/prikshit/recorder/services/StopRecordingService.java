package com.prikshit.recorder.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import com.prikshit.recorder.R;
import com.prikshit.recorder.constants.Constants;
import com.prikshit.recorder.main.AlarmManagers;
import com.prikshit.recorder.main.Logger;
import com.prikshit.recorder.main.TmpData;
import com.prikshit.recorder.sensors.CustomGPS;

import java.util.Calendar;

/**
 * This class basically checks for movement after the recording is switched on.
 * If there is no significant movement in some time, then it will stop the recording automatically and
 * will notify the user about this.
 *
 * Created on: 11-02-2015 by
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 */
public class StopRecordingService extends BroadcastReceiver {
    private final String TAG = "autoStopRecording";
    private final long checkDuration = 10*1000;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Logger.i(TAG, "AUTOSTOP alarm receive triggered");
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Logger.d(TAG, "GPS location updates requested ");
                Location currLocation = CustomGPS.getLastLocation();
                if (currLocation != null) {
                    Logger.d(TAG, "last not null location received");
                    // check for an interval of time
                    float avgSpeed = currLocation.getSpeed();
                    Long lastTime = Calendar.getInstance().getTimeInMillis();
                    while( Calendar.getInstance().getTimeInMillis() - lastTime < checkDuration){
                        avgSpeed += CustomGPS.getLastLocation().getSpeed();
                        avgSpeed /=2;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Logger.d(TAG, "Error in sleeping thread used for checking speed");
                        }
                    }
                    Logger.d(TAG,"avg speed is "+ avgSpeed);
                    if (avgSpeed < Constants.SPEED_THRESHOLD) {
                        Logger.d(TAG, "Avg speed is less than defined threshold");
                        // stop autoStop alarm
                        Logger.d(TAG, "cancelling AutoStop alarm");
                        AlarmManagers.cancelAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
                        TmpData.setStopAlarmRunning(false);

                        //Log.d(TAG, "stopping now at " + getInstance().getTime());
                        // no need to stop service from here
                        // it will be done by the listener in the activity
                        //context.stopService(new Intent(context, Constants.RECORDING_CLASS));
                        // set the last location as null after the service has stopped.
                        CustomGPS.setLastLocation(null);
                        if(Constants.NOTIFICATION_ENABLED) {
                            showNotification(context);
                        }
                        // stop the service and set the TmpData.recordindOn variable as false;
                        context.stopService(new Intent(context, Constants.RECORDING_CLASS));
                        TmpData.recordingOn = false;
                        Logger.i(TAG, "sending recording state back to main activity as "+ false);
                        Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", false);
                        context.sendBroadcast(newIntent);

                        // start autoStart Alarm
                        Logger.d(TAG,"starting AutoStart alarm");
                        AlarmManagers.startAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
                        TmpData.setStartAlarmRunning(true);

                    } else {
                        Logger.i(TAG, "Device is moving. No need to stop");
                    }
                }
                else {
                    Logger.d(TAG, "GPS not yet activated");
                }
                // quit the thread when done
                Logger.i(TAG, "quitting AutoStop\n\n");
                Looper.myLooper().quit();
            }
        };
        thread1.start();
    }


    public void showNotification(Context context) {
        Logger.i(TAG, "showing notification for stop recording");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Recording Disabled")
                .setContentText("No Movement Detected. Recording Stopped");
        Uri notSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notSound);
        notificationManager.notify(1, builder.build());
    }

}
