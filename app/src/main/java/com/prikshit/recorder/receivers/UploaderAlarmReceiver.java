package com.prikshit.recorder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prikshit.recorder.constants.Constants;


/**
 * this receives the alarm created for the uploader service and starts that service
 *
 * Created by Pankaj on 2/8/2015.
 */
public class UploaderAlarmReceiver extends BroadcastReceiver {
    private static String TAG = "uploaderAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("uploader alarm received");
        //start the UploaderService class on receiving the alarm
        context.startService(new Intent(context, Constants.UPLOADER_CLASS));
    }

}