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
 * A java class implemented as a separate listener to light sensor only.
 */
public class CustomLightSensor implements SensorEventListener {
    private final SensorManager sensorManager;
    private float lastReading;
    private final boolean lightSensorPresent;

    public CustomLightSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensorPresent = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null;
        if(lightSensorPresent) {
            Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            sensorManager.registerListener(this, light, RecordingMode.getCurrentMode());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        lastReading = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public String getLastReadingString() {
        if(isLightSensorPresent()) {
            return String.format("%.3f", lastReading);
        }
        else return "-";
    }

    boolean isLightSensorPresent(){
        return this.lightSensorPresent;
    }

    public void unregisterListener() {
        if(isLightSensorPresent())
            sensorManager.unregisterListener(this);
    }
}
