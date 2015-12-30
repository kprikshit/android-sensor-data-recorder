package com.prikshit.recorder.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.prikshit.recorder.constants.Constants;
import com.prikshit.recorder.main.Logger;
import com.prikshit.recorder.main.RecordingMode;
import com.prikshit.recorder.sensors.CustomGPS;
import com.prikshit.recorder.sensors.CustomGravity;
import com.prikshit.recorder.sensors.CustomGyroScope;
import com.prikshit.recorder.sensors.CustomLightSensor;
import com.prikshit.recorder.sensors.CustomMagnetometer;
import com.prikshit.recorder.sensors.CustomWifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.prikshit.recorder.constants.Constants.*;

/**
 * The java class is implemented as a service which listens to various listener and
 * appends the data obtained in a file.
 *
 * Created by:
 * Prikshit Kumar
 * kprikshit@iitrpr.ac.in
 */
public class DataRecorderService extends Service {
    // Intent related to display switch in Mainactivity
    private final String activityIntentId = "display-switch-state-change";
    private final String displayDataSwitchIntentName = "displaySwitchChecked";

    // intent related to sending sensor data to Main Activity
    private final String serviceIntentId = "android.intent.action.MAIN";
    private final String sensorDataIntentName = "sensorData";
    private final String gpsDataIntentName = "locationData";

    // Format of TimeStamp to be used in front of each reading
    SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
    // info regarding to file used for storing the data
    private File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DIRECTORY);
    private File dataFile = new File(sdDirectory, DATA_FILE_NAME );
    private FileOutputStream dataOutputStream;
    // WRITE LAG DISABLED the lag file Name
    // private File logFile = new File(sdDirectory, lagFileName);
    // private FileOutputStream logOutputStream;

    // Custom Defined Primary Sensors
    // Accelerometer is not used because we will be using this sensor in this java file only
    private CustomGyroScope gyroScope;
    private CustomLightSensor lightSensor;
    private CustomMagnetometer magnetometer;
    private CustomGPS gpsSensor;
    private Sensor accelSensor;
    private SensorManager sensorManager;
    private CustomGravity gravitySensor;
    private CustomWifi wifiReader;
    // CELLULAR DISABLED cellular data has also been disabled for this version.
    // private CustomCellular cellularReader;

    // for various sensor data and gps data
    // using a global variable to reduce unnecessary allocation
    private StringBuilder allData = new StringBuilder();
    // display data Switch enabled or not in activity
    private boolean displayDataSwitchEnabled = false;
    private SensorEventListener mSensorListener;
    // broadcast receiver corresponding to display switch in the activity
    private BroadcastReceiver displaySwitchReceiver;
    // filter for intents sent by the display data switch in the activity
    IntentFilter intentFilter = new IntentFilter(activityIntentId);
    // tag for debug messages
    private final String TAG = "DataRecorderService";
    // String isBump = "false";

    @Override
    public void onCreate() {
        gyroScope = new CustomGyroScope(this);
        //lightSensor = new CustomLightSensor(this);
        magnetometer = new CustomMagnetometer(this);
        gpsSensor = new CustomGPS(this);
        gravitySensor = new CustomGravity(this);
        // wifiReader = new CustomWifi(this);
        // CELLULAR DISABLED
        // cellularReader = new CustomCellular(this);'
        // directory check will be handled in Activity file.
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(TAG, "recorder service started");
        final Context context = getBaseContext();

        // a new runnable task which will be run on a separate thread
        Runnable r  = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Handler handler = new Handler();
                // initializing accelerometer sensor and its event listener
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                mSensorListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        getData(context, event);
                    }
                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };
                // registering sensorEvent Listener for accelerometer
                sensorManager.registerListener(mSensorListener, accelSensor, RecordingMode.getCurrentMode() , handler);
                // registering broadcast Listener for display Data Switch
                registerDisplayDataSwitchStateListener();
                Looper.loop();
            }
        };
        // starting a new thread
        Thread workerThread = new Thread(r);
        workerThread.start();
        //      if(intent==null);else isBump = intent.getStringExtra("bump");
        //    if(isBump==null){
        //      isBump = "false";
        //}

        return START_STICKY;
    }

    /**
     * this will register a broadcast listener for receiving the stat of displaySwitch in Home
     * and accordingly we will send intents from here to Home containing sensorInformation
     */
    public void registerDisplayDataSwitchStateListener(){
    // broadcast receiver for displayData Switch Information
        displaySwitchReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                displayDataSwitchEnabled = intent.getBooleanExtra(displayDataSwitchIntentName,false);
            }
        };
        getBaseContext().registerReceiver(displaySwitchReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "recorder service destroy called");
        //unregister sensorListeners
        gyroScope.unregisterListener();
        magnetometer.unregisterListener();
        //lightSensor.unregisterListener();
        gpsSensor.unregisterListener();
        sensorManager.unregisterListener(mSensorListener);
        //unregister broadcast receiver listener
        getBaseContext().unregisterReceiver(displaySwitchReceiver);
        super.onDestroy();
    }

    /**
     * this will store sensor data into a file whenever there is a sensorEvent related to Accelerometer
     * @param context
     * @param event
     */
    public void getData(Context context, SensorEvent event){
        allData.append(timeStampFormat.format(new Date()));
        allData.append(",");
        // now appending accelerometer data
        //normalized accelerationData appended here
        float[] mag = magnetometer.getLastReading();
        appendNormalizedAcceleration(event.values, mag);

        // now appending data from other sensors
        allData.append(",");
        allData.append(gyroScope.getLastReadingString());
        allData.append(",");
        //magnetometer data
        allData.append(String.format("%.3f", mag[0]));
        allData.append(",");
        allData.append(String.format("%.3f", mag[1]));
        allData.append(",");
        allData.append(String.format("%.3f", mag[2]));
        //allData.append(",");
        // light sensor data
        //allData.append(lightSensor.getLastReadingString());

        // if display switch is enabled in the activity, only then send the intent which is to be sent to activity
        if (displayDataSwitchEnabled) {
            String locationData = gpsSensor.getLastLocationInfo();
            // Sending this information back to activity for displaying on view
            Intent intent = new Intent(serviceIntentId);
            intent.putExtra(gpsDataIntentName, locationData);
            intent.putExtra(sensorDataIntentName, allData.toString());
            context.sendBroadcast(intent);
            allData.append(",");
            allData.append(locationData);
        } else {
            allData.append(",");
            allData.append(gpsSensor.getLastLocationInfo());
        }

        // appending WiFi and cellular data
        // Calling wifiManager at this rate will cause the application to crash.
        // We need to come up with some other plan to get the wifi SSIDs /their info

        //allData.append(",");
        //allData.append(wifiReader.getWifiSignals());
        //System.out.println(wifiReader.getWifiSignals());

        // CELLULAR DISABLED
        // optionalData = optionalData + cellularReader.getCellularSignals();

        // write this data to file
        //Log.i(TAG, "listener on Thread: " + String.valueOf(android.os.Process.myTid()));
        //if(isBump=="true")System.out.println("yobump "+isBump);
        //allData.append(","+isBump);
        allData.append("\n");
        try {
            dataOutputStream.write(allData.toString().getBytes());
            allData.setLength(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this will normalize acceleration values based on orientation of phone given by GyroScope
     * @param accelerometerValues
     * @param geomagneticValues
     */
    public void appendNormalizedAcceleration(float[] accelerometerValues, float[] geomagneticValues){
        if(accelerometerValues != null && magnetometer.isMagnetoPresent() && gravitySensor.isGravityPresent() ) {
            float[] R = new float[16];
            SensorManager.getRotationMatrix(R, new float[16], gravitySensor.getLastReading(), geomagneticValues);
            float[] relativeAcc = new float[4];
            float[] inv = new float[16];
            float[] earthAcc = new float[16];
            relativeAcc[0] = accelerometerValues[0];
            relativeAcc[1] = accelerometerValues[1];
            relativeAcc[2] = accelerometerValues[2];
            relativeAcc[3] = 0;
            Matrix.invertM(inv, 0, R, 0);
            Matrix.multiplyMV(earthAcc, 0, inv, 0, relativeAcc, 0);

            allData.append(String.format("%.3f", earthAcc[0]));
            allData.append(",");
            allData.append(String.format("%.3f", earthAcc[1]));
            allData.append(",");
            allData.append(String.format("%.3f", earthAcc[2]));
        }
        else {
            allData.append(String.format("%.3f", accelerometerValues[0]));
            allData.append(",");
            allData.append(String.format("%.3f", accelerometerValues[1]));
            allData.append(",");
            allData.append(String.format("%.3f", accelerometerValues[2]));
        }
    }

}
