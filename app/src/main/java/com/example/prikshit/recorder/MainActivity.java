package com.example.prikshit.recorder;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/***
 * The Main Activity of App
 * Uses Sensor Information
 */
public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private boolean isDisplayDataEnabled = false;
    private boolean isRecordDataEnabled =  false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        /**
         * Initially, the CardView gpsDataCard and sensorData are not displayed
         */
        final CardView sensorDataCard = (CardView) findViewById(R.id.sensorCard);
        final CardView gpsDataCard = (CardView) findViewById(R.id.gpsDataCard);
        if(!isDisplayDataEnabled){
            sensorDataCard.setVisibility(CardView.INVISIBLE);
            gpsDataCard.setVisibility(CardView.INVISIBLE);
        }

        /**
         * Switches and their Listeners
         */
        Switch recordSwitch = (Switch) findViewById(R.id.recordingSwitch);
        Switch displaySwitch = (Switch) findViewById(R.id.displaySwitch);

        /**
         * Before doing anything check whether the service is running or not
         */
        if(isServiceRunning(DataRecorderService.class)){
            isRecordDataEnabled = true;
            recordSwitch.setChecked(true);
        }

        /**
         * Record Data Switch Listener
         */
        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRecordDataEnabled = isChecked;
                //Start the background Service
                if(isChecked)
                    startService(new Intent(getBaseContext(),DataRecorderService.class));
                else
                    stopService(new Intent(getBaseContext(),DataRecorderService.class));
            }
        });


        /**
         * Display Data Switch listeners
         */
        displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sensorDataCard.setVisibility(CardView.VISIBLE);
                    gpsDataCard.setVisibility(CardView.VISIBLE);
                }
                else{
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
    public void onStart(){
        super.onStart();
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
            Toast.makeText(this, "Setting will come in next Version",Toast.LENGTH_SHORT).show();
            return true;
        }
        if(id == R.id.action_about){
            startActivity(new Intent(this, About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * update the sensor data onto the sensorDataCard
     * List Format:
     *           Accel, Gyro, Magneto, Light
     * All data should be in String format
     * @param data
     */
    public void updateSensorCard(List<String> data) {
        TextView accelData = (TextView) findViewById(R.id.accelData);
        TextView gyroData = (TextView) findViewById(R.id.gyroData);
        TextView magnetoData = (TextView) findViewById(R.id.magnetoData);
        TextView lightData = (TextView) findViewById(R.id.lightData);


        if (!data.get(0).isEmpty()) accelData.setText(data.get(0)+" m/s2");
        else accelData.setText("-,-,-");

        if (!data.get(1).isEmpty()) gyroData.setText(data.get(1)+" rad/s");
        else gyroData.setText("-,-,-");

        if (!data.get(2).isEmpty()) magnetoData.setText(data.get(2)+" μT");
        else magnetoData.setText("-,-,-");

        if (!data.get(3).isEmpty()) lightData.setText(data.get(3)+" lux");
        else lightData.setText("-,-,-");
    }

    /**
     *  Updates the GPS information on GPS CardView
     * @param location
     */
    public void updateGPSCard(Location location){
        if(location!=null) {
            TextView latitude = (TextView) findViewById(R.id.latitudeData);
            TextView longitude = (TextView) findViewById(R.id.longitudeData);
            TextView accuracy = (TextView) findViewById(R.id.accuracyData);
            TextView altitude = (TextView) findViewById(R.id.altitudeData);
            TextView speed = (TextView) findViewById(R.id.speedData);
            TextView time = (TextView) findViewById(R.id.timeData);

            latitude.setText(Double.toString(location.getLatitude()));
            longitude.setText(Double.toString(location.getLongitude()));
            accuracy.setText(Double.toString(location.getAccuracy()) + " m");
            altitude.setText(Double.toString(location.getAltitude()) + " m");
            speed.setText(Float.toString(location.getSpeed()) + " m/s");
            time.setText(Long.toString(location.getTime()));
        }
    }

    public boolean isServiceRunning(Class<?> serviceClass){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceClass.getName().equals(runningService.service.getClassName()))return true;
        }
        return false;
    }
}
