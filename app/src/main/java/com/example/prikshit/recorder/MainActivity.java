package com.example.prikshit.recorder;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;


/**
 * Created on: 08-01-2015 by
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 *
 * the java file associated with the main Activity of the application
 * i.e. the Home Screen of the application
 */
public class MainActivity extends ActionBarActivity {
    private static boolean isDisplayDataEnabled = false;
    private static boolean recordDataEnabled = false;
    private boolean isBatterySaverEnabled = false;

    // Class corresponding to Recorder service
    public final Class serviceClassName = DataRecorderService.class;

    // Received Intent Properties
    public final String serviceIntentId =  "android.intent.action.MAIN";
    // name of variables received in intent from service
    public final String sensorDataIntentName = "sensorData";
    public final String gpsDataIntentName = "locationData";

    // To be sent Intent Properties
    // name of variable in intent to be sent to service for state of display Data button
    public final String displaySwitchIntentName = "displaySwitchChecked";
    // intent id to be sent to service
    public final String activityIntentId = "display-switch-state-change";

    // savedInstanceState Bundle variable names etc
    public final String displaySwitchStateBundleName = "displayDataSwitchState";
    public final String TAG  = "MainActivity";

    private Toolbar toolbar;
    private WifiManager wifiManager;
    private LocationManager locationManager;
    private BroadcastReceiver dataIntentReceiver;
    private BroadcastReceiver stateIntentReceiver;
   // Intent recorderIntent;
  //  Button bumpButton;// = (Button)findViewById(R.id.bump_button);
    //boolean isBump = false;
    int uploaderAlarmInterval = 60*60;//in secs;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      /*  if (!isServiceRunning(UploaderService.class)){
            Intent intent = new Intent(Intent.ACTION_SYNC, null, this, UploaderService.class);
           // startService(intent);
        }
    */
        UploaderAlarmReceiver alarm = new UploaderAlarmReceiver();
        alarm.setAlarm(this,uploaderAlarmInterval);//call uploader service after 10 minutes

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile",0);
        settings.getBoolean(displaySwitchStateBundleName, false);

        // for checking whether wifi is on or not.
        wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
      //  bumpButton = (Button)findViewById(R.id.bump_button);
       // bumpButton.setVisibility(View.INVISIBLE);
        //recorderIntent = new Intent(getBaseContext(),serviceClassName);
/*
        bumpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN) {
                    isBump = true;
                    if (isServiceRunning(serviceClassName)) {
                        System.out.println("isbump "+isBump);
                        recorderIntent.putExtra("bump",""+isBump);
                        startService(recorderIntent);
                    }
                }
                else{
                    isBump = false;
                    if (isServiceRunning(serviceClassName)) {
                        System.out.println("isbump "+isBump);
                        recorderIntent.putExtra("bump",""+isBump);
                        startService(recorderIntent);
                    }
                }

                //only the first call of startservice starts the service, all other times extras will be added
                //check this out
                //http://stackoverflow.com/questions/15346647/android-passing-variables-to-an-already-running-service
                return false;
            }
        });
*/
    };

    @Override
    public void onStart(){
        super.onStart();
    }

    /**
     * on Resume, value of variable displayDataEnabled will be automatically restored
     * and register the broadcast listener
     */
    @Override
    public void onResume() {
        super.onResume();
        registerButtonListeners(this);
        registerBroadcastListeners();

        if(MainActivity.recordDataEnabled) {
            if(!TmpData.isStopAlarmRunning()) {
                // Log.d(TAG, "recording enabled and no AUTOSTOP alarm running. Starting now");
                AlarmManagers.startAlarm(this, Constants.AUTO_STOP_RECORDING_CLASS);
                TmpData.setStopAlarmRunning(true);
            }
          //  bumpButton.setVisibility(View.VISIBLE);
        }
        else{
            if(!TmpData.isStartAlarmRunning()) {
                // Log.d(TAG,"recording disabled and no AUTOSTART alarm running. Starting now");
                AlarmManagers.startAlarm(this, Constants.AUTO_START_RECORDING_CLASS);
                TmpData.setStartAlarmRunning(true);
            }
         //   bumpButton.setVisibility(View.INVISIBLE);
        }
        broadcastDisplayInfo(isDisplayDataEnabled);
    }
/*
    public void registerBroadcastListeners(){
        // receiver for intent sent by service for displaying data
        dataIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSensorCard(intent.getStringExtra(sensorDataIntentName));
                updateGPSCard(intent.getStringExtra(gpsDataIntentName));
            }
        };
        this.registerReceiver(dataIntentReceiver, new IntentFilter(serviceIntentId));
        // receiver for intents sent by Auto Start and Stop
        stateIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d(TAG, "received intent for ");
                recordDataEnabled = intent.getBooleanExtra("recordingEnabled",false);
                Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
         //       bumpButton.setVisibility(View.VISIBLE);
                recordSwitch.setChecked(recordDataEnabled);
                if(!recordDataEnabled){
                    Switch displaySwitch = (Switch) findViewById(R.id.displaySwitch);
           //         bumpButton.setVisibility(View.INVISIBLE);
                    recordSwitch.setChecked(false);
                    isDisplayDataEnabled = false;
                }
            }
        };
        this.registerReceiver(stateIntentReceiver, new IntentFilter("auto.recording.state"));
    }
*/
    @Override
    public void onPause() {
        super.onPause();
        broadcastDisplayInfo(false);
        this.unregisterReceiver(this.dataIntentReceiver);
        this.unregisterReceiver(this.stateIntentReceiver);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isDisplayDataEnabled = false;
        broadcastDisplayInfo(false);
        //AlarmManagers.cancelAlarm(this, Constants.AUTO_START_RECORDING_CLASS);
        //AlarmManagers.cancelAlarm(this, Constants.AUTO_STOP_RECORDING_CLASS);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(displaySwitchStateBundleName,isDisplayDataEnabled);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        isDisplayDataEnabled =  savedInstanceState.getBoolean(displaySwitchStateBundleName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();//noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Wait for Next Version.", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_about) {
            startActivity(new Intent(this, About.class));
            //overridePendingTransition(R.anim.abc_right_in, R.anim.abc_left_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void registerButtonListeners(final Context context){
        // Initially, the CardView gpsDataCard and sensorData are not displayed
        final CardView sensorDataCard = (CardView) findViewById(R.id.sensorCard);
        final CardView gpsDataCard = (CardView) findViewById(R.id.gpsDataCard);
        if (!isDisplayDataEnabled) {
            //using GONE instead of INVISIBLE
            sensorDataCard.setVisibility(CardView.GONE);
            gpsDataCard.setVisibility(CardView.GONE);
        }
        // Switches and their Listeners
        final Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
        final Switch displaySwitch = (Switch) findViewById(R.id.displaySwitch);
        Switch batterySwitch = (Switch) findViewById(R.id.batterySwitch);

        // Before doing anything check whether the service is running or not
        if (isServiceRunning(serviceClassName)) {
            MainActivity.recordDataEnabled = true;
            recordSwitch.setChecked(true);
        }

        // check whether the GPS is turned on or not here only,instead of checking in the service.
        // after checking, start/stop service accordingly.
        // Record Data Switch Listener
        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                //Start the background Service
                if (isChecked) {
                    // if GPS is enabled, only then start the service, otherwise not
                    if (isGPSEnabled) {
                        // change the alarms accordingly
                      //  bumpButton.setVisibility(View.VISIBLE);
                        if(TmpData.isStartAlarmRunning()){
                            AlarmManagers.cancelAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
                            TmpData.setStartAlarmRunning(false);
                        }
                        if(!TmpData.isStopAlarmRunning()){
                            AlarmManagers.startAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
                            TmpData.setStopAlarmRunning(true);
                        }

                        Intent serviceIntent = new Intent(getBaseContext(), serviceClassName);
                        serviceIntent.setAction(Intent.ACTION_MAIN);
                        //Log.i("activityThread", String.valueOf(android.os.Process.myTid()));

                        startService(serviceIntent);
                        // if display data is switched on before record data, then send a broadcast intent to service mentioning
                        // that the display data is enabled so it should start sending data
                        broadcastDisplayInfo(true);
                    } else {
                        //show GPS settings
                        showGPSSettingsAlert();
                        //bumpButton.setVisibility(View.INVISIBLE);
                        // set the switched to off state
                        recordSwitch.setChecked(false);
                        MainActivity.recordDataEnabled = false;
                    }
                    // after all this, we ask user for WiFi permissions
                    if (!wifiManager.isWifiEnabled()) {
                        //showWiFiSettingsAlert();
                    }
                } else {
                   // bumpButton.setVisibility(View.INVISIBLE);
                    if(TmpData.isStopAlarmRunning()){
                        AlarmManagers.cancelAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
                        TmpData.setStopAlarmRunning(false);
                    }
                    if(!TmpData.isStartAlarmRunning()){
                        AlarmManagers.startAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
                        TmpData.setStartAlarmRunning(true);
                    }
                    stopService(new Intent(getBaseContext(), serviceClassName));
                }
                MainActivity.recordDataEnabled = isChecked;
            }
        });


        /**
         * Display Data Switch listeners
         * The idea behind adding a new Broadcast Listener is to stop sending the unnecessary intents from service
         * when the display data is switch off.
         * TODO:
         * in the next version we will remove the display data functionality all together
         */
        displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensorDataCard.setVisibility(CardView.VISIBLE);
                    gpsDataCard.setVisibility(CardView.VISIBLE);
                } else {
                    sensorDataCard.setVisibility(CardView.GONE);
                    gpsDataCard.setVisibility(CardView.GONE);
                }

                isDisplayDataEnabled = isChecked;
                broadcastDisplayInfo(isDisplayDataEnabled);
            }
        });
        /**
         batterySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
        Log.i("MainActivity", "Starting battery saver now");
        RecordingMode.setCurrentMode(Constants.BATTERY_SAVER_RECORDING_MODE);
        }
        else {
        Log.i("MainActivity", "Fast recording now");
        RecordingMode.setCurrentMode(Constants.FAST_RECORDING_MODE);
        }
        }
        });
         */
    }

    public void registerBroadcastListeners(){
        // receiver for intent sent by service for displaying data
        dataIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateSensorCard(intent.getStringExtra(sensorDataIntentName));
                updateGPSCard(intent.getStringExtra(gpsDataIntentName));
            }
        };
        this.registerReceiver(dataIntentReceiver, new IntentFilter(serviceIntentId));
        // receiver for intents sent by Auto Start and Stop
        stateIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.d(TAG, "received intent for ");
                recordDataEnabled = intent.getBooleanExtra("recordingEnabled",false);
                Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
                recordSwitch.setChecked(recordDataEnabled);
                if(!recordDataEnabled){
                    Switch displaySwitch = (Switch) findViewById(R.id.displaySwitch);
                    recordSwitch.setChecked(false);
                    isDisplayDataEnabled = false;
                }
            }
        };
        this.registerReceiver(stateIntentReceiver, new IntentFilter("auto.recording.state"));
    }

    /**
     * update the sensor data onto the sensorDataCard
     * List Format:
     * Accel(3), Gyro(3), Magneto(3), Light(1)
     * All data should be in String format
     *
     * @param sensorData
     */
    public void updateSensorCard(String sensorData) {
        if(sensorData != null || sensorData != "") {
            TextView accelData = (TextView) findViewById(R.id.accelData);
            TextView gyroData = (TextView) findViewById(R.id.gyroData);
            TextView magnetoData = (TextView) findViewById(R.id.magnetoData);
            TextView lightData = (TextView) findViewById(R.id.lightData);
            String[] array = sensorData.split(",");

            // in place of 0 , there is timestamp now.
            accelData.setText(array[1] + "," + array[2] + "," + array[3] + " m/s2");
            gyroData.setText(array[4] + "," + array[5] + "," + array[6] + " rad/s");
            magnetoData.setText(array[7] + "," + array[8] + "," + array[9] + " μT");
            lightData.setText(array[10] + " lux");
        }
    }

    /**
     * Updates the GPS information on GPS CardView
     * Format:
     * latitude, longitude, accuracy, altitude, speed, time
     *
     * @param locationData
     */
    public void updateGPSCard(String locationData) {
        if ( !locationData.isEmpty() ) {
            String array[] = locationData.split(",");
            TextView latitude = (TextView) findViewById(R.id.latitudeData);
            TextView longitude = (TextView) findViewById(R.id.longitudeData);
            TextView accuracy = (TextView) findViewById(R.id.accuracyData);
            TextView altitude = (TextView) findViewById(R.id.altitudeData);
            TextView speed = (TextView) findViewById(R.id.speedData);
            TextView time = (TextView) findViewById(R.id.timeData);

            latitude.setText(array[0]);
            longitude.setText(array[1]);
            accuracy.setText(array[2] + " m");
            altitude.setText(array[3] + " m");
            speed.setText(array[4] + " m/s");
            time.setText(array[5]);
        }
    }

    /**
     * Checks whether the service associated with the app is running or not.
     * GLOBAL Variable: serviceClassName: class associated with the service running in background
     * @return true if service is running, false otherwise
     */
    public boolean isServiceRunning(Class serviceClassName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName.getName().equals(runningService.service.getClassName())) return true;
        }
        return false;
    }

    /**
     * the GPS is disabled alert dialog box
     * also, allows to go to settings to change the location settings
     */
    public void showGPSSettingsAlert(){
        AlertDialog.Builder alertDialog =  new AlertDialog.Builder(this);
        alertDialog.setTitle("Change Location Settings?");
        alertDialog.setMessage("Location has been disabled. Do you want to go to settings to switch it on?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            /**
             * What happens when user clicks on settings.
             * @param dialog
             * @param which
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Show the location settings to user when settings is pressed
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            //when click button is pressed
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    /**
     * This alert dialog box is shown when WiFi is disabled.
     */
    public void showWiFiSettingsAlert(){
        AlertDialog.Builder wifiDialog = new AlertDialog.Builder(this);
        wifiDialog.setTitle("Switch on WiFi?");
        wifiDialog.setMessage("WiFi access is required for better results. Do you want to switch it on? (Optional)");
        wifiDialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            /**
             * What happens when user clicks on settings.
             * @param dialog
             * @param which
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                wifiManager.setWifiEnabled(true);
                dialog.cancel();
            }
        });

        wifiDialog.setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
            //when click button is pressed
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        wifiDialog.show();
    }

    /**
     * broadcast the display switch state for to be received by the service
     *@param broadcastInfo a boolean variable for broadcasting the state of DisplayData Switch to service
     */
    public void broadcastDisplayInfo(boolean broadcastInfo){
        Intent intent = new Intent(activityIntentId).putExtra(displaySwitchIntentName, broadcastInfo);
        getApplicationContext().sendBroadcast(intent);
    }

    public static boolean isRecordDataEnabled() {
        return recordDataEnabled;
    }

    public static void setRecordDataEnabled(boolean recordDataEnabled) {
        MainActivity.recordDataEnabled = recordDataEnabled;
    }

    /**
     * This will cancel all the alarms and hence no data will be recorded
     * @param context
     */
    public void cancelAllAlarms(Context context){
        AlarmManagers.cancelAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
        AlarmManagers.cancelAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
        MainActivity.setRecordDataEnabled(false);
    }

}
