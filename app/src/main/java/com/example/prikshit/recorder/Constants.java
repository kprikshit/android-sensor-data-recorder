package com.example.prikshit.recorder;

import android.hardware.SensorManager;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-02-2015
 */
public final class Constants {
    public final static String NAME = "Data Recorder";
    public final static boolean ENABLED = true;
    public final static String DATA_FILE_NAME = "sensorData.csv";
    public final static String DIRECTORY = "/Data_Recorder";
    public final static int FAST_RECORDING_MODE = SensorManager.SENSOR_DELAY_FASTEST;
    public final static int MEDIUM_RECORDING_MODE = SensorManager.SENSOR_DELAY_GAME;
    public final static int NORMAL_RECORDING_MODE = SensorManager.SENSOR_DELAY_NORMAL;
    public final static int BATTERY_SAVER_RECORDING_MODE = SensorManager.SENSOR_DELAY_NORMAL;
    public final static String TIMESTAMP_FORMAT = "yyyy:MM:dd:hh:mm:ss.SSS";
    public final static int BATTERY_SAVER_LEVEL = 15;

}
