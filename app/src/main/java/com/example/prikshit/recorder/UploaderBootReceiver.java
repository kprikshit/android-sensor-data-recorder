package com.example.prikshit.recorder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Created by Pankaj on 2/8/2015.
 */
//on phone boot set alarm for the UploaderService
public class UploaderBootReceiver extends BroadcastReceiver {
    UploaderAlarmReceiver alarm = new UploaderAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context,60*60);
        }
    }

}

