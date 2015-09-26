package com.prikshit.recorder.main;

import android.os.Environment;
import android.util.Log;

import com.prikshit.recorder.constants.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A custom logger created for self defined logging
 *
 * Created on: 13-03-2015 by
 * Prikshit Kumar
 * kprikshit@iitrpr.ac.in
 */
public class Logger {
    private final static String fileName = "log.txt";
    private final static String directory  = "data_recorder";
    private final static String format = "MM-dd HH:mm:ss.SSS";
    private static SimpleDateFormat timeStampFormat = new SimpleDateFormat(format);
    private static File sdDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.DIRECTORY);
    private static File logFile = new File(sdDirectory, Constants.LOGFILE_NAME);
    private static FileOutputStream fileOutputStream;

    public static void appendLog(String level, String TAG, String message){
        if(Constants.LOGGING_ENABLED) {
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                    fileOutputStream = new FileOutputStream(logFile, true);
                } catch (IOException e) {
                    Log.d("Logger", "Can't create log file. Error: " + e.getMessage());
                }
            } else {
                try {
                    fileOutputStream = new FileOutputStream(logFile, true);
                } catch (FileNotFoundException e) {
                    Log.e("Logger", "logFile Not Found: " + e.getMessage());
                }
            }
            String finalMessage = timeStampFormat.format(new Date()) + " " + level + "/" + TAG + ": " + message + "\r\n";
            try {
                fileOutputStream.write(finalMessage.getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                Log.d("Logger", "unable to write logs to file " + e.getMessage());
            }
        }
    }

    public static void d(String TAG, String message){
        appendLog("D",TAG, message);
    }

    public static void i(String TAG, String message){
        appendLog("I",TAG, message);
    }

    public static void e(String TAG, String message){
        appendLog("E",TAG, message);
    }
}
