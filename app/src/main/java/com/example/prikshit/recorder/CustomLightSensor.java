package com.example.prikshit.recorder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 *
 * A java class implemented as a separate listener to light sensor only.
 */
public class CustomLightSensor implements SensorEventListener {
    private Sensor light;
    private SensorManager sensorManager;
    private float lastReading;

    public CustomLightSensor(Context context) {
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

    public boolean isLightSensorPresent() {
        return sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null;
    }

    public float getLastReading() {
        return this.lastReading;
    }

    public String getLastReadingString() {
        return String.format("%.3f", lastReading);
    }

    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }
}
