package com.example.prikshit.recorder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.opengl.Matrix;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 *
 * The java class is implemented as a service which listens to various listener and
 * appends the data obtained in a file.
 */
public class DataRecorderService extends Service {
    // various intent information
    private final String displayDataSwitchIntentName = "displaySwitchChecked";
    private final String sensorDataIntentName = "sensorData";
    private final String gpsDataIntentName = "locationData";
    private final String activityIntentId = "display-switch-state-change";
    private final String serviceIntentId = "android.intent.action.MAIN";

    // information about file for storing lag between consecutive writing to file
    // WRITE LAG DISABLED
    // String lagFileName = "fileWriteLag.csv";
    // Format of TimeStamp to be used in front of each reading
    SimpleDateFormat timeStampFormat = new SimpleDateFormat(Constants.TIMESTAMP_FORMAT);
    // info regarding to file used for storing the data
    private File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DIRECTORY);
    private File dataFile = new File(sdDirectory, Constants.DATA_FILE_NAME );
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
    private Sensor accelSensor;
    private SensorManager sensorManager;
    private CustomGravity gravitySensor;
    private CustomWifi wifiReader;
    // CELLULAR DISABLED cellular data has also been disabled for this version.
    // private CustomCellular cellularReader;

    // time when last reading was appended/written to file.
    private long lastWriteTime;
    // the minimum delay for between appending data to the file.
    private long minUpdateDelay = 0;

    // for various sensor data and gps data
    // using a global variable to reduce unnecessary allocation
    private StringBuilder allData = new StringBuilder();
    // display data enabled or not in activity
    private boolean displayDataEnabled = false;
    private SensorEventListener mSensorListener;
    // broadcast receiver corresponding to display switch in the activity
    private BroadcastReceiver displaySwitchReceiver;
    // filter for intents sent by the display data switch in the activity
    IntentFilter intentFilter = new IntentFilter(activityIntentId);
    // tag for debug messages
    private final String TAG = "DataRecorderService2";
//    String isBump = "false";

    @Override
    public void onCreate() {
        gyroScope = new CustomGyroScope(this);
        lightSensor = new CustomLightSensor(this);
        magnetometer = new CustomMagnetometer(this);
        gpsSensor = new CustomGPS(this);
        gravitySensor = new CustomGravity(this);
        // wifiReader = new CustomWifi(this);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Context context = getBaseContext();

        // a new runnable task which will be run on a separate thread
        Runnable r  = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Handler handler = new Handler();

                Log.i(TAG,"Service Thread ID(Start): " + android.os.Process.myTid());
                // initializing accelerometer sensor and its event listener
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

                // sensor listener
                mSensorListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        getData(context, event);
                    }
                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };
                // registering event listener
                sensorManager.registerListener(mSensorListener, accelSensor, RecordingMode.getCurrentMode() , handler);

                // broadcast receiver for displayData Switch Information
                displaySwitchReceiver= new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        displayDataEnabled = intent.getBooleanExtra(displayDataSwitchIntentName,false);
                    }
                };
                getBaseContext().registerReceiver(displaySwitchReceiver, intentFilter);
                Log.i(TAG,"service Thread ID: "+String.valueOf(android.os.Process.myTid()));
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

    @Override
    public void onDestroy() {
        System.out.println("on destroy service called");
        gyroScope.unregisterListener();
        magnetometer.unregisterListener();
        lightSensor.unregisterListener();
        gpsSensor.unregisterListener();
        sensorManager.unregisterListener(mSensorListener);
        getBaseContext().unregisterReceiver(displaySwitchReceiver);
        super.onDestroy();
    }

    public void getData(Context context, SensorEvent event){
        long currTime = System.currentTimeMillis();
        if (currTime - lastWriteTime > minUpdateDelay) {
            allData.append("\"");
            allData.append(timeStampFormat.format(new Date()));
            allData.append("\"");
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
            allData.append(",");
            // light sensor data
            allData.append(lightSensor.getLastReadingString());

            // if display switch is enabled in the activity, only then send the intent which is to be sent to activity
            if (displayDataEnabled) {
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
            //Log.i(TAG, "listener on Thread: " + String.valueOf(android.os.Process.myTid()));
          //  if(isBump=="true")System.out.println("yobump "+isBump);
            //allData.append(","+isBump);
            //allData.append("\n");
            try {
                dataOutputStream.write(allData.toString().getBytes());
                allData.setLength(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastWriteTime = currTime;
        }
    }

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

    public static void stopActivity(){

    }
}
