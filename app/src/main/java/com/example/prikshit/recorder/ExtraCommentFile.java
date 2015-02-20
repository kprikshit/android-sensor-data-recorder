package com.example.prikshit.recorder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static com.example.prikshit.recorder.Constants.AUTO_START_RECORDING_CLASS;
import static com.example.prikshit.recorder.Constants.AUTO_STOP_RECORDING_CLASS;
import static com.example.prikshit.recorder.Constants.SPEED_MARGIN;
import static java.util.Calendar.getInstance;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 18-01-2015
 */
public class ExtraCommentFile {
}


/**
 * package com.example.parmeetsingh.wifinetwork;

 import java.util.ArrayList;
 import java.util.List;

 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;

 public class MainActivity extends Activity {
 TextView mainText;
 WifiManager mainWifi;
 WifiReceiver receiverWifi;
 List<ScanResult> wifiList;
 StringBuilder sb = new StringBuilder();

 @Override
 protected void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.activity_main);
 mainText = (TextView) findViewById(R.id.tv1);
 mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
 if (mainWifi.isWifiEnabled() == false)
 {
 // If wifi disabled then enable it
 Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
 Toast.LENGTH_LONG).show();

 mainWifi.setWifiEnabled(true);
 }
 receiverWifi = new WifiReceiver();
 registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 mainWifi.startScan();
 mainText.setText("Starting Scan...");
 }

 public boolean onCreateOptionsMenu(Menu menu) {
 menu.add(0, 0, 0, "Refresh");
 return super.onCreateOptionsMenu(menu);
 }

 public boolean onMenuItemSelected(int featureId, MenuItem item) {
 mainWifi.startScan();
 mainText.setText("Starting Scan");
 return super.onMenuItemSelected(featureId, item);
 }

 protected void onPause() {
 unregisterReceiver(receiverWifi);
 super.onPause();
 }

 protected void onResume() {
 registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 super.onResume();
 }

 // Broadcast receiver class called its receive method
 // when number of wifi connections changed

 class WifiReceiver extends BroadcastReceiver {

 // This method call when number of wifi connections changed
 public void onReceive(Context c, Intent intent) {

 sb = new StringBuilder();
 wifiList = mainWifi.getScanResults();
 sb.append("\nNumber Of Wifi connections :"+wifiList.size()+"\n\n");

 //List<String> networkList = new ArrayList<String>();
 int counter=0;
 for(ScanResult result : wifiList){
 int level = WifiManager.calculateSignalLevel(result.level, 5) + 1;
 sb.append(new Integer(++counter).toString() + ". ");
 sb.append(result.SSID + " ");
 sb.append(new Integer(level).toString());
 sb.append("\n\n");
 //      networkList.add(content);
 }
 for(int i = 0; i < wifiList.size(); i++){

 sb.append(new Integer(i+1).toString() + ". ");
 sb.append((wifiList.get(i)).toString());
 sb.append("\n\n");
 }
mainText.setText(sb);
        }

        }
        }
 */


/*
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
public class StartRecorderService extends BroadcastReceiver {
    private final String TAG = "autoStartRecording";
    // timeout limit for checking for gps signal
    private final int gpsTimeout = 25*1000;

    @Override
    public void onReceive(Context original, Intent baseIntent) {
        final Context context = original;
        final Intent intent = baseIntent;
        final CustomGPS gps = new CustomGPS(context);
        Thread thread = new Thread(){
            @Override
            public void run(){
                Looper.prepare();
                Handler handler = new Handler();
                Log.i(TAG, "start receiver thread id: " + android.os.Process.myTid());
                Log.d(TAG, "gps enabled");
                // if gps is enabled, we check for movement
                // else we schedule the next alarm for twice the normal time.
                if(gps.isGpsEnabled()){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // getting required information
                    Long startTime = Calendar.getInstance().getTimeInMillis();
                    while(true){
                        Location currLocation = gps.getLastLocation();
                        if(currLocation != null){
                            Log.d(TAG, "not null value of gps");
                            boolean check = checkMovementNow(currLocation, context, intent);
                            if(check){
                                gps.setLastLocation(null);
                                gps.unregisterListener();
                                // start service
                                Intent serviceIntent = new Intent(context, RECORDING_CLASS);
                                serviceIntent.setAction(Intent.ACTION_MAIN);
                                context.startService(serviceIntent);
                            }
                            break;
                        }
                        // TIMEOUT for GPS
                        else if(Math.abs(Calendar.getInstance().getTimeInMillis() - startTime) > gpsTimeout ){
                            Log.d(TAG, "timeout occurred");
                            break;
                        }
                        try {
                            Thread.sleep(3500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //Log.d(TAG, "inside current loop");
                    }
                    Log.d(TAG,"out of loop");
                    gps.unregisterListener();
                }
                else{
                    Log.d(TAG, "Gps is disabled");
                }
            }
        };
        thread.start();
    }

    public void writeToFile(Location newLocation, Context context){
        String locData = newLocation.getLatitude()+","+newLocation.getLongitude()+"\n";
        try {
            FileOutputStream fos = context.openFileOutput(Constants.TMP_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(locData.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkMovementNow(Location currLocation, Context context, Intent intent){
        Double[] lastRecordedData = readFromFile(context);
        double latMargin = Math.abs(currLocation.getLatitude() - lastRecordedData[0]);
        double lngMargin = Math.abs(currLocation.getLongitude() - lastRecordedData[1]);
        // movement has occurred from last time
        //if( latMargin > LAT_LNG_MARGIN && lngMargin > LAT_LNG_MARGIN){
        Log.d(TAG, String.valueOf(currLocation.getSpeed()));
        if(currLocation.getSpeed() > SPEED_MARGIN){
            // stop alarm for autoStart Check
            AlarmManagers.cancelAlarm(context, AUTO_START_RECORDING_CLASS);
            TmpData.setStartAlarmRunning(false);
            Log.d(TAG, "Movement Detected. Recording has been started");
            // start alarm for autoStop check
            AlarmManagers.startAlarm(context, AUTO_STOP_RECORDING_CLASS);
            TmpData.setStopAlarmRunning(true);

            // showing notification showing that the service has been started
            NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("Recording Started")
                    .setContentText("Movement was detected. Recording Enabled");
            notificationManager.notify(0, builder.build());
            // sending an intent back to activity
            Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", true);
            context.sendBroadcast(newIntent);

            writeToFile(currLocation,context);
            return true;
        }
        else{
            writeToFile(currLocation,context);
            // no movement has occurred, set alarm for normal time now.
            //AlarmManagers.setNextAlarm(context, intent, Constants.AUTO_START_RECORDING_CLASS, Constants.CHECK_START_INTERVAL);
            Log.d(TAG, "No Movement from last time.");
            return false;
        }
        // store the current location into the file
    }

    public Double[] readFromFile(Context context){
        Double lastLocationData[]= new Double[2];
        try {
            FileInputStream fis = context.openFileInput(Constants.TMP_FILE_NAME);
            int c;
            String tmp = "";
            while( (c = fis.read()) != -1) tmp = tmp + Character.toString((char)c);
            if(tmp != "") {
                String[] result = tmp.split(",");
                lastLocationData[0] = Double.valueOf(result[0]);
                lastLocationData[1] = Double.valueOf(result[1]);
            }
            else{
                lastLocationData[0] = 0.00;
                lastLocationData[1] = 0.00;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastLocationData;
    }
}


/**
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
public class StopRecorderService extends BroadcastReceiver {
    private final String TAG = "autoStopRecording";

    @Override
    public void onReceive(Context context, Intent intent) {
        // checking location change now
        Location currLocation = CustomGPS.getLastLocation();
        if(currLocation != null ) {
            Double[] lastData = readFromFile(context);
            double latError = Math.abs(lastData[0] - currLocation.getLatitude());
            double lngError = Math.abs(lastData[1] - currLocation.getLongitude());
            //if (latError < LAT_LNG_MARGIN && lngError < LAT_LNG_MARGIN) {
            if(currLocation.getSpeed() < SPEED_MARGIN){
                Log.d(TAG, "stopping now at " + getInstance().getTime());

                context.stopService(new Intent(context, Constants.RECORDING_CLASS));
                // sending a broadcast intent back to activity
                Intent newIntent = new Intent("auto.recording.state").putExtra("recordingEnabled", false);
                context.sendBroadcast(newIntent);

                // showing notification showing that the service has been started
                NotificationManager notificationManager  = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Recording Disabled")
                        .setContentText("No Movement Detected. Recording Stopped");
                notificationManager.notify(1, builder.build());
                // stop autoStop alarm
                CustomGPS.setLastLocation(null);
                AlarmManagers.cancelAlarm(context, AUTO_STOP_RECORDING_CLASS);
                TmpData.setStopAlarmRunning(false);
                // start autoStart Alarm
                AlarmManagers.startAlarm(context, AUTO_START_RECORDING_CLASS);
                TmpData.setStartAlarmRunning(true);

            } else {
                Log.d(TAG,"device is moving. No need to stop");
                // still some movement. schedule for next time.
                //AlarmManagers.setNextAlarm(context, intent, AUTO_STOP_RECORDING_CLASS, CHECK_STOP_INTERVAL);
            }
            writeToFile(currLocation, context);
        }
        else{
            if(MainActivity.isRecordDataEnabled())
                //AlarmManagers.setNextAlarm(context, intent, AUTO_STOP_RECORDING_CLASS, CHECK_STOP_INTERVAL);
                Log.d(TAG,"GPS not yet activated");
        }
    }

    public void writeToFile(Location newLocation, Context context){
        String locData = newLocation.getLatitude()+","+newLocation.getLongitude()+"\n";
        try {
            // before writing to file, delete the content
            FileOutputStream fos = context.openFileOutput(Constants.TMP_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(locData.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Double[] readFromFile(Context context){
        Double data[]= new Double[2];
        try {
            FileInputStream fis = context.openFileInput(Constants.TMP_FILE_NAME);
            int c;
            String tmp = "";
            while( (c = fis.read()) != -1) tmp = tmp + Character.toString((char)c);
            if(tmp != "") {
                String[] result = tmp.split(",");
                data[0] = Double.valueOf(result[0]);
                data[1] = Double.valueOf(result[1]);
            }
            else{
                data[0] = 0.00;
                data[1] = 0.00;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

}
*/