package group22.quikschedule.Maps;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.TargetApi;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import group22.quikschedule.R;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.Manifest;
import android.support.v4.app.ActivityCompat;

import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;



/**
 * Very basic fragment that should set the map up.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback,
        OnMyLocationButtonClickListener, ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GeoCodeListener
{

    /**
     * called when the maps fragment is started, creates the view, calls create to make the map,
     * and begins the directions showing process if needed.
     * @param inflater Inflates the view
     * @param container what the view is inflated into
     * @param savedInstanceState unused
     * @return The view that was created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_maps, container, false);
        Bundle extras = getActivity().getIntent().getExtras();
        // If we have a destination, get it
        if( extras != null ) {
            destination = extras.getString("Location");
        }
        create();
        return view;
    }

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap myMap;
    private boolean permissionDenied = false;
    private GoogleApiClient client;

    /**
     * sets the start, part of geocode listener
     * @param start the value to set start to.
     */
    @Override
    public void setStart(LatLng start) {
        this.start = start;
    }

    private LatLng start;

    /**
     * sets the end, part of geocode listener
     * @param end the value to set end to.
     */
    @Override
    public void setEnd(LatLng end) {
        this.end = end;
    }

    private LatLng end;



    // temporary default
    private String destination = "CENTR";



    /**
     * sets up the map when the fragment is created. Also begins directions look up and display
     * step.
    */
    private void create() {
        Log.d("Maps", "activity began");
        // Set to null so that a new directions item can be displayed.
        start = null;
        end = null;
        getActivity().setContentView(R.layout.fragment_maps);
        SupportMapFragment mapFragment = (SupportMapFragment)
                (getActivity().getSupportFragmentManager().findFragmentById(R.id.map));

        // Used to find current/last tracked location
        client = new GoogleApiClient.Builder( getActivity() )
                .addApi( LocationServices.API )
                .addConnectionCallbacks( this )
                .addOnConnectionFailedListener( this )
                .build();

        //Get the google map
        mapFragment.getMapAsync(this);

        // Show the directions, poissbly use class code lookup.
        if (Directions.converter.containsKey(destination)) {
            showDirections(Directions.converter.get(destination));
        }
        else {
            showDirections(destination);
        }
        Log.d("Maps", "Made geocode request");
    }

    /**
     * Connects to API client
     */
    @Override
    public void onStart() {
        client.connect();
        super.onStart();
    }

    /**
     * API client callback, looks up last known location and sets the start to it.
     * @param connectionHint
     */
    @Override
    public void onConnected( Bundle connectionHint ) {
        Location myLoc = LocationServices.FusedLocationApi.getLastLocation( client );
        String lat;
        String lng;
        // Parse info about current location.
        if( myLoc != null ) {
            lat = String.valueOf( myLoc.getLatitude() );
            lng = String.valueOf( myLoc.getLongitude() );
            double latDbl = Double.parseDouble(lat);
            double lngDbl = Double.parseDouble(lng);
            start = new LatLng(latDbl, lngDbl);
            onLatLngComplete();
        }
    }

    /**
     * Called when the connection is suspended for some reason, hopefully won't happen
     * @param cause Why the connection was suspended.
     */
    @Override
    public void onConnectionSuspended (int cause) {
        Log.d( "Maps", "Connection suspended"  );
    }

    /**
     * Called if the connection to the API client fails, display error message
     * @param result the result of the connection
     */
    @Override
    public void onConnectionFailed ( ConnectionResult result ) {
        Toast.makeText(getContext(), "Error is connecting to Google API", Toast.LENGTH_LONG).show();
        Log.d( "Maps", "Connection failed"  );
    }

    /**
     * called when the map is ready to display, clears map and then sets up the myLocation button
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
        myMap = map;
        map.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    /**
     * Method to handle myLocation button presses, doesn't really do anything.
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
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            Log.d("maps", "fine permission needs request.");
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE);
            Log.d("maps", "fine permission requested.");
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
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

    /**
     * When we get the result for the permission to use myLocation is granted or not.
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

    /**
     * Plots the poly line on the map, using the directions from a Directions API call, and then
     * decoding the polyline that was sent with that result.
     *
     * @param result The directions that hold the polyline to decode.
     */
    public void plotLine(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

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
                builder.include( position );
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.MAGENTA);
            Log.d("MapsFragment","onPostExecute lineoptions decoded");

        }

        LatLngBounds bounds = builder.build();
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int padding = (int) (0.15 * dpWidth); // offset from edges of map in pixels
                                              // based on width of screen
        // Zoom the camera to show the whole line.
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        // Drawing polyline in the Google Map for the i-th route
        if(lineOptions != null) {
            Log.d("MapsFragment", "drawing Polyline");
            myMap.clear();
            myMap.addPolyline(lineOptions);
            // Place marker at destination.
            myMap.addMarker(new MarkerOptions()
                    .position(end)
                    .title("Destination"));
            myMap.moveCamera( cu );
        }
        else {
            Log.d("MapsFragment","without Polylines drawn");
        }

        Log.d("plotLine", "Should finish line.");
    }

    /**
     * Part of the GeocodeListener interface, will make the directions request once both the
     * start and end points of the route are known.
     */
    public void onLatLngComplete() {
        if (start != null && end != null) {
            Directions.makeDirectionsRequest(start, end, this);
            Log.d("MapsFragment", "making routing request");
        }
    }

    /**
     * Would be used to show directions from a specified to start to a specified end.
     *
     * Currently unused.
     *
     * @param start the starting location for the route
     * @param end The ending location for the route.
     */
    public void showDirections(String start, String end) {
        Geocode.nameToLatLng(start, this, true);
        Geocode.nameToLatLng(end, this, false);
    }

    /**
     * Shows directions, assuming a start based on the current location, and to the end location.
     * @param end The end location for the route.
     */
    public void showDirections(String end) {
        Log.d("MapsFragment", "showing directions");
        Geocode.nameToLatLng(end, this, false);
    }

    /**
     * When the directions completes, plot the line based on that.
     */
    @Override
    public void onGeocodeListenerComplete() {
        plotLine(Directions.getStaticDirections());
    }

    /**
     * If there is a problem with the location lookup or directions lookup, display an error.
     */
    @Override
    public void onGeocodeListenerFail() {
        Toast.makeText(getContext(), "Location could not be found", Toast.LENGTH_LONG).show();
    }
}