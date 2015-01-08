package com.example.prikshit.recorder;

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
public class MainActivity extends ActionBarActivity implements SensorEventListener {

    /***
     * File Read Write Information
     */
    String fileName="data1.txt";
    private File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Data_Recorder");
    private File dataFile = new File(directory, fileName);
    private FileOutputStream dataOutput;

    /**
     * Custom Defined Primary Sensors
     * Accelerometer is not used because we will be using this sensor in this java file only
     */
    private CustomGyroScope gyroScope;
    private CustomLightSensor lightSensor;
    private CustomMagnetometer magnetometer;
    private CustomGPS gpsSensor;

    private Toolbar toolbar;
    //Secondary Sensors: NOT YET USED
    private Sensor gravity, linearAccel, rotationVector;

    // For accelerometer sensor in this file
    private Sensor accelSensor;
    private SensorManager sensorManager;

    // the time when lastSensorData was updated
    private long lastSensorUpdate;
    /**
     * Minimum Time to add new data in milliseconds
     */
    private long minUpdateDelay = 2;

    // Sensor Update Settings
    private int sensorReadingDelay =  SensorManager.SENSOR_DELAY_FASTEST;

    // Storing root for future purposes
    Context rootContext;

    /**
     * For various switched
     */
    private boolean isDisplayDataEnabled = false;
    private boolean isRecordDataEnabled =  false;

    /**
     * Format of TimeStamp to be used in data appending
     */
    SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss.SSS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //saving this context for future purposes
        rootContext = this;

        /**
         * Initializing Sensors and Listener
         */
        gyroScope = new CustomGyroScope(this);
        lightSensor = new CustomLightSensor(this);
        magnetometer = new CustomMagnetometer(this);
        gpsSensor = new CustomGPS(this);

        /**
         * Custom Action Bar / App Bar / Tool Bar
         */
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelSensor,SensorManager.SENSOR_DELAY_FASTEST);


        /**
         * Initially, the sensorData Card is not displayed.
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
         * Display Data Switch listeners
         */
        displaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /**
                 * Show/Hide Accordingly
                 */
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
        /**
         * Record Data Switch Listener
         */
        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /**
                 * update variable accordingly
                 */
                isRecordDataEnabled = isChecked;
                //Start the background Service
                if(isChecked){
                    startService(new Intent(getBaseContext(),DataRecorderService.class));
                }
                else{
                    stopService(new Intent(getBaseContext(),DataRecorderService.class));
                }
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        /**
         * Create Directory if not present
         */
        directory.mkdirs();
        try {
            dataOutput = new FileOutputStream(dataFile,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        /**
         * About Section
         */
        if(id == R.id.action_about){
            startActivity(new Intent(this, About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * What happens when sensorEvent triggered
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        /**
         * Check first if recording is enabled
         * If yes, check whether the displaying is also enabled or not
         */
        if(isRecordDataEnabled){
            long currTime = System.currentTimeMillis();
            if (currTime - lastSensorUpdate > minUpdateDelay) {
                List<String> sensorData = new ArrayList<>();

                String accelData = String.format("%.3f", event.values[0]) + "," + String.format("%.3f", event.values[1]) + "," + String.format("%.3f", event.values[2]);
                sensorData.add(accelData);
                sensorData.add(gyroScope.getLastReadingString());
                sensorData.add(magnetometer.getLastReadingString());
                sensorData.add(lightSensor.getLastReadingString());

                // write this data to file
                writeToFile(sensorData, gpsSensor.getLastLocation());
                if(isDisplayDataEnabled){
                    updateUI(sensorData);
                    updateGPSCard(gpsSensor.getLastLocation());
                }
                lastSensorUpdate = currTime;
            }
        }
        /**
         * If recording is not enabled but displaying is enabled
         */
        else if(isDisplayDataEnabled) {
            long currTime = System.currentTimeMillis();
            if (currTime - lastSensorUpdate > minUpdateDelay) {
                List<String> sensorData = new ArrayList<>();

                String accelData = String.format("%.3f", event.values[0]) + "," + String.format("%.3f", event.values[1]) + "," + String.format("%.3f", event.values[2]);
                sensorData.add(accelData);
                sensorData.add(gyroScope.getLastReadingString());
                sensorData.add(magnetometer.getLastReadingString());
                sensorData.add(lightSensor.getLastReadingString());
                updateUI(sensorData);
                updateGPSCard(gpsSensor.getLastLocation());
                lastSensorUpdate = currTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //TODO
    }


    /**
     * update the sensor data onto the sensorDataCard
     * List Format:
     *           Accel, Gyro, Magneto, Light
     * All data should be in String format
     * @param data
     */
    public void updateUI(List<String> data) {
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

    public void writeToFile(List<String> sensorData, Location location){
        String allData = "";
        for(int i=0;i<sensorData.size();i++){
            allData = allData + sensorData.get(i)+";";
        }
        String locationData="";
        if(location!=null) {
            locationData = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()) + "," + Double.toString(location.getAccuracy()) + "," + Double.toString(location.getAltitude()) + "," + Double.toString(location.getSpeed());
        }
        else{
            locationData="-,-,-,-,-";
        }
        allData = timeStampFormat.format(new Date()) + ";" + allData + locationData + "\n";
        try {
            dataOutput.write(allData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
