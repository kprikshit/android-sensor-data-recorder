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
 * A java class implemented as a separate listener to gyroscope only.
 */
 public class CustomGyroScope implements SensorEventListener {
    private final SensorManager sensorManager;
    private float[] lastReading;
    private final boolean gyroPresent;
    private StringBuilder gyroData;

    public CustomGyroScope(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroPresent = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!= null;
        if(gyroPresent) {
            lastReading = new float[3];
            Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, gyro, RecordingMode.getCurrentMode() );
            gyroData = new StringBuilder();
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
        if(isGyroPresent()) {
            // resetting the builder
            gyroData.setLength(0);
            gyroData.append(String.format("%.3f", lastReading[0]));
            gyroData.append(",");
            gyroData.append(String.format("%.3f", lastReading[1]));
            gyroData.append(",");
            gyroData.append(String.format("%.3f", lastReading[2]));
            return gyroData.toString();
        }
        else return "-,-,-";
    }

    boolean isGyroPresent(){
        return this.gyroPresent;
    }

    public void unregisterListener() {
        if(isGyroPresent())
            sensorManager.unregisterListener(this);
    }
}
