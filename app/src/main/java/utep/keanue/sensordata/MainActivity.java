package utep.keanue.sensordata;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.provider.ContactsContract;
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
import android.text.Editable;
import android.util.Log;
//Text, buttons and toasts
import android.view.KeyEvent;
import android.view.PointerIcon;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//Google Play Services
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static utep.keanue.sensordata.R.attr.buttonBarButtonStyle;
import static utep.keanue.sensordata.R.attr.icon;

/**
 * Class that will demonstrate the use of Accelerometer and GPS sensors to: add research info.
 * implement: SensorEventListener
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.
        ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

    //Permissions //
    final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;
    final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    final static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 0;

    //Record && Location //
    static boolean RECORD = false;
    static boolean LOCATION = false;
    static int location_selector = -1;

    //Settings //
    private static int setMeasurementInterval = 0;
    private EditText myEditText;

    //Location Variables //
    private static int locationInterval = 10000;
    private static int locationFastestInterval = 5000;
    private static int locationDisplacement = 10;

    //Create private objects to use in application //
    private TextView xText, yText, zText,longText, latText, altText, instructionsText;
    private Sensor AccelerometerSensor;
    private SensorManager sensorManager;

    //Bottom Bar Object
    public BottomBar bottomBar;
    public BottomBar phantomBar;

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
        instructionsText = (TextView) findViewById(R.id.instructionsText);

        //Assign Instructions
        instructionsText.setText("Enter Interval:");

        //Assign GPS Location
        longText = (TextView) findViewById(R.id.long_text);
        latText = (TextView) findViewById(R.id.lat_text);
        altText = (TextView) findViewById(R.id.alt_text);

        /* Edit Text */
        myEditText = (EditText) findViewById(R.id.number_opt);
        myEditText.setText(""); //Set to empty
        myEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    if(!checkIntervalInput(myEditText.getText()) ){
                        Toast.makeText(MainActivity.this, "Interval between 1 - 50", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    //Update measurementInterval
                    setMeasurementInterval = Integer.parseInt(myEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });


        //Google Play Services`
        if (checkPlayServices()) {
            buildGoogleApiClient();
            createLocationRequest();
        }

        //Bottom Bar
        /** Bottom Bar
         * Thanks to this bar I will remove three buttons (Read, Start & Delete)
         * This is still in testing phase. */
        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.selectTabWithId(R.id.tab_home);


        findViewById(R.id.tab_readRecords).setOnClickListener(this);
        findViewById(R.id.tab_startRecording).setOnClickListener(this);
        findViewById(R.id.tab_home).setOnClickListener(this);
        findViewById(R.id.tab_location).setOnClickListener(this);
        findViewById(R.id.tab_deleteFile).setOnClickListener(this);

    }//end onCreate

    @Override
    public void onClick(View v) {
        Class clazz = null;

        switch (v.getId()) {
            case R.id.tab_readRecords:
                startActivity(new Intent(MainActivity.this, readFile.class));
                //bottomBar.selectTabWithId(R.id.tab_readRecords);
                break;
            case R.id.tab_startRecording:
                //TODO Create toasts for recording on/off
                if(setMeasurementInterval == 0) {
                    Toast.makeText(MainActivity.this, "Select Interval", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Start recording
                if(!RECORD){
                    RECORD = true;
                    startMeasurements(setMeasurementInterval);
                    //Change title
                    bottomBar.selectTabAtPosition(1,true);
                    TextView titleView = (TextView) bottomBar.findViewById(R.id.tab_startRecording).findViewById(R.id.bb_bottom_bar_title);
                    ImageView icon = (ImageView) bottomBar.findViewById(R.id.tab_startRecording).findViewById(R.id.bb_bottom_bar_icon);
                    icon.setImageResource(R.drawable.ic_stop_black_24dp);
                    //TODO use strings.xml
                    titleView.setText("Stop Recording");
                    break;
                }
                //Stop recording
                else {
                    RECORD = false;
                    TextView titleView = (TextView) bottomBar.findViewById(R.id.tab_startRecording).findViewById(R.id.bb_bottom_bar_title);
                    titleView.setText("Start Recording");
                    ImageView icon = (ImageView) bottomBar.findViewById(R.id.tab_startRecording).findViewById(R.id.bb_bottom_bar_icon);
                    icon.setImageResource(R.drawable.ic_play_black_24dp);
                    //Select home tab
                    bottomBar.selectTabAtPosition(2,true);
                }
                break;
            case R.id.tab_home:
                bottomBar.selectTabAtPosition(2,true);
                break;
            case R.id.tab_location:
                location_selector++;
                Log.d("Selector -> ", ""+location_selector);
                bottomBar.invalidate();
                if(location_selector %2 == 0){
                    Log.d("Selector = 0", "");
                    LOCATION = true;
                    togglePeriodLocationUpdates();
                    bottomBar.selectTabAtPosition(3,true);
                    TextView titleView = (TextView) bottomBar.findViewById(R.id.tab_location).findViewById(R.id.bb_bottom_bar_title);
                    titleView.setText("Disable Location");
                    ImageView icon = (ImageView) bottomBar.findViewById(R.id.tab_location).findViewById(R.id.bb_bottom_bar_icon);
                    icon.setImageResource(R.drawable.ic_location_off_black_24dp);

                    break;
                }
                else{
                    Log.d("Selector = 1", "");
                    LOCATION = false;
                    togglePeriodLocationUpdates();
                    TextView titleView = (TextView) bottomBar.findViewById(R.id.tab_location).findViewById(R.id.bb_bottom_bar_title);
                    titleView.setText("Enable Location");
                    ImageView icon = (ImageView) bottomBar.findViewById(R.id.tab_location).findViewById(R.id.bb_bottom_bar_icon);
                    icon.setImageResource(R.drawable.ic_location_on_black_24dp);
                }
                //bottomBar.selectTabAtPosition(4);
                bottomBar.selectTabAtPosition(2,true);
                break;
            case R.id.tab_deleteFile:
                bottomBar.selectTabAtPosition(2,true);
                File fileName = new File(FileHelper.path + FileHelper.fileName);
                if (FileHelper.deleteFile(fileName)) {
                    Toast.makeText(MainActivity.this, "Deleted file", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error delete file!!!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    /* File Management Methods */
    /** Start Measurements
     * @param interval How many times per seconds the measurements are going to be made*/
    public void startMeasurements(int interval) {

        //Request permission write external file
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);

        }
        //How many times per second the read is going to be made
        final int millis =  1000/interval;

        //Create a new thread
        Thread t = new Thread() {

            @Override
            public void run() {
                int measurements_made = 0;
                try {
                    while (RECORD) {
                        Thread.sleep(millis);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Extract Global Variables
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd ## hh:mm:ss:SSS");
                                String timeStamp = simpleDateFormat.format(new Date());
                                String interval = ("Interval: "+setMeasurementInterval);
                                //Concatenated Data
                                //String colTimeStamp = "<font color='#EE0000'>"+timeStamp+"</font>";
                                String full_data = (timeStamp +"\n" + current_ac_data +" "+current_loc_data);

                                //Error Saving file (Maybe add exeption or something)
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
     * This will change the TextView to the values of the sensor and saves the values to global string
     * @param event The smartphone automatically detects it.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        //Global Display Variables
        xText.setText("X Axis:\n" + event.values[0]);
        yText.setText("Y Axis:\n" + event.values[1]);
        zText.setText("Z Axis:\n" + event.values[2]);

        //Local Variables
        String x = extractDataSensor(xText.getText().toString());
        String y = extractDataSensor(yText.getText().toString());
        String z = extractDataSensor(zText.getText().toString());

        //Save full accelerometer data
        current_ac_data = (x+","+y+","+z);

        //TODO create a method for fixing x
        //Include: deal with negative numbers
        //Crop z values for 'neat' display
        String fixZ = z.substring(0,5);
        zText.setText("Z Axis:\n" + fixZ);

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
    //   @Override
//    public void onPause() {
//        stopLocationUpdates();
//        super.onStop();
//    }//end OnStop GoogleAPI

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
        //Check for permission

        //No permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d("stopLoc()", "No permission. Will ask for permission...");

            //ASK FOR PERMISSION
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            Log.d("Asking ->", Manifest.permission.ACCESS_FINE_LOCATION);
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
            //btn_toggleGPS.setText(getString(R.string.Toggle_GPS_Off));
            mRequestLocationUpdates = true;
            startLocationUpdates();
        } else {
            //btn_toggleGPS.setText(getString(R.string.Toggle_GPS_On));
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

                String sLat = extractDataLocation(latText.getText().toString());
                String sLong = extractDataLocation(longText.getText().toString());
                String sAlt = extractDataLocation(altText.getText().toString());

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
    private String extractDataSensor(String string){
        String[] parts = string.split("\n");
        return parts[1];
    }

    private String extractDataLocation(String string){
        String[] parts = string.split(":");
        return parts[1];
    }

    /** Method that checks if the input is on range
     * @param editText User input for interval*/
    private boolean checkIntervalInput(Editable editText){
        String stringCheck = editText.toString();
        int intCheck = Integer.parseInt(stringCheck);
        //LOG
        //Log.d("CheckInt", "int check: "+intCheck);
        if(intCheck < 1 || intCheck > 50)
            return false;
        return true;
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
    /** Spinner On Item Selected
     * DEPRECATED*/
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
