package com.example.prikshit.recorder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.internal.ch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import static com.example.prikshit.recorder.Constants.*;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.getInstance;

/**
 * Created on: 15-02-2015 by
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 */
public class StartRecorderService extends BroadcastReceiver {
    private final String TAG = "autoStartRecording";
    // duration of interval for getting the avg speed
    private final long intervalDuration = 1*60*1000;
    private final long gpsTimeout = 1*60*1000;

    @Override
    public void onReceive(Context original, Intent baseIntent) {
        final Context context = original;
        final CustomGPS gps = new CustomGPS(context);
        //Log.d(TAG, "received alarm at "+ Calendar.getInstance().getTime());
        Thread thread = new Thread(){
            @Override
            public void run(){
                Looper.prepare();
                //check whether gps is enabled or not
                if(gps.isGpsEnabled()){
                    Long startTime = Calendar.getInstance().getTimeInMillis();
                    //while either timeout occurs or we detect movement or no movement, run this loop
                    while(true){
                        Location currLocation = gps.getLastLocation();
                        if(currLocation != null){
                            // check for a movement for a interval instead of checking once
                            // getting the avg speed for some interval of time
                            float avgSpeed = currLocation.getSpeed();
                            Long lastTime = Calendar.getInstance().getTimeInMillis();
                            while(Calendar.getInstance().getTimeInMillis() - lastTime < intervalDuration){
                                avgSpeed += gps.getLastLocation().getSpeed();
                                avgSpeed /=2;
                            }
                            if(avgSpeed > SPEED_MARGIN){
                                // no need to start the service from here
                                // just change state of record switch and service will be started from there.
                                // sending an intent back to activity for changing the state of record switch

                                // stop alarm for autoStart Check
                                AlarmManagers.cancelAlarm(context, AUTO_START_RECORDING_CLASS);
                                TmpData.setStartAlarmRunning(false);
                                // start alarm for autoStop check
                                AlarmManagers.startAlarm(context, AUTO_STOP_RECORDING_CLASS);
                                TmpData.setStopAlarmRunning(true);

                                showNotification(context);
                                Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", true);
                                context.sendBroadcast(newIntent);
                            }
                            break;
                        }
                        // location given by gps is null i.e. GPS is not yet activated
                        else if(Math.abs(Calendar.getInstance().getTimeInMillis() - startTime) > gpsTimeout ){
                            //Log.d(TAG, "timeout occurred");
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
                    //Log.d(TAG, "Gps is disabled");
                }
                // quit thread when done with everything
                Looper.myLooper().quit();
            }
        };
        thread.start();
    }

    public void showNotification(Context context){
        NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Recording Started")
                .setContentText("Movement was detected. Recording Enabled");
        notificationManager.notify(0, builder.build());
    }

}
