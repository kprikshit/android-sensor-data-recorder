package com.example.prikshit.recorder;

import android.hardware.SensorManager;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-02-2015
 */
public final class Constants {
    //App related information
    public final static String NAME = "Data Recorder";
    public final static String DIRECTORY = "/Data_Recorder";
    public final static String DATA_FILE_NAME = "sensorData.csv";

    // Recording related info
    public final static String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static int FAST_RECORDING_MODE = SensorManager.SENSOR_DELAY_FASTEST;
    public final static int MEDIUM_RECORDING_MODE = SensorManager.SENSOR_DELAY_GAME;
    public final static int NORMAL_RECORDING_MODE = SensorManager.SENSOR_DELAY_NORMAL;
    public final static int BATTERY_SAVER_RECORDING_MODE = SensorManager.SENSOR_DELAY_NORMAL;

    // Various class information
    public final static Class RECORDING_CLASS = DataRecorderService.class;
    public final static Class AUTO_START_RECORDING_CLASS = StartRecorderService.class;
    public final static Class AUTO_STOP_RECORDING_CLASS = StopRecorderService.class ;

    // Misc Info
    public static int BATTERY_SAVER_LEVEL = 30;
    public static boolean LOGGING_ENABLED = true;

    // Auto Start and Stop Information
    public static long CHECK_STOP_INTERVAL = 1*30*1000;
    public static long CHECK_START_INTERVAL = 30*60*1000;
    public static float SPEED_THRESHOLD = 8.0f;

    // Server related information
    public static String SERVER_ADDRESS = "10.1.201.41";


    // Notification related info
    public static boolean NOTIFICATION_ENABLED = true;

    // Log File related infor
    public static String LOGFILE_NAME  = "log.txt";
}
