package com.prikshit.recorder.activities;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.prikshit.recorder.R;
import com.prikshit.recorder.constants.Constants;
import com.prikshit.recorder.main.AlarmManagers;
import com.prikshit.recorder.main.Logger;
import com.prikshit.recorder.main.TmpData;

import java.io.File;


/**
 * Created on: 08-01-2015 by
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 *
 * the java file associated with the main Activity of the application
 * i.e. the Home Screen of the application
 */
public class Home extends ActionBarActivity {
    private static boolean displaySwitchEnabled = false;
    private static boolean recordSwitchEnabled = false;
    private boolean batterySwitchEnabled = false;

    // Class corresponding to Recorder service
    public final Class serviceClassName = Constants.RECORDING_CLASS;

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
    public final String TAG  = "Home";

    private Toolbar toolbar;
    private WifiManager wifiManager;
    private LocationManager locationManager;
    private BroadcastReceiver dataIntentReceiver;
    private BroadcastReceiver stateIntentReceiver;
    int uploaderAlarmInterval = 60*60;//in secs;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // creating Directory Data_Recorder here
        File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DIRECTORY);
        if(sdDirectory.exists() && sdDirectory.isDirectory()){
            //directory is present, no need to do anything here
        }
        else{
            sdDirectory.mkdirs();
        }
        // for checking whether wifi is on or not.
        wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        updateConstants();
        Logger.i(TAG, "Home created");
    };

    @Override
    public void onStart(){
        super.onStart();
        registerButtonListeners(this);
        startStopAlarms();
    }


    /**
     * on Resume, value of variable displayDataEnabled will be automatically restored
     * and register the broadcast listener
     */
    @Override
    public void onResume() {
        super.onResume();
        updateRecordSwitch();
        registerBroadcastListeners();
        broadcastDisplaySwitchState(displaySwitchEnabled);
    }

    /**
     * Start/ Stop alarm for automatic recording
     */
    public void startStopAlarms(){
        // start uploader alarm
        AlarmManagers.startUploaderAlarm(this);
        // low probability of this case happening
        // Case: When recording is running but auto stop alarm is not running
        if(recordSwitchEnabled) {
            if(!TmpData.isStopAlarmRunning()) {
                Logger.i(TAG, "Recording enabled but stop alarm not running. Starting Now");
                AlarmManagers.startAlarm(this, Constants.AUTO_STOP_RECORDING_CLASS);
                TmpData.setStopAlarmRunning(true);
            }
            //  bumpButton.setVisibility(View.VISIBLE);
        }
        else{
            if(!TmpData.isStartAlarmRunning()) {
                Logger.i(TAG, "Recording is disabled. Starting auto start alarm now");
                AlarmManagers.startAlarm(this, Constants.AUTO_START_RECORDING_CLASS);
                TmpData.setStartAlarmRunning(true);
            }
            //   bumpButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister broadcast receivers
        this.unregisterReceiver(this.dataIntentReceiver);
        this.unregisterReceiver(this.stateIntentReceiver);
        broadcastDisplaySwitchState(false);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        displaySwitchEnabled = false;
        broadcastDisplaySwitchState(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(displaySwitchStateBundleName, displaySwitchEnabled);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        displaySwitchEnabled =  savedInstanceState.getBoolean(displaySwitchStateBundleName);
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
            //Toast.makeText(this, "Wait for Next Version.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Settings.class));
            return true;
        }
        if (id == R.id.action_about) {
            startActivity(new Intent(this, About.class));
            //overridePendingTransition(R.anim.abc_right_in, R.anim.abc_left_out);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * this will update the switch state based on whether the recording service is running or not
     */
    public void updateRecordSwitch(){
        final Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
        if (isServiceRunning(serviceClassName)) {
            recordSwitchEnabled = true;
            recordSwitch.setChecked(true);
            TmpData.recordingOn = true;
        }
        else{
            recordSwitch.setChecked(false);
            TmpData.recordingOn = false;
        }
    }

    public void registerButtonListeners(final Context context){
        displayDataSwitchListener();
        // Switches and their Listeners
        final Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
        Switch batterySwitch = (Switch) findViewById(R.id.batterySwitch);

        // Before doing anything check whether the service is running or not
        updateRecordSwitch();
        // check whether the GPS is turned on or not here only,instead of checking in the service.
        // after checking, start/stop service accordingly.
        // Record Data Switch Listener
        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                //Start the background Service
                Logger.d(TAG+"/recordSwitchListener", "record switch state changed to " +isChecked);
                if (isChecked) {
                    // if GPS is enabled, only then start the service, otherwise not
                    if (isGPSEnabled) {
                        // change the alarms accordingly
                      //  bumpButton.setVisibility(View.VISIBLE);
                        if(TmpData.isStartAlarmRunning()){
                            Logger.d(TAG+"/recordSwitchListener","cancelling AutoStart alarm");
                            AlarmManagers.cancelAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
                            TmpData.setStartAlarmRunning(false);
                        }
                        if(!TmpData.isStopAlarmRunning()){
                            Logger.d(TAG+"/recordSwitchListener","starting AutoStop alarm");
                            AlarmManagers.startAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
                            TmpData.setStopAlarmRunning(true);
                        }
                        // if recording is not on, then start the recording, other wise not
                        if(!TmpData.recordingOn) {
                            Intent serviceIntent = new Intent(getBaseContext(), serviceClassName);
                            serviceIntent.setAction(Intent.ACTION_MAIN);

                            Logger.i(TAG + "/recordSwitchListener", "recorder service started");
                            startService(serviceIntent);
                            TmpData.recordingOn = true;
                            // if display data is switched on before record data, then send a broadcast intent to service mentioning
                            // that the display data is enabled so it should start sending data
                            if (displaySwitchEnabled)
                                broadcastDisplaySwitchState(true);
                        }
                    } else {
                        //show GPS settings
                        showGPSSettingsAlert();
                        // bumpButton.setVisibility(View.INVISIBLE);
                        // set the switched to off state
                        recordSwitch.setChecked(false);
                        Home.recordSwitchEnabled = false;
                    }
                    // after all this, we ask user for WiFi permissions
                    if (!wifiManager.isWifiEnabled()) {
                        //showWiFiSettingsAlert();
                    }
                } else {
                    if(TmpData.isStopAlarmRunning()){
                        Logger.d(TAG+"/recordSwitchListener","cancelling AutoStop alarm");
                        AlarmManagers.cancelAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
                        TmpData.setStopAlarmRunning(false);
                    }
                    if(!TmpData.isStartAlarmRunning()){
                        Logger.d(TAG+"/recordSwitchListener","starting AutoStart alarm");
                        AlarmManagers.startAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
                        TmpData.setStartAlarmRunning(true);
                    }
                    if(TmpData.recordingOn) {
                        Logger.i(TAG + "/recordSwitchListener", "recorder service stopped");
                        stopService(new Intent(getBaseContext(), serviceClassName));
                        TmpData.recordingOn = false;
                    }
                }
                Home.recordSwitchEnabled = isChecked;
            }
        });


        /**
         batterySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
        Log.i("Home", "Starting battery saver now");
        RecordingMode.setCurrentMode(Constants.BATTERY_SAVER_RECORDING_MODE);
        }
        else {
        Log.i("Home", "Fast recording now");
        RecordingMode.setCurrentMode(Constants.FAST_RECORDING_MODE);
        }
        }
        });
         */
    }

    /**
     * Display Data Switch listeners
     * The idea behind adding a new Broadcast Listener is to stop sending the unnecessary intents from service
     * when the display data is switch off.
     */
    public void displayDataSwitchListener(){
        final Switch displaySwitch = (Switch) findViewById(R.id.displaySwitch);
        // Initially, the CardView gpsDataCard and sensorData are not displayed
        final CardView sensorDataCard = (CardView) findViewById(R.id.sensorCard);
        final CardView gpsDataCard = (CardView) findViewById(R.id.gpsDataCard);

        if (!displaySwitchEnabled) {
            sensorDataCard.setVisibility(CardView.GONE);
            gpsDataCard.setVisibility(CardView.GONE);
        }

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
                displaySwitchEnabled = isChecked;
                broadcastDisplaySwitchState(displaySwitchEnabled);
            }
        });
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
                recordSwitchEnabled = intent.getBooleanExtra("recordingEnabled",false);
                Logger.d(TAG+"/recordSwitchStateReceiver", "Recording switch state received as "+ recordSwitchEnabled);
                Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
                recordSwitch.setChecked(recordSwitchEnabled);
                if(!recordSwitchEnabled){
                    Switch displaySwitch = (Switch) findViewById(R.id.displaySwitch);
                    displaySwitchEnabled = false;
                    displaySwitch.setChecked(false);
                    broadcastDisplaySwitchState(false);
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
            //TextView lightData = (TextView) findViewById(R.id.lightData);
            String[] array = sensorData.split(",");

            // in place of 0 , there is timestamp now.
            accelData.setText(array[1] + "," + array[2] + "," + array[3] + " m/s2");
            gyroData.setText(array[4] + "," + array[5] + "," + array[6] + " rad/s");
            magnetoData.setText(array[7] + "," + array[8] + "," + array[9] + " μT");
            //lightData.setText(array[10] + " lux");
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
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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
     * broadcast the display switch state for to be received by the service
     *@param broadcastInfo a boolean variable for broadcasting the state of DisplayData Switch to service
     */
    public void broadcastDisplaySwitchState(boolean broadcastInfo){
        Intent intent = new Intent(activityIntentId).putExtra(displaySwitchIntentName, broadcastInfo);
        getApplicationContext().sendBroadcast(intent);
    }

    /**
     * This will cancel all the alarms and hence no data will be recorded
     * @param context
     */
    public void cancelAllAlarms(Context context){
        AlarmManagers.cancelAlarm(context, Constants.AUTO_STOP_RECORDING_CLASS);
        AlarmManagers.cancelAlarm(context, Constants.AUTO_START_RECORDING_CLASS);
        recordSwitchEnabled = false;
        TmpData.setStopAlarmRunning(false);
        TmpData.setStartAlarmRunning(false);
    }

    /**
     * update constants based on values stored in shared preferences
     */
    public void updateConstants(){
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        Constants.BATTERY_SAVER_LEVEL = Integer.parseInt(sharedPreferences.getString("lowBatteryLevel","30"));
        Constants.CHECK_START_INTERVAL = 60*1000*(Integer.parseInt(sharedPreferences.getString("checkStartInterval","60")));
        Constants.CHECK_STOP_INTERVAL = 60*1000*(Integer.parseInt(sharedPreferences.getString("checkStopInterval","30")));
        Constants.SPEED_THRESHOLD = (Float.parseFloat(sharedPreferences.getString("speedThreshold", "8.0f")));

        Constants.NOTIFICATION_ENABLED = sharedPreferences.getBoolean("notifications_auto_start_stop",false);
        Constants.LOGGING_ENABLED = sharedPreferences.getBoolean("loggingEnabled",false);

        Constants.SERVER_ADDRESS = sharedPreferences.getString("serverAddress","10.1.201.41");
        Constants.UPLOADER_INTERVAL = 60*1000*Integer.parseInt(sharedPreferences.getString("upload_frequency","60"));
        Constants.MIN_UPLOAD_SIZE_LIMIT = 1024*1024*(Integer.parseInt(sharedPreferences.getString("min_upload_file_size","100")));

    }

}
