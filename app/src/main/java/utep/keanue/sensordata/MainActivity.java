package utep.keanue.sensordata;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//My Imports
//Sensor Imports
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
//Text, buttons and toasts
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//Google Play Services
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
//File and Date
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class that will demonstrate the use of Accelometer (Sensor).
 * implement: SensorEventListener
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.
        ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static int locationInterval = 10000;
    private static int locationFastestInterval = 5000;
    private static int locationDisplacement = 10;
    /**
     * Create private objects to use in application
     */
    private TextView xText, yText, zText, longText, latText;
    private Button btn_save, btn_read, btn_delete;
    private Sensor mySensor;
    private SensorManager SM;
    private TextView txtContent;
    //The following can be removed and set as return elements
    //instead of having them as global variables
    // ** Security and Code Design **//
    private String current_ac_data;     //Accelerometer data
    private String current_time;        //Time stamp

    //Google API and Location
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mRequestLocationUpdates = false;
    private LocationRequest mLocationRequest;

    /**
     * onCreate Method.
     * First thing the application will do once started
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Accelerometer Variables */
        // Create Sensor Manager
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Create Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register Sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        /* Text Views */
        //Assign Accelerometer
        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);

        //Assign GPS Location
        longText = (TextView) findViewById(R.id.long_text);
        latText = (TextView) findViewById(R.id.lat_text);

        //Display Saved Data
        txtContent = (TextView) findViewById(R.id.txtContent);

        /* Buttons */
        //Assign Buttons
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_read = (Button) findViewById(R.id.btn_read);
        btn_delete = (Button) findViewById(R.id.btn_delete);

        //Google Play Services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        //TODO Create a button for start/stop location services

        // Save on File
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            /** Save current data along with timestamp to file */
            public void onClick(View v) {

                //Run updateInterval
                updateInterval(1, 5);
            }
        });//end onClick SaveFile

        // Read File
        btn_read = (Button) findViewById(R.id.btn_read);
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtContent.setText(FileHelper.ReadFile(MainActivity.this));
            }
        });


        // Delete File
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileName = new File(FileHelper.path + FileHelper.fileName);
                if (FileHelper.deleteFile(fileName)) {
                    Toast.makeText(MainActivity.this, "Deleted file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error delete file!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }//end onCreate


    /**
     * Update Interval
     */
    public void updateInterval(int seconds, int instances) {
        final int millis = seconds * 1000;
        final int ins = instances;

        //Create a new thread
        Thread t = new Thread() {

            @Override
            public void run() {

                try {
                    int i = 0;
                    while (i < ins) {
                        Thread.sleep(millis);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Extract Global Variables
                                //Time Stamp
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd -> hh:mm:ss.SSS");
                                String timeStamp = simpleDateFormat.format(new Date());
                                //Concatenated Data
                                String full_data = (timeStamp + "[" + current_ac_data + "]");

                                if (FileHelper.saveToFile(full_data)) {
                                    Toast.makeText(MainActivity.this, "Saved to file", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Error save file!!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        //Update i
                        i++;
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }//end updateInterval

    /**
     * Sensor Changed
     * This will change the TextView to the values of the sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        xText.setText("X: " + event.values[0]);
        yText.setText("Y: " + event.values[1]);
        zText.setText("Z: " + event.values[2]);

        //Local Variables
        String x = xText.getText().toString();
        String y = yText.getText().toString();
        String z = zText.getText().toString();

        //Log (Testing Purposes)d
        //Log.d("xString: ",x);
        //Log.d("yString: ",y);
        //Log.d("zString: ",z);

        //Save full accelerometer data
        current_ac_data = (x + " " + y + " " + z);
    }

    /**
     * Method from interface (NOT USED)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Not Used
    }


    /** Google API Client onStart */
    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }//end OnStart GoogleAPI

    /** Google API Client onResume */
    @Override
    public void onResume() {
        super.onResume();

        checkPlayServices();
        if (mGoogleApiClient.isConnected() && mRequestLocationUpdates) {

            startLocationUpdates();
        }
    }//end OnResume GoogleAPI

    /** Google API Client onStop */
    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }//end OnStop GoogleAPI

    /** Google API Client onPause */
    @Override
    public void onPause() {
        super.onStop();

        stopLocationUpdates();
    }//end OnStop GoogleAPI

    /** display Location */
    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                latText.setText("lt: " + latitude);
                longText.setText("lt: " + longitude);
            } else {
                latText.setText("Enable location");
                longText.setText("Enable location");
            }
            return;
        }
        Log.d("Display Location Method", "No Permission Granted");
        return;
    }

    private void togglePeriodLocationUpdates() {
        if (!mRequestLocationUpdates) {
            //STOP LOCATION SERVICES
            mRequestLocationUpdates = true;
            startLocationUpdates();
        } else {
            //START LOCATION SERVICES
            mRequestLocationUpdates = false;
            stopLocationUpdates();
        }
    }

    /** Build Google API Client */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /** Create Location Request */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationInterval);
        mLocationRequest.setFastestInterval(locationFastestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(locationDisplacement);
    }

    /** Check Play Services */
    private boolean checkPlayServices() {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    /** Start Location Updates */
    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
            return;
        }

    }

    /** Stop Location Updates */
    public void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            return;
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();

        if(mRequestLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("CNCT FAILED", "Connection Failed: " + connectionResult.getErrorCode());

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location Changed", Toast.LENGTH_SHORT).show();
        displayLocation();
    }
}//end class
