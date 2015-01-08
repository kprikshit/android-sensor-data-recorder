package com.example.prikshit.recorder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Prikshit on 08-01-2015.
 */
public class CustomLightSensor implements SensorEventListener {
    private Sensor light;
    private SensorManager sensorManager;
    private float lastReading;

    public CustomLightSensor(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        lastReading = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean isLightSensorPresent(){
        return sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!=null;
    }

    public float getLastReading(){
        return this.lastReading;
    }

    public String getLastReadingString(){
        return String.format("%.3f",lastReading);
    }
}
