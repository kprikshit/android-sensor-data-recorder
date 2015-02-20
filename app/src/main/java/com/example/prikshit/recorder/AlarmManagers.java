package com.example.prikshit.recorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import static com.example.prikshit.recorder.Constants.*;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.getInstance;

/**
 * A global class with static function for various alarm Manager functions according to class
 * (AUTO_STOP and AUTO_START recording class only)
 *
 * Created on: 15-02-2015 by
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 */
public class AlarmManagers {
    public static final String TAG = "AlarmManagers";

    public static void startAlarm(Context context,Class <?> className){
        Intent intent;
        if(className == AUTO_START_RECORDING_CLASS) {
            intent = new Intent(context, AUTO_START_RECORDING_CLASS);
            //Log.d(TAG, "start alarm initialized");
        }
        else {
            intent = new Intent(context, AUTO_STOP_RECORDING_CLASS);
            //Log.d(TAG, "stop alarm initialized");
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager  = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // first time when the alarm is started
        long firstStartTime  = Calendar.getInstance().getTimeInMillis();

        if(className == AUTO_START_RECORDING_CLASS) {
            alarmManager.setRepeating(AlarmManager.RTC, firstStartTime + CHECK_START_INTERVAL, CHECK_START_INTERVAL, pendingIntent);
        }
        else
            alarmManager.setRepeating(AlarmManager.RTC, firstStartTime + CHECK_STOP_INTERVAL, CHECK_STOP_INTERVAL, pendingIntent);

    }

    /**
     * No need to use this function, unless we want to change duration of alarm after setting it.
     * @param context
     * @param intent
     * @param className
     * @param nextAlarmInterval
     */
    public static void setNextAlarm(Context context, Intent intent, Class <?> className, long nextAlarmInterval){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent ;
        if(className == AUTO_START_RECORDING_CLASS )
            newIntent = new Intent(context, AUTO_START_RECORDING_CLASS);
        else
            newIntent = new Intent(context, AUTO_STOP_RECORDING_CLASS);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar cal = getInstance();
        cal.add(SECOND, (int)nextAlarmInterval/1000);
        alarmManager.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), nextAlarmInterval, pending);
    }

    public static void cancelAlarm(Context context, Class className) {
        Intent intent = new Intent(context, className);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

}
