package com.example.prikshit.recorder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Prikshit on 08-01-2015.
 */
public class DataRecorderService extends Service implements SensorEventListener{

    /***
     * File Read Write Information
     */
    String fileName="data1.txt";
    private File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Data_Recorder");
    private File dataFile = new File(sdDirectory, fileName);
    private FileOutputStream dataOutputStream;

    /**
     * Custom Defined Primary Sensors
     * Accelerometer is not used because we will be using this sensor in this java file only
     */
    private CustomGyroScope gyroScope;
    private CustomLightSensor lightSensor;
    private CustomMagnetometer magnetometer;
    private CustomGPS gpsSensor;

    private Sensor accelSensor;
    private SensorManager sensorManager;

    private long lastReadingUpdateTime;
    private long minUpdateDelay = 0;

    /**
     * Format of TimeStamp to be used in front of each reading
     */
    SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss.SSS");

    /**
     * What to do when the service is created
     * Initialize all the sensorListeners
     */
    @Override
    public void onCreate(){
        gyroScope = new CustomGyroScope(this);
        lightSensor = new CustomLightSensor(this);
        magnetometer = new CustomMagnetometer(this);
        gpsSensor = new CustomGPS(this);

        sdDirectory.mkdirs();
        try {
            dataOutputStream = new FileOutputStream(dataFile,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * On start, initialize the current sensorEvent Listener with accelerometer as sensor
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

        return START_STICKY;
    }

    /**
     * Before destroying, de-register all the listeners
     */
    @Override
    public void onDestroy(){
        gyroScope.unregisterListener();
        magnetometer.unregisterListener();
        lightSensor.unregisterListener();
        gpsSensor.unregisterListener();
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    /**
     * SensorEvent Listener was triggered,
     * Time to write readings to file
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        /**
         * Check first if recording is enabled
         * If yes, check whether the displaying is also enabled or not
         */
        long currTime = System.currentTimeMillis();
        if (currTime - lastReadingUpdateTime > minUpdateDelay) {
            //Taking only 3 point precision for accel values
            String allSensorData = String.format("%.3f", event.values[0]) + "," + String.format("%.3f", event.values[1]) + "," + String.format("%.3f", event.values[2]);
            allSensorData = allSensorData + "," + gyroScope.getLastReadingString();
            allSensorData = allSensorData + "," + magnetometer.getLastReadingString();
            allSensorData = allSensorData + "," + lightSensor.getLastReadingString();

            Location location = gpsSensor.getLastLocation();
            /**
             * new data is now added to GPS information
             * this new data is UNIX timestamp as given by the GPS
             * New GPS Data:
             *      latitude, longitude, accuracy, altitude, speed, time
             */
            String locationData = "";
            if(location==null) locationData = "-,-,-,-,-,-";
            else {
                locationData = Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude()) + "," + Double.toString(location.getAccuracy()) + "," + Double.toString(location.getAltitude()) + "," + Double.toString(location.getSpeed()) + "," + Long.toString(location.getTime());
            }
            // write this data to file
            writeToFile(allSensorData, locationData);
            lastReadingUpdateTime = currTime;
            /**
             * Sending this information back to activity for displaying on view
             */
            Intent intent = new Intent("android.intent.action.MAIN").putExtra("locationData", locationData);
            intent.putExtra("sensorData", (java.io.Serializable) allSensorData);
            this.sendBroadcast(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /***
     * Writing data to file.
     * @param allSensorData
     * @param locationData
     */
    public void writeToFile(String allSensorData, String locationData){
        String allData = timeStampFormat.format(new Date()) +","+ allSensorData +","+ locationData +"\n";
        try {
            dataOutputStream.write(allData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
