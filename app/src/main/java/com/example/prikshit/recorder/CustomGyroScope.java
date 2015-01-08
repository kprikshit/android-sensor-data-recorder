package com.example.prikshit.recorder;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Prikshit on 08-01-2015.
 */
public class CustomGyroScope implements SensorEventListener {
    private Sensor gyro;
    private SensorManager sensorManager;
    private float[] lastReading;

    public CustomGyroScope(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lastReading = new float[3];
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for(int i=0;i<3;i++){
            lastReading[i] = event.values[i];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean isGyroScopePresent(){
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!=null;
    }

    public float[] getLastReading(){
        return this.lastReading;
    }

    public String getLastReadingString(){
        return String.format("%.3f",lastReading[0]) + "," + String.format("%.3f",lastReading[1]) + "," + String.format("%.3f",lastReading[2]);
    }
}
