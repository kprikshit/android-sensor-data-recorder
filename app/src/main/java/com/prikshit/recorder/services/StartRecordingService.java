package com.prikshit.recorder.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.prikshit.recorder.R;
import com.prikshit.recorder.constants.Constants;
import com.prikshit.recorder.main.AlarmManagers;
import com.prikshit.recorder.main.Logger;
import com.prikshit.recorder.main.TmpData;
import com.prikshit.recorder.sensors.CustomGPS;

import java.util.Calendar;

/**
 * Created on: 15-02-2015 by
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 */
public class StartRecordingService extends BroadcastReceiver {
    private final String TAG = "autoStartRecording";
    // duration of interval for getting the avg speed
    private final long intervalDuration = 1*60*1000;
    private final long gpsTimeout = 1*60*1000;

    @Override
    public void onReceive(Context original, Intent baseIntent) {
        final Context context = original;
        final CustomGPS gps = new CustomGPS(context);
        Logger.i(TAG, "AUTOSTART alarm receive triggered");
        Thread thread = new Thread(){
            @Override
            public void run(){
                Looper.prepare();
                //check whether gps is enabled or not
                if(gps.isGpsEnabled()){
                    Long startTime = Calendar.getInstance().getTimeInMillis();
                    Log.d(TAG, "GPS location updates requested ");
                    //while either timeout occurs or we detect movement or no movement, run this loop
                    while(true){
                        Location currLocation = gps.getLastLocation();
                        if(currLocation != null){
                            Log.d(TAG, "last not null location received");
                            // check for a movement for a interval instead of checking once
                            // getting the avg speed for some interval of time
                            float avgSpeed = currLocation.getSpeed();
                            Long lastTime = Calendar.getInstance().getTimeInMillis();
                            while(Calendar.getInstance().getTimeInMillis() - lastTime < intervalDuration){
                                avgSpeed += gps.getLastLocation().getSpeed();
                                avgSpeed /=2;
                            }
                            Logger.d(TAG,"avg speed is "+ avgSpeed);
                            if(avgSpeed > Constants.SPEED_THRESHOLD){
                                // no need to start the service from here
                                // just change state of record switch and service will be started from there.
                                // sending an intent back to activity for changing the state of record switch

                                // stop alarm for autoStart Check
                                Logger.d(TAG, "Avg peed is more than defined threshold");
                                Logger.d(TAG, "cancelling auto start alarm");
                                AlarmManagers.cancelAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
                                TmpData.setStartAlarmRunning(false);

                                if(Constants.NOTIFICATION_ENABLED) {
                                    showNotification(context);
                                }
                                // start Service
                                context.startService(new Intent(context, Constants.RECORDING_CLASS));
                                TmpData.recordingOn = true;
                                Logger.i(TAG, "sending recording state back to main activity as " + true);
                                Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", true);
                                context.sendBroadcast(newIntent);

                                // start alarm for autoStop check
                                Logger.d(TAG, "starting auto stop alarm");
                                AlarmManagers.startAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
                                TmpData.setStopAlarmRunning(true);
                            }
                            break;
                        }
                        // location given by gps is null i.e. GPS is not yet activated
                        else if(Math.abs(Calendar.getInstance().getTimeInMillis() - startTime) > gpsTimeout ){
                            Logger.i(TAG, "GPS timeout occurred");
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    gps.unregisterListener();
                }
                else{
                    Logger.i(TAG, "GPS is disabled");
                }
                // quit thread when done with everything
                Logger.i(TAG,"Quitting onReceive AutoStart\n\n");
                Looper.myLooper().quit();
            }
        };
        thread.start();
    }

    public void showNotification(Context context){
        Logger.i(TAG, "showing notification for start recording");
        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Recording Started")
                .setContentText("Movement was detected. Recording Enabled");
        notificationManager.notify(0, builder.build());
    }

}
