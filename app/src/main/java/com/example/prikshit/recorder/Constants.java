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
    public final static String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static int BATTERY_SAVER_LEVEL = 30;
    public final static Class RECORDING_CLASS = DataRecorderService.class;

    // interval for checking whether phone has stopped moving (in milliseconds)
    public final static long CHECK_STOP_INTERVAL = 15*60*1000;
    // interval for checking whether phone has started moving or not
    public final static long CHECK_START_INTERVAL = 30*60*1000;

    public final static double SPEED_MARGIN = 8.00;

    public final static Class AUTO_START_RECORDING_CLASS = StartRecorderService.class;
    public final static Class AUTO_STOP_RECORDING_CLASS = StopRecorderService.class ;

}
