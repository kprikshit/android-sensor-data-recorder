package com.example.prikshit.recorder;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by Prikshit on 08-01-2015.
 */
public class DataRecorderService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this,"Started",Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this,"Stopped",Toast.LENGTH_SHORT).show();
    }
}
