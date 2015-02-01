package com.example.prikshit.recorder;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**Created on: 08-01-2015 by:
   *     Prikshit Kumar
   *    kprikshit22@gmail.com/kprikshit@iitrpr.ac.in
   *     CSE, IIT Ropar
   *     Modified by:
   *     Parmeet Singh
   *     sparmeet@iitrpr.ac.in
*/
/**
 *
 *
 * The java class is implemented as a service which listens to various listener and
 * appends the data obtained in a file.
 */
public class DataRecorderService extends Service implements SensorEventListener {

    /**
     * File Read Write Information
     */
    String fileName = "sensorData.csv";
    float earthAcc[];
    /**
     * file information for storing lag between files
     */
    // WRITE LAG DISABLED
    // String lagFileName = "fileWriteLag.csv";
    /**
     * Format of TimeStamp to be used in front of each reading
     */
    SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss.SSS");
    private File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Data_Recorder");
    private File dataFile = new File(sdDirectory, fileName);
    private FileOutputStream dataOutputStream;
    // WRITE LAG DISABLED the lag file Name
    // private File logFile = new File(sdDirectory, lagFileName);
    // private FileOutputStream logOutputStream;
    /**
     * Custom Defined Primary Sensors
     * Accelerometer is not used because we will be using this sensor in this java file only
     */
    private CustomGyroScope gyroScope;
    private CustomLightSensor lightSensor;
    private CustomMagnetometer magnetometer;
    private CustomGPS gpsSensor;
    private CustomGravity gravitySensor;
    private Sensor accelSensor;
    private SensorManager sensorManager;


    private CustomWifi wifiReader;
    // CELLULAR DISABLED cellular data has also been disabled for this version.
    // private CustomCellular cellularReader;

    // time when last reading was appended/written to file.
    private long lastWriteTime;
    // the minimum delay for between appending data to the file.
    private long minUpdateDelay = 0;

    // for storing all sensors and other data
    private StringBuilder allData = new StringBuilder();

    /**
     * What to do when the service is created
     * Initialize all the sensorListeners
     */
    @Override
    public void onCreate() {
        gyroScope = new CustomGyroScope(this);
        lightSensor = new CustomLightSensor(this);
        magnetometer = new CustomMagnetometer(this);
        gpsSensor = new CustomGPS(this);
        wifiReader = new CustomWifi(this);
        gravitySensor = new CustomGravity(this);
        // CELLULAR DISABLED
        //cellularReader = new CustomCellular(this);

        sdDirectory.mkdirs();
        try {
            dataOutputStream = new FileOutputStream(dataFile, true);
            // WRITE LAG DISABLED: lag information has been disabled currently.
            //logOutputStream = new FileOutputStream(logFile,true);
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
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

        return START_STICKY;
    }

    /**
     * Before destroying, de-register all the listeners
     */
    @Override
    public void onDestroy() {
        gyroScope.unregisterListener();
        magnetometer.unregisterListener();
        lightSensor.unregisterListener();
        gpsSensor.unregisterListener();
        gravitySensor.unregisterListener();
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    /**
     * SensorEvent Listener was triggered,
     * Time to write readings to file
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        long currTime = System.currentTimeMillis();
        if (currTime - lastWriteTime > minUpdateDelay) {
            allData.append(timeStampFormat.format(new Date()));
            allData.append(",");
            // now appending accelerometer data
            float[] mag = magnetometer.getLastReading();
            //float[] gravityValues = gravitySensor.getLastReading();
            String magnetoData = String.format("%.3f", mag[0]) + "," +String.format("%.3f", mag[1]) + "," + String.format("%.3f", mag[2]);
            //normalized accelerationData appended here
            appendNormalizedAcceleration(event.values, mag);

            // now appending data from other sensors
            allData.append(",");
            allData.append(gyroScope.getLastReadingString());
            allData.append(",");
            allData.append(magnetoData);
            allData.append(",");
            allData.append(lightSensor.getLastReadingString());
            // for sending to display on activity screen
            /**
             * FUTURE SCOPE:
             * Calling broadcast this many time may cause some undesirable effect (slowing down) on phone performance
             * this can be removed in future versions
             */

            String sensorData = allData.toString();
            String locationData = gpsSensor.getLastLocationInfo();
            // Sending this information back to activity for displaying on view
            Intent intent = new Intent("android.intent.action.MAIN").putExtra("locationData", locationData);
            intent.putExtra("sensorData", (java.io.Serializable) sensorData);
            this.sendBroadcast(intent);
            allData.append(",");
            allData.append(locationData);

            // appending GPS data
            //allData.append(",");
            //allData.append(gpsSensor.getLastLocationInfo());

            // appending WiFi and cellular data
            /**
             * DISABLED
             * Calling wifiManager at this rate will cause the application to crash.
             * We need to come up with some other plan to get the wifi SSIDs /their info
             */
            //allData.append(",");
            //allData.append(wifiReader.getWifiSignals());
            //System.out.println(wifiReader.getWifiSignals());

            // CELLULAR DISABLED
            // optionalData = optionalData + cellularReader.getCellularSignals();

            // write this data to file
            allData.append("\n");
            writeToFile();
            lastWriteTime = currTime;
        }
    }

    public void appendNormalizedAcceleration(float[] accelerometerValues,float[] geomagneticMatrix)
    {
        if(accelerometerValues!=null && geomagneticMatrix!=null) {
            float[] R = new float[16];
            float[] I = new float[16];
            SensorManager.getRotationMatrix(R, I, gravitySensor.getLastReading(), geomagneticMatrix);
            float[] relativacc = new float[4];
            float[] inv = new float[16];
            float[] earthAcc = new float[16];
            relativacc[0] = accelerometerValues[0];
            relativacc[1] = accelerometerValues[1];
            relativacc[2] = accelerometerValues[2];
            relativacc[3] = 0;
            Matrix.invertM(inv, 0, R, 0);
            Matrix.multiplyMV(earthAcc, 0, inv, 0, relativacc, 0);

            allData.append(String.format("%.3f",earthAcc[0]) + "," + String.format("%.3f",earthAcc[1]) + "," + String.format("%.3f",earthAcc[2]));
        }
        else
            allData.append(String.format("%.3f",accelerometerValues[0]) + "," + String.format("%.3f",accelerometerValues[1]) + "," + String.format("%.3f",accelerometerValues[2]));
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Writing data to file.
     * all the data except the timeStamp which is to be appended into file
     */
    public void writeToFile() {
        /**
         * <p>
         *     Storing sensor, location and wifi data only.
         *     Not storing the cellular data.
         * </p>
         */
        // WRITE LAG DISABLED
        //long beforeTime = System.nanoTime();
        try {
            dataOutputStream.write(allData.toString().getBytes());
            allData.setLength(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * <p>
         *     Now storing lag time in a new file.
         * </p>
         */
        // WRITE LAG DISABLED
        /**
        long currTime = System.nanoTime();
        allData= Long.toString(beforeTime) +","+ Long.toString(currTime)+","+ Long.toString(currTime-beforeTime)+"\n";
        try {
            logOutputStream.write(allData.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
    }

}
