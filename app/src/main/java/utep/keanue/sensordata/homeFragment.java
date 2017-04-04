package utep.keanue.sensordata;

import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by xerg on 4/1/2017.
 */

public class homeFragment extends android.support.v4.app.Fragment implements SensorEventListener{

    private TextView xText, yText, zText, acText;
    public String current_ac_data;
    SensorManager mSensorManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) this.getActivity().getSystemService(MainActivity.SENSOR_SERVICE);
        Sensor mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(homeFragment.this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.home_content, container, false);
        //TextView
        xText = (TextView) v.findViewById(R.id.textFragX);
        yText = (TextView) v.findViewById(R.id.textFragY);
        zText = (TextView) v.findViewById(R.id.textFragZ);
        acText= (TextView) v.findViewById(R.id.textCurrAC);

        acText.setText(current_ac_data);
        return v;
    }


    //*** WORKING ON ///
    public interface OnSensorDataPass {
        public void onSensorDataPass(String data);
    }

    OnSensorDataPass dataPasser;

    //WORKING ON//

    @Override
    public void onStart() {
        super.onStart();

        if(this.getUserVisibleHint()) {
            this.registerSensorListener();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterSensorListener();
    }

    private void registerSensorListener() {
        mSensorManager.registerListener(this, mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterSensorListener() {
        mSensorManager.unregisterListener(this);
    }


    /* Accelerometer Methods */

    /** Sensor Changed
     * This will change the TextView to the values of the sensor
     */
    public void onSensorChanged(SensorEvent event) {

        //Global Display Variables
        xText.setText("X Axis: \n" + event.values[0]);
        yText.setText("Y Axis: \n" + event.values[1]);
        zText.setText("Z Axis: \n" + event.values[2]);

        //Local Variables
        String x = extractData(xText.getText().toString());
        String y = extractData(yText.getText().toString());
        String z = extractData(zText.getText().toString());

        //Save full accelerometer data
        current_ac_data = (x+","+y+","+z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /** Extract data from a String */
    private String extractData(String string){
        String[] parts = string.split(":");
        return parts[1];
    }


}
