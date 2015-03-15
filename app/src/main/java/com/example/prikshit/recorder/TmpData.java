package com.example.prikshit.recorder;

import android.content.Context;
import android.location.Location;

/**
 * Created on: 13-02-2015 by
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 */
public class TmpData {
    public static boolean startAlarmRunning = false;
    public static boolean stopAlarmRunning = false;
    public static boolean recordingOn = true;

    public static boolean isStopAlarmRunning() {
        return stopAlarmRunning;
    }

    public static void setStopAlarmRunning(boolean stopAlarmRunning) {
        TmpData.stopAlarmRunning = stopAlarmRunning;
    }

    public static boolean isStartAlarmRunning() {
        return startAlarmRunning;
    }

    public static void setStartAlarmRunning(boolean startAlarmRunning) {
        TmpData.startAlarmRunning = startAlarmRunning;
    }
}
