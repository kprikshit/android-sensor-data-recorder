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

    @Override
    public void onReceive(Context original, Intent baseIntent) {
        final Context context = original;
        final CustomGPS gps = new CustomGPS(context);
        Thread thread = new Thread(){
            @Override
            public void run(){
                Looper.prepare();
                Handler handler = new Handler();
                //Log.i(TAG,"start receiver thread id: " + android.os.Process.myTid());
                //Log.d(TAG, "gps enabled");
                if(gps.isGpsEnabled()){

                    Long startTime = Calendar.getInstance().getTimeInMillis();
                    while(true){
                        Location currLocation = gps.getLastLocation();
                        if(currLocation != null){
                            //Log.d(TAG, "gps not null received");
                            boolean movementDetected = checkMovementNow(currLocation, context);
                            if(movementDetected){
                                //Log.d(TAG ,"movement is there");
                                gps.unregisterListener();
                                // no need to start the service from here
                                // just change state of record switch and service will be started from there.

                                // Intent serviceIntent = new Intent(context, RECORDING_CLASS);
                                // serviceIntent.setAction(Intent.ACTION_MAIN);
                                // context.startService(serviceIntent);
                            }
                            break;
                        }
                        else if(Math.abs(Calendar.getInstance().getTimeInMillis() - startTime) > GPS_TIMEOUT ){
                            //Log.d(TAG, "timeout occurred");
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //Log.d(TAG,"out of loop");
                    gps.unregisterListener();
                }
                else{
                    //Log.d(TAG, "Gps is disabled");
                }
            }
        };
        thread.start();
    }

    /**
     * this function checks for the movement given current location
     * @param currLocation
     * @param context
     */
    public boolean checkMovementNow(Location currLocation, Context context){
        Log.d(TAG, String.valueOf(currLocation.getSpeed()));
        if(currLocation.getSpeed() > SPEED_MARGIN){
            //Log.d(TAG, "Movement Detected. Recording has been started");

            // stop alarm for autoStart Check
            AlarmManagers.cancelAlarm(context, AUTO_START_RECORDING_CLASS);
            TmpData.setStartAlarmRunning(false);
            // start alarm for autoStop check
            AlarmManagers.startAlarm(context, AUTO_STOP_RECORDING_CLASS);
            TmpData.setStopAlarmRunning(true);

            showNotification(context);

            // sending an intent back to activity for changing the state of record switch
            Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", true);
            context.sendBroadcast(newIntent);
            return true;
        }
        else{
            //Log.d(TAG, "No Movement from last time.");
            return false;
        }
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
