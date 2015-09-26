package  com.prikshit.recorder.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.prikshit.recorder.main.RecordingMode;

/**
 * Prikshit Kumar
 * <kprikshit22@gmail.com/kprikshit@iitrpr.ac.in>
 * CSE, IIT Ropar
 * Created on: 08-01-2015
 *
 * A java class implemented as a separate listener to magnetometer only.
 */
public class CustomMagnetometer implements SensorEventListener {
    private final SensorManager sensorManager;
    private float[] lastReading;
    private final boolean magnetoPresent;
    private StringBuilder magneticData;

    public CustomMagnetometer(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        magnetoPresent = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
        if(magnetoPresent) {
            lastReading = new float[3];
            Sensor magneto = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, magneto, RecordingMode.getCurrentMode());
            magneticData = new StringBuilder();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        System.arraycopy(event.values, 0, lastReading, 0, 3);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getLastReadingString() {
        if(isMagnetoPresent()) {
            magneticData.setLength(0);
            magneticData.append(String.format("%.3f", lastReading[0]));
            magneticData.append(",");
            magneticData.append(String.format("%.3f", lastReading[1]));
            magneticData.append(",");
            magneticData.append(String.format("%.3f", lastReading[2]));
            return magneticData.toString();
        }
        else return "-,-,-";
    }

    public float[] getLastReading(){
        return this.lastReading;
    }

    public boolean isMagnetoPresent(){
        return this.magnetoPresent;
    }

    public void unregisterListener() {
        if(isMagnetoPresent()) sensorManager.unregisterListener(this);
    }

}
