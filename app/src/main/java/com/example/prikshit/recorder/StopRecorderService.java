package com.example.prikshit.recorder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import static com.example.prikshit.recorder.Constants.*;
import static java.util.Calendar.*;

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
public class StopRecorderService extends BroadcastReceiver {
    private final String TAG = "autoStopRecording";

    @Override
    public void onReceive(Context context, Intent intent) {
        Location currLocation = CustomGPS.getLastLocation();
        if (currLocation != null) {
            if (currLocation.getSpeed() < SPEED_MARGIN) {

                // stop autoStop alarm
                AlarmManagers.cancelAlarm(context, AUTO_STOP_RECORDING_CLASS);
                TmpData.setStopAlarmRunning(false);
                // start autoStart Alarm
                AlarmManagers.startAlarm(context, AUTO_START_RECORDING_CLASS);
                TmpData.setStartAlarmRunning(true);

                //Log.d(TAG, "stopping now at " + getInstance().getTime());
                // no need to stop service from here
                // it will be done by the listener in the activity
                //context.stopService(new Intent(context, Constants.RECORDING_CLASS));
                // set the last location as null after the service has stopped.
                CustomGPS.setLastLocation(null);

                // sending a broadcast intent back to activity
                Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", false);
                context.sendBroadcast(newIntent);

                showNotification(context);
            } else {
                //Log.d(TAG, "device is moving. No need to stop");
            }
        } else {
            //Log.d(TAG, "GPS not yet activated");
        }
    }


    public void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Recording Disabled")
                .setContentText("No Movement Detected. Recording Stopped");
        notificationManager.notify(1, builder.build());
    }

}
