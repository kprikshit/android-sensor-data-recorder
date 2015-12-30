package com.prikshit.recorder.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prikshit.recorder.constants.Constants;


/**
 * Created on: 27-03-2015 by
 * Prikshit Kumar
 * kprikshit@iitrpr.ac.in
 */
public class NotificationReceiver extends BroadcastReceiver{
    private String TAG = "notificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "received");
        int notificationId = intent.getIntExtra("notId",0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // dismiss notification
        notificationManager.cancel(notificationId);
        // cancel upload to server
        context.stopService(new Intent(context, Constants.UPLOADER_CLASS));
    }
}
