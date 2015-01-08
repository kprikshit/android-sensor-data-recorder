package com.example.prikshit.recorder;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private Toolbar toolbar;
    private Sensor accel;
    private Sensor gyro;
    private SensorManager sensorManager;

    //Other Settings
    private long lastAccelUpdate;
    private long lastGyroUpdate;
    //frequency of data recording in milliseconds
    private long freq=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        //starting sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);

        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMinValue(2);
        np.setMaxValue(100);
        np.setWrapSelectorWheel(true);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(newVal==2){
                    freq=newVal;
                }
                else{
                    freq=newVal*10;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Setting will come in next Version",Toast.LENGTH_SHORT).show();
            return true;
        }
        if(id == R.id.action_about){
            startActivity(new Intent(this, About.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currTime= System.currentTimeMillis();
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            if(currTime-lastAccelUpdate > freq) {
                TextView accelData = (TextView) findViewById(R.id.accelData);
                String data = "X-axis : " + event.values[0] + "\nY-axis : " + event.values[1] + "\nZ-axis : " + event.values[2];
                accelData.setText(data);
                lastAccelUpdate = currTime;
            }
        }
        else if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            if(currTime-lastGyroUpdate >freq) {
                TextView gyroData = (TextView) findViewById(R.id.gyroData);
                String data = "X-axis : " + event.values[0] + "\nY-axis : " + event.values[1] + "\nZ-axis : " + event.values[2];
                gyroData.setText(data);
                lastGyroUpdate = currTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
