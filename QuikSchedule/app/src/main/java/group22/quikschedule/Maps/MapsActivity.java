package group22.quikschedule.Maps;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import group22.quikschedule.R;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.Manifest;
import android.support.v4.app.ActivityCompat;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Very basic activity that should set the map up.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback
{


    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap myMap;
    private boolean permissionDenied = false;

    public void setStart(LatLng start) {
        this.start = start;
    }

    private LatLng start;

    public void setEnd(LatLng end) {
        this.end = end;
    }

    private LatLng end;




    /**
     * sets up the map when the activity is created.
     * @param savedInstanceState - passed to super's onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Maps", "activity began");
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        showDirections("5030 Via Papel, San Diego 92122", Directions.codeToName("CENTR"));
        Log.d("Maps", "Made geocode request");






    }

    /** Zooms map to center hall on creation, and enables the myLocation button.
     *
     * @param map - The map that everything is displayed on.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        try {
            while (map == null)
            {
                Thread.sleep(10);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        map.clear();
        Log.d("Maps", "callback occurred.");
        LatLng centerHall = new LatLng(32.8772572,-117.2365204);
        map.addMarker(new MarkerOptions()
                .position(centerHall)
                .title("Center"));
        Log.d("Maps", "marker added.");
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(centerHall, 16.5f));
        myMap = map;
        map.setOnMyLocationButtonClickListener(this);
        enableMyLocation();


    }

    /** Method to handle myLocation button presses, doesn't really do anything.
     *
     * @return false so that the event continues and zoom happens.
     */
    @Override
    public boolean onMyLocationButtonClick(){
        Log.d("maps", "clicked the myLocation button.");
        return false;
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @TargetApi(23)
    private void enableMyLocation() {
        Log.d("maps", "trying to enable location");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            Log.d("maps", "fine permission needs request.");
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE);
            Log.d("maps", "fine permission requested.");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission to access the location is missing.
                Log.d("maps", "coarse permission needs request.");
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                Log.d("maps", "coarse permission requested.");
            }
        } else if (myMap != null) {
            // Access to the location has been granted to the app.
            Log.d("maps", "permission given");
            myMap.setMyLocationEnabled(true);
            Log.d("maps","enabled my location");
            myMap.getUiSettings().setMyLocationButtonEnabled(false);
            myMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    /** When we get the result for the permission to use myLocation is granted or not.
     *
     * @param requestCode - what permission was requested -- should be location permission
     * @param permissions - What permissions we have
     * @param grantResults - result of permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            Log.d("maps", "Permission denied");
            permissionDenied = false;
        }
    }

    /**
     * Checks if the permission has been granted.
     * @param grantPermissions  permissions
     * @param grantResults if they were granted or not
     * @param permission the permission we want to check on.
     * @return was it granted.
     */
    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults,
                                              String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    public void plotLine(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);
            Log.d("MapsActivity","onPostExecute lineoptions decoded");

        }

        // Drawing polyline in the Google Map for the i-th route
        if(lineOptions != null) {
            myMap.addPolyline(lineOptions);
        }
        else {
            Log.d("MapsActivity","without Polylines drawn");
        }

        Log.d("plotLine", "Should finish line.");
    }

    public void onGeocodeComplete() {
        if (start != null && end != null) {
            Directions.makeRequest(start, end, this);
        }
    }

    public void showDirections(String start, String end) {
        Geocode.nameToLatLng(start, this, true);
        Geocode.nameToLatLng(end, this, false);
    }
}