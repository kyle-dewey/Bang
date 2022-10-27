package com.example.bangv3;

//imports from the original bang
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import static java.lang.Math.*;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.os.Build;
import android.view.View;
import android.graphics.Color;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;
import android.provider.Settings;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.CameraPosition;

//imports automatically generated by android studio
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.example.bangv3.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Context context = this;

    private final int PERMISSIONS_FINE_LOCATION = 99;
    private final int PERMISSIONS_EXTERNAL_STORAGE = 100;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Location location;
    private double currLat;
    private double currLong;
    private double prevLat;
    private double prevLong;
    private String currMGRS;
    private double currAccuracy;
    private boolean mapReady = false;
    private boolean firstZoom = true;
    private boolean hasBeenCleared = false;
    private boolean servicesConnected = false;
    private double bearing;
    private boolean resetCameraPosition = true;
    private Marker currMarker;


    //these dictate how often the location is updated
    private final int DEFAULT_UPDATE_RATE = 10000;
    private final int FAST_UPDATE_RATE = 5000;

    //creates the marker file
    private EventMarkerSvcImpl eventMarkerSvcImpl;
    private ArrayList<EventMarker> eventMarkerArrayList;

    //part of the new google play location service code
    Location currentLocation;
    private LocationCallback locationCallBack;

    //tracks whether or not we are currently tracking location
    boolean updateOn = false;

    //Location request is a config file for all of the settings in FusedLocationProviderClient
    LocationRequest locationRequest;

    // basically handles all of the location services
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE=101;

    TextView tv_accuracy, tv_currentGrid;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        eventMarkerSvcImpl.writeObject();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //creates the data structure for saving marker objects
        eventMarkerSvcImpl  = new EventMarkerSvcImpl(getApplicationContext());
        super.onCreate(savedInstanceState);


        //these are the settings for how often the app is polling the gps. Values are in milliseconds
        locationRequest = new LocationRequest();
        locationRequest.setInterval(DEFAULT_UPDATE_RATE);
        locationRequest.setFastestInterval(FAST_UPDATE_RATE);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //this is triggered whenever our interval is met
        locationCallBack = new LocationCallback(){
            //location result is the most current location
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //updates the UI with the new location
                Location location = locationResult.getLastLocation();
                updateUI(location);
                //rebuilds the custom info window so that the current location can be passed to in via the constructor
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(context, currMarker));

            }
        };

        //i think this is the new version of setContentView
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //assigns the two text views to the varibles
        tv_accuracy = binding.accuracy;
        tv_currentGrid = binding.currentGrid;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        updateGPS();
    }

    @Override
    protected void onPause() {

        resetCameraPosition = true;
        eventMarkerSvcImpl.writeObject();
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    protected void onResume() {
        updateGPS();
        startLocationUpdates();
        super.onResume();



    }

    @Override
    protected void onStop() {

        resetCameraPosition = true;
        eventMarkerSvcImpl.writeObject();
        super.onStop();
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSIONS_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this, "update permissions", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.print("XXXXXXXXXXXXXXXXXX onMapReady() CALLED XXXXXXXXXXXXX");

        mMap = googleMap;
        mMap.clear();

        mapReady = true;

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //deals with an unknown initial position
        unknownLocationMarker();


        //centers the camera on the given position
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currMarker.getPosition()));

        try {

            //serialization code for rebuilding all the saved event markers.
            eventMarkerSvcImpl.clearList();
            eventMarkerSvcImpl.readObject();
            eventMarkerArrayList = eventMarkerSvcImpl.getList();
            recreateEventMarkers(eventMarkerArrayList);

        }catch (IOException f){

            //if this is the first time opening the app this code adds an empty arraylist to the service layer and saves it.
            System.out.println("XXXXXXXXXXXXXXXXXx");
            System.out.println("there are no additional markers to add");
            eventMarkerArrayList = new ArrayList<EventMarker>();
            eventMarkerSvcImpl.writeObject();
        }



        //onlongclick for adding a marker to the map
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                createEventMarker(latLng);

            }
        });

        // onclick or the custom info window launches the editMarker activity if the marker pressed is not current location
        mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                if(marker.getId().compareTo(currMarker.getId()) == 0){

                    //prevents a click event on the current position marker's window from crashing the app
                    System.out.println("current location window pressed");

                }else {

                    //launches the editMarker activity. passes the marker's id as an extra
                    Intent intent = new Intent(getApplicationContext(), EditMarker.class);
                    intent.putExtra(EditMarker.EXTRA_ID, marker.getTitle());
                    startActivity(intent);

                }
            }
        });
    }

    //actions taken when the location of the device changes on initial start up the camera pans to the users
    //location. If the users location changes by more than 100m the camera recenters the user
    public void onLocationChanged(Location location) {

        currLat = location.getLatitude();
        currLong = location.getLongitude();

        currAccuracy = location.getAccuracy();


        currMGRS = currLat + ", " + currLong;

        LatLng currentLocation = new LatLng(currLat , currLong );
        LatLng previousLocation = new LatLng(prevLat, prevLong);

        //declares the current grid and accuracy fields
        TextView currentLocationView = findViewById(R.id.currentGrid);
        TextView accuracyView = findViewById(R.id.accuracy);

        currentLocationView.setText(currMGRS);
        accuracyView.setText(String.valueOf(Math.round(currAccuracy)) + " m");

        if(mapReady) {

            //deletes the previous marker
            if(currMarker != null){

                currMarker.remove();
            }

            //adds the current location marker
            currMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("Current Location ")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            );

            //rebuilds the custom info window so that the current location can be passed to in via the constructor
            //mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this, currMarker));


            //handles the camera view mechanics
            if(firstZoom){

                centerCamera(currentLocation);

                firstZoom = false;

                prevLat = currLat;
                prevLong = currLong;

                //tests to see if the device has moved at least 100m before panning the camera to the user
            }else if(calculateDistance(previousLocation,currentLocation) > 100){

                //pans to current location without changing the zoom level
                centerCamera(currentLocation);

                prevLat = currLat;
                prevLong = currLong;

            }


            //gives you the zoom buttons
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    //used to insert defaults into the UI when location is unknown
    public void unknownLocationMarker(){

        try {

            try {
                currLat = location.getLatitude();
                currLong = location.getLongitude();
            }catch(NullPointerException h){
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                System.out.println("locationProvider.getLastLocation produced a nullPointerExecption");
            }
            currAccuracy = 999999;
            currMGRS = "Unknown";
            LatLng currentLocation = new LatLng(currLat , currLong );

            //declares the current grid and accuracy fields
            TextView currentLocationView = findViewById(R.id.currentGrid);
            TextView accuracyView = findViewById(R.id.accuracy);

            //sets the text views to their default
            currentLocationView.setText(currMGRS);
            accuracyView.setText(String.valueOf(Math.round(currAccuracy)) + " m");

            if(mapReady) {


                //deletes the previous marker
                if (currMarker != null) {

                    currMarker.remove();
                }

                //adds the current location marker
                currMarker = mMap.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("Last Known Location ")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                );

                //gives you the zoom buttons
                mMap.getUiSettings().setZoomControlsEnabled(true);

            }
        }catch(SecurityException se){

        }

    }

    /**
    //checks permissions and then starts a fusedLocation update listener
    private void requestPermissions() {

        //performs a permissions check to make sure the app has location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // TODO: Consider calling

            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;

            Toast.makeText(context, "User must grant permission to use location services, and internal storage", duration).show();

            goToSettings();
            return;
        }
        return;

    }
     **/

    //sets up location services
    private void startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    }

    //calculates the distance between two points on the globe
    public static double calculateDistance(LatLng StartP, LatLng EndP) {
        int Radius=6371;//radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult= Radius*c;
        double km=valueResult/1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec =  Integer.valueOf(newFormat.format(km));
        double meter=valueResult%1000;
        int  meterInDec= Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value",""+valueResult+"   KM  "+kmInDec+" Meter   "+meterInDec);

        return Radius * c *1000;
    }

    //computes bearing between two points
    public static double computeBearing(LatLng from, LatLng to) {


        double fromLat = toRadians(from.latitude);
        double fromLng = toRadians(from.longitude);
        double toLat = toRadians(to.latitude);
        double toLng = toRadians(to.longitude);
        double dLng = toLng - fromLng;
        double heading = atan2(
                sin(dLng) * cos(toLat),
                cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng));
        return toDegrees(heading);
        //return wrap(toDegrees(heading), -180, 180);
    }

    //writes an updated snippit for markers based on two latlngs
    public static String writeSnippet(LatLng currLatLng, LatLng markerLatLng, String distanceString, String bearingString){

        //computes bearing
        double bearing = Math.round(computeBearing(currLatLng, markerLatLng));
        if(bearing < 0){ bearing = bearing +360;}

        //computes distance
        double distance = calculateDistance(markerLatLng , currLatLng);

        //concatinates a string to return
        //String result = ( "Distance: " + Math.round(distance) + " m " + "Bearing: " + bearing + " deg" );
        String result = ( distanceString+ ": " + Math.round(distance) + " m " + bearingString + ": " + bearing + " deg" );

        return result;
    }

    //sends the user to the app settings to change location permissions
    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }

    //centers the camera on target LatLng provided as an argument
    public void centerCamera(LatLng target){
        //pans and zooms the camera to current location sets zoom
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(target)
                .zoom(17)
                .bearing(0)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //creates a new marker and adds it to the marker array
    public void createEventMarker(LatLng latLng){

        System.out.println("createEventMarker() CALLED");
        //EventMarker newEventMarker = new EventMarker(eventMarkerSvcImpl.getCurrentID(),"Marker " +
                //Integer.toString(eventMarkerSvcImpl.getCurrentID()) ,latLng.latitude,latLng.longitude);

        EventMarker newEventMarker = new EventMarker(eventMarkerSvcImpl.getCurrentID(),getString(R.string.marker) + " " +
                Integer.toString(eventMarkerSvcImpl.getCurrentID()) ,latLng.latitude,latLng.longitude, context.getString(R.string.defaultDescriptionBlurb));

        eventMarkerSvcImpl.addEventMarker(newEventMarker);
        //eventMarkerArrayList.add(newEventMarker);

        Marker aMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(Integer.toString(newEventMarker.getID()))
                .snippet(newEventMarker.getDescription()));

        eventMarkerSvcImpl.printListContent();

        eventMarkerSvcImpl.writeObject();
    }

    //reinstantiates all of the markers in the .bin
    public void recreateEventMarkers(ArrayList<EventMarker> markerList){

        System.out.println("recreateEventMarkers() CALLED");
        System.out.println(markerList.size());
        ArrayList<EventMarker> temp = markerList;

        for(int x = 0 ; x < temp.size(); x++) {

            LatLng tempLatLng = new LatLng(temp.get(x).getLatPosition(), temp.get(x).getLongPosition());

            Marker aMarker = mMap.addMarker(new MarkerOptions()
                    .position(tempLatLng)
                    .title(Integer.toString(temp.get(x).getID()))
                    .snippet("default"));

            //sets the bearing attribute.
            temp.get(x).setBearing(Math.round(computeBearing(currMarker.getPosition(), aMarker.getPosition())));
            if (temp.get(x).getBearing() < 0) {
                temp.get(x).setBearing(temp.get(x).getBearing() + 360);
            }

            temp.get(x).setDistanceMeters(calculateDistance(aMarker.getPosition(), currMarker.getPosition()));

            aMarker.setSnippet("Distance: " + Math.round(temp.get(x).getDistanceMeters()) + " m "
                    + "Bearing: " + temp.get(x).getBearing() + " deg");

            if(temp.get(x).getDisplayHazard()) {
                if (temp.get(x).getPrimaryHazRadius() > 0.0) {

                    //draw primary hazard area
                    Circle aCircle = mMap.addCircle(new CircleOptions()
                            .center(tempLatLng)
                            .radius(temp.get(x).getPrimaryHazRadius())
                            .strokeColor(Color.RED)
                            .fillColor(Color.parseColor("#500084d3")));

                    aCircle.setStrokeWidth(5);


                }
                if (temp.get(x).getSecondaryHazRadius() > 0.0) {

                    //draw secondary hazard area
                    Circle bCircle = mMap.addCircle(new CircleOptions()
                            .center(tempLatLng)
                            .radius(temp.get(x).getSecondaryHazRadius())
                            .strokeColor(Color.GREEN)
                            .fillColor(Color.parseColor("#500084d3")));

                    bCircle.setStrokeWidth(5);

                }
            }
        }
    }

    //prevents back click events from sending the user back to the editMarker activity and creating
    // weird paths through the application
    @Override
    public void onBackPressed(){
        System.out.println("back pressed");
        //intializes an intent to pass to another activity
        //Intent intent = new Intent(this , TitlePage.class);
        //startActivity(intent);

    }

    //updates the current location text view on the UI
    public void updateUI(Location location){

        //deletes the previous marker
        if(currMarker != null){

            currMarker.remove();
        }

        String currLat = String.valueOf(location.getLatitude());
        String currLong = String.valueOf(location.getLongitude());
        tv_currentGrid.setText(currLat + ", " + currLong);
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        LatLng currentLatLong = new LatLng(location.getLatitude(), location.getLongitude());

        //centers the camera on the given position

        if(resetCameraPosition) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLong));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
            resetCameraPosition = false;
        }
        //adds the current location marker
        currMarker = mMap.addMarker(new MarkerOptions()
                .position(currentLatLong)
                //.title("Current Location ")
                .title(context.getString(R.string.currentLocation))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );

    }

    //asks for permission and then updates the GPS location
    public void updateGPS(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);

        //checking to ensure the app has the correct permissions
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //user provided permissions
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                //the location that android passes into this is the current location
                public void onSuccess(@NonNull Location location) {
                    //permission was granted. We now have access to location.
                    updateUI(location);
                    startLocationUpdates();
                    LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
                }
            });
        }else {
            //go get permission
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
        }
        //checking to ensure the app has storage permissions
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            //has permissions

        }else{
            //gets permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_EXTERNAL_STORAGE);
            }
        }

    }

    //onclick function for the ui button that recenters the camera on the user
    public void onClickRecenterCamera(View view){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currMarker.getPosition()));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
    }
}