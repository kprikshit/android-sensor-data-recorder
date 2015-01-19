package com.example.prikshit.recorder;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 *
 * the java file associated with the main Activity of the application
 * i.e. the Home Screen of the application
 */
public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private boolean isDisplayDataEnabled = false;
    private boolean isRecordDataEnabled = false;
    private BroadcastReceiver receiver;
    IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
    private WifiManager wifiManager;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        // for checking whether wifi is on or not.
        wifiManager = (WifiManager) this.getSystemService(this.WIFI_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        /**
         * Initially, the CardView gpsDataCard and sensorData are not displayed
         */
        final CardView sensorDataCard = (CardView) findViewById(R.id.sensorCard);
        final CardView gpsDataCard = (CardView) findViewById(R.id.gpsDataCard);
        if (!isDisplayDataEnabled) {
            sensorDataCard.setVisibility(CardView.INVISIBLE);
            gpsDataCard.setVisibility(CardView.INVISIBLE);
        }

        /**
         * Switches and their Listeners
         */
        final Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
        Switch displaySwitch = (Switch) findViewById(R.id.displaySwitch);

        /**
         * Before doing anything check whether the service is running or not
         */
        if (isServiceRunning(DataRecorderService.class)) {
            isRecordDataEnabled = true;
            recordSwitch.setChecked(true);
        }

        //check whether the GPS is turned on or not here only,instead of checking in the service.
        // after checking, start/stop service accordingly.
        /**
         * Record Data Switch Listener
         */
        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isRecordDataEnabled = isChecked;
                //Start the background Service
                if (isChecked) {
                    /**
                     * if GPS is enabled, only then start the service,
                     * otherwise not
                     */
                    if (isGPSEnabled)
                        startService(new Intent(getBaseContext(), DataRecorderService.class));
                    else {
                        //show GPS settings
                        showGPSSettingsAlert();
                        // set the switched to off state
                        recordSwitch.setChecked(false);
                        isRecordDataEnabled = false;
                    }
                    // after all this, we ask user for WiFi permissions
                    if( !wifiManager.isWifiEnabled() ){
                        //showWiFiSettingsAlert();
                    }
                } else
                    stopService(new Intent(getBaseContext(), DataRecorderService.class));
            }
        });


        /**
         * Display Data Switch listeners
         */
        displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sensorDataCard.setVisibility(CardView.VISIBLE);
                    gpsDataCard.setVisibility(CardView.VISIBLE);
                } else {
                    sensorDataCard.setVisibility(CardView.INVISIBLE);
                    gpsDataCard.setVisibility(CardView.INVISIBLE);
                }
                /**
                 * Update variable accordingly
                 */
                isDisplayDataEnabled = isChecked;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isDisplayDataEnabled) {
                    // get the sensor data
                    String sensorData = intent.getStringExtra("sensorData");
                    // get the GPS data in format of string
                    String locationData = intent.getStringExtra("locationData");

                    updateSensorCard(sensorData);
                    updateGPSCard(locationData);
                }
            }
        };
        this.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(this.receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Wait for Next Version.", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_about) {
            Toast.makeText(this, "Wait for Next Version.", Toast.LENGTH_SHORT).show();
            // about activity launch has been disabled at the moment to add necessary UI.
            //startActivity(new Intent(this, About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
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
     * @param serviceClass
     * @return
     */
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(runningService.service.getClassName())) return true;
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

}
