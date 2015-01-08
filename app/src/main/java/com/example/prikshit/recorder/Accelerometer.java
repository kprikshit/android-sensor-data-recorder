package com.example.prikshit.recorder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;

/**
 * Created by Prikshit on 08-01-2015.
 */
public class Accelerometer implements SensorEventListener{
    private Sensor sensor ;
    private SensorManager sensorManager;

    public Accelerometer(){
        //SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        //sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
