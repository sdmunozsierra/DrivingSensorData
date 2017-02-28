package utep.keanue.sensordata;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

    //Permissions
    final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;


    //Location Variables
    private static int locationInterval = 10000;
    private static int locationFastestInterval = 5000;
    private static int locationDisplacement = 10;

    //Create private objects to use in application
    private TextView xText, yText, zText, longText, latText;
    private Button btn_save, btn_read, btn_delete, btn_toggleGPS;
    private Sensor AccelerometerSensor;
    private SensorManager sensorManager;

    //The following can be removed and set as return elements
    //instead of having them as global variables
    // ** Security and Code Design **//
    private String current_ac_data;     //Accelerometer data

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

        /* Sensor Variables */
        // Create Sensor Manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Create Accelerometer Sensor
        AccelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register Sensor Listener
        sensorManager.registerListener(this, AccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        /* Text Views */
        //Assign Accelerometer
        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);

        //Assign GPS Location
        longText = (TextView) findViewById(R.id.long_text);
        latText = (TextView) findViewById(R.id.lat_text);

        /* Buttons */
        //Assign Buttons
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_read = (Button) findViewById(R.id.btn_read);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_toggleGPS = (Button) findViewById(R.id.btn_toggleGPS);
        Button btn_setting = (Button) findViewById(R.id.btn_settings);

        //TODO DEBUG
        //Google Play Services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        /* Settings Button */
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });//end onClick SaveFile);


        /* Toggle GPS */
        btn_toggleGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePeriodLocationUpdates();
            }
        });//end onClick SaveFile);


        /* Save on File */
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            /** Save current data along with timestamp to file */
            public void onClick(View v) {


                //Run updateInterval
                updateInterval(1, 5);
            }
        });//end onClick SaveFile


        // Read File
        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //START A NEW ACTIVITY
                startActivity(new Intent(MainActivity.this, readFile.class));
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
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd @ hh:mm:ss.SSS \n");
                                String timeStamp = simpleDateFormat.format(new Date());
                                //Concatenated Data
                                //String colTimeStamp = "<font color='#EE0000'>"+timeStamp+"</font>";
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

    /** Google API Client onStart */
    @Override
    public void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }//end OnStart GoogleAPI

    /** Google API Client onResume */
    @Override
    public void onResume() {
        checkPlayServices();
        if (mGoogleApiClient.isConnected() && mRequestLocationUpdates) {
            Log.d("ONRESUME CHECK GPS", "Check GPS on Resume");
            startLocationUpdates();
        }
        super.onResume();
    }//end OnResume GoogleAPI

    /** Google API Client onStop */
    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }//end OnStop GoogleAPI

    /** Google API Client onPause */
    @Override
    public void onPause() {
        stopLocationUpdates();
        super.onStop();
    }//end OnStop GoogleAPI


    /** Display Location */
    private void displayLocation() {

        //CHECK IF PERMISSION IS OK!!!!

        //PERMISSION NOT OK!
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("DisplayLoc()", "NO PERM -> ask for permission");

            //ASK FOR PERMISSION
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            Log.d("Asking ->", Manifest.permission.ACCESS_FINE_LOCATION);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

        // PERMISSION IS OK!
        else {
            Log.d("DisplayLoc()", "OK PERM");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                latText.setText("lt: " + latitude);
                longText.setText("lt: " + longitude);
            } else {
                latText.setText("Enable location");
                longText.setText("Enable location");
                return;
            }

        }
    }

    /** Toggle Location Updates */
    private void togglePeriodLocationUpdates() {
        if (!mRequestLocationUpdates) {
            btn_toggleGPS.setText(getString(R.string.Toggle_GPS_Off));
            mRequestLocationUpdates = true;
            startLocationUpdates();
        } else {
            btn_toggleGPS.setText(getString(R.string.Toggle_GPS_On));
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
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Log.d("Check Play Services()", "Success");
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    /** Start Location Updates */
    public void startLocationUpdates() {
        //CHECK IF PERMISSION IS OK!!!!

        //PERMISSION NOT OK!
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("startLoc()", "NO PERM -> ask for permission");

            //ASK FOR PERMISSION
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            Log.d("Asking ->", Manifest.permission.ACCESS_FINE_LOCATION);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

        // PERMISSION IS OK!
        else {
            Log.d("startLoc()", "OK PERM");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    /** Stop Location Updates */
    public void stopLocationUpdates() {
        //CHECK IF PERMISSION IS OK!!!!

        //PERMISSION NOT OK!
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("stopLoc()", "NO PERM -> ask for permission");

            //ASK FOR PERMISSION
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            Log.d("Asking ->", Manifest.permission.ACCESS_FINE_LOCATION);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }

        // PERMISSION IS OK!
        else {
            Log.d("stopLoc()", "OK PERM");
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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

    /* Implemented methods not used */
    /**
     * Method from interface (NOT USED)
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Not Used
    }


}//end class
