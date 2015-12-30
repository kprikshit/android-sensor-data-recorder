package  com.prikshit.recorder.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.prikshit.recorder.main.RecordingMode;


/**
 * Created on: 01-02-2015 by
 * Parmeet Singh
 * sparmeet@iitrpr.ac.in
 * CSE, IIT Ropar
 *
 * A java class implemented as a separate listener to gravity only.
 */
public class CustomGravity implements SensorEventListener {
    private final SensorManager sensorManager;
    private float[] lastReading;
    private final boolean gravityPresent;
    private StringBuilder gravityData;

    public CustomGravity(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gravityPresent = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)!= null;
        if(gravityPresent) {
            lastReading = new float[3];
            Sensor gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            sensorManager.registerListener(this, gravity, RecordingMode.getCurrentMode());
            gravityData = new StringBuilder();
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
        if(isGravityPresent()) {
            // resetting the builder
            gravityData.setLength(0);
            gravityData.append(String.format("%.3f", lastReading[0]));
            gravityData.append(",");
            gravityData.append(String.format("%.3f", lastReading[1]));
            gravityData.append(",");
            gravityData.append(String.format("%.3f", lastReading[2]));
            return gravityData.toString();
        }
        else return "-,-,-";
    }
    public float[] getLastReading(){
        return this.lastReading;
    }

    public boolean isGravityPresent(){
        return this.gravityPresent;
    }

    public void unregisterListener() {
        if(isGravityPresent())
            sensorManager.unregisterListener(this);
    }
}
