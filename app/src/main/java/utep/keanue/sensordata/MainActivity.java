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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
        ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        AdapterView.OnItemSelectedListener {

    //Permissions //
    final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    //Record //
    static boolean RECORD = false;

    //Settings //
    private static int setMeasurementInterval = 0;

    //Location Variables //
    private static int locationInterval = 10000;
    private static int locationFastestInterval = 5000;
    private static int locationDisplacement = 10;

    //Create private objects to use in application //
    private TextView xText, yText, zText, longText, latText, altText;
    private Button btn_save, btn_read, btn_delete, btn_toggleGPS;
    private Sensor AccelerometerSensor;
    private SensorManager sensorManager;

    // Data Strings //
    private String current_ac_data;     //Accelerometer data
    private String current_loc_data;    //Location data

    //Google API and Location //
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
        altText = (TextView) findViewById(R.id.alt_text);

        /* Buttons */
        //Assign Buttons
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_read = (Button) findViewById(R.id.btn_read);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_toggleGPS = (Button) findViewById(R.id.btn_toggleGPS);


        /* Spinner */
        Spinner spinner = (Spinner) findViewById(R.id.spinner_opt);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.array_seconds, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Spinner Action
        spinner.setOnItemSelectedListener(this);

        //Google Play Services
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

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
                Log.d("Clicked!", "Going into button");
                if(setMeasurementInterval == 0){
                    Toast.makeText(MainActivity.this, "Select Interval", Toast.LENGTH_SHORT).show();

                }

                //Button START
                else if(btn_save.getText().equals("START")){
                    Log.d("Clicked!", "StartMeasurements()");
                    //Run updateInterval
                    RECORD = true;
                    startMeasurements(setMeasurementInterval);
                    btn_save.setText("STOP");
                    //return;
                }
                else{
                    Log.d("Clicked!", "StopMeasurements()");
                    RECORD = false;
                    btn_save.setText("START");
                }

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

    /* File Management Method */
    /** Start Measurements
     * @param interval How many times per seconds the measurements are going to be made*/
    public void startMeasurements(int interval) {
        //How many times per second the read is going to be made
        final int millis =  1000/interval;

//        RECORD = true;

        //Create a new thread
        Thread t = new Thread() {

            @Override
            public void run() {
                int measurements_made = 0;
                try {
                    while (RECORD == true) {
                        Thread.sleep(millis);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Extract Global Variables
                                //Time Stamp
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd ## hh:mm:ss:SSS");
                                String timeStamp = simpleDateFormat.format(new Date());
                                //Concatenated Data
                                //String colTimeStamp = "<font color='#EE0000'>"+timeStamp+"</font>";
                                String full_data = (timeStamp +" ## RPS: " + setMeasurementInterval+"\n"
                                        + current_ac_data +" "+current_loc_data);


                                //Error Saving file (Maybe add exeption or something
                                if (!FileHelper.saveToFile(full_data)) {

                                    Toast.makeText(MainActivity.this, "Error save file!!!", Toast.LENGTH_SHORT).show();
                                    //return;
                                }


                            }
                        });
                        measurements_made++;
                    }//end while
                } catch (InterruptedException e) {}//end catch
                //Display Message
                String totalMeasurements = "Total Reads: "+measurements_made;
                //Toast.makeText(MainActivity.this, totalMeasurements, Toast.LENGTH_SHORT).show();
                Log.d("Total Measurements", totalMeasurements);
            }
        };

        t.start();
    }//end updateInterval

    /* Accelerometer Methods */
    /** Sensor Changed
     * This will change the TextView to the values of the sensor
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        //Global Display Variables
        xText.setText("X Axis: " + event.values[0]);
        yText.setText("Y Axis: " + event.values[1]);
        zText.setText("Z Axis: " + event.values[2]);

        //Local Variables
        String x = extractData(xText.getText().toString());
        String y = extractData(yText.getText().toString());
        String z = extractData(zText.getText().toString());

        //Save full accelerometer data
        current_ac_data = (x+","+y+","+z);
    }


    /* Google API Methods */
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

    /** Build Google API Client */
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    /* Play Services*/
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


    /* Location Services Methods */
    /** Start Location Updates */
    public void startLocationUpdates() {
        //CHECK IF PERMISSION IS OK!!!!

        //PERMISSION NOT OK!
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //Log.d("startLoc()", "NO PERM -> ask for permission");

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
            //Log.d("startLoc()", "OK PERM");
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

    /** Create Location Request */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(locationInterval);
        mLocationRequest.setFastestInterval(locationFastestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(locationDisplacement);
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

    /** On Location Changed*/
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location Changed", Toast.LENGTH_SHORT).show();
        displayLocation();
    }

    /** Display Location */
    private void displayLocation() {

        //If permission not ok
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
            Log.d("DisplayLoc()", "OK PERMISSION");

            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                //Get Last Location
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                double altitude = mLastLocation.getAltitude();

                //Display in Screen
                latText.setText("latitude: " + latitude);
                longText.setText("Longitude: " + longitude);
                altText.setText("Altitude: "+altitude);

                String sLat = extractData(latText.getText().toString());
                String sLong = extractData(longText.getText().toString());
                String sAlt = extractData(altText.getText().toString());

                //Save in global
                current_loc_data =(","+sLat+","+sLong+","+sAlt);

            } else {
                latText.setText("Enable location");
                longText.setText("Enable location");
                altText.setText("Enable location");
                return;
            }
        }
    }

    /* Extra Methods */
    /** Extract data from a String */
    private String extractData(String string){
        String[] parts = string.split(":");
        return parts[1];
    }


    /* Interface Methods (Not Used) */
    /** On Accuracy Changed (NOT USED)*/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Not Used
    }

    /* Google API Client Methods */
    /** On Connected (NOT USED)*/
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();

        if(mRequestLocationUpdates){
            startLocationUpdates();
        }
    }

    /** On Suspend (NOT USED)*/
    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    /** On Connection Failed (NOT USED) */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("CNCT FAILED", "Connection Failed: " + connectionResult.getErrorCode());

    }

    /** Spinner Methods */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if(position == 0){
            setMeasurementInterval = 0;
        }else {
            Log.d("Spinner", parent.getItemAtPosition(position).toString());
            setMeasurementInterval = Integer.parseInt(parent.getItemAtPosition(position).toString());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

    }
}//end class
