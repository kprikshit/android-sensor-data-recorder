package com.example.prikshit.recorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Toast;

/**
 * Created by Pankaj on 2/8/2015.
 */
public class UploaderAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //start the UploaderService class
        System.out.println("receive");

        context.startService(new Intent(context,UploaderService.class));
    }

    //
    public void setAlarm(Context context,int repeatInterval)
    {
        //start the UploaderService class at regular intervals through an intent on this class itself
        System.out.println("ser alarm");

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,UploaderAlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),repeatInterval * 1000, alarmIntent);
    }
}