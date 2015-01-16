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
 * A java class implemented as a separate listener to magnetometer only.
 */
public class CustomMagnetometer implements SensorEventListener {
    private Sensor magneto;
    private SensorManager sensorManager;
    private float[] lastReading;

    public CustomMagnetometer(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lastReading = new float[3];
        magneto = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magneto, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for (int i = 0; i < 3; i++) {
            lastReading[i] = event.values[i];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean isMagnetoPresent() {
        return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
    }

    public float[] getLastReading() {
        return this.lastReading;
    }

    public String getLastReadingString() {
        return String.format("%.3f", lastReading[0]) + "," + String.format("%.3f", lastReading[1]) + "," + String.format("%.3f", lastReading[2]);
    }

    /**
     * Unregistering the sensor listener
     */
    public void unregisterListener() {
        sensorManager.unregisterListener(this);
    }

}
