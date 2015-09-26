package  com.prikshit.recorder.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.prikshit.recorder.main.RecordingMode;

/**
 * Author: Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 *
 * A java class implemented as a separate listener to accelerometer only.
 * Right now, this is not used in the project but it can be used in future.
 */
 public class CustomAccelerometer implements SensorEventListener {
    private Sensor accel;
    private SensorManager sensorManager;
    private float[] lastReading;
    private boolean accelPresent;

    public CustomAccelerometer(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelPresent = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null;
        if(accelPresent) {
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accel, RecordingMode.getCurrentMode());
        }
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

    public String getLastReadingString() {
        if(isAccelPresent()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("%.3f", lastReading[0]));
            stringBuilder.append(",");
            stringBuilder.append(String.format("%.3f", lastReading[1]));
            stringBuilder.append(",");
            stringBuilder.append(String.format("%.3f", lastReading[2]));
            return stringBuilder.toString();
        }
        else return "-,-,-";
    }

    public boolean isAccelPresent(){
        return this.accelPresent;
    }

    public void unregisterListener() {
        if(isAccelPresent())
            sensorManager.unregisterListener(this);
    }

}
