package group22.quikschedule.Maps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;

import group22.quikschedule.Calendar.DatabaseContract;
import group22.quikschedule.Calendar.DatabaseHelper;
import group22.quikschedule.Calendar.EventView;

/**
 * Created by Ty Dewes and David Thomson on 11/14/16.
 */

public class Polling extends BroadcastReceiver {

    private LatLng start;
    private GoogleApiClient client;
    int time;

    @Override
    public void onReceive( Context context, Intent intent )
    {
        PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
        PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "" );
        wl.acquire();

        Toast.makeText( context, "Alarm", Toast.LENGTH_LONG ).show();

        LocationListener listener = new LocationListener();

        client = new GoogleApiClient.Builder( context )
                .addApi( LocationServices.API )
                .addConnectionCallbacks( listener )
                .addOnConnectionFailedListener( listener )
                .build();

        // Need to get end
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);


        String sql = "SELECT " + DatabaseContract.DatabaseEntry.COLUMN_DATA + " FROM " +
                DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE " +
        DatabaseContract.DatabaseEntry.COLUMN_DAY + " IS '" + (day - 1) +"'";
        PriorityQueue<EventView> pq = DatabaseHelper.getEvents( context, sql );
        String end = "";
        Geocode.nameToLatLng(end, listener, false);

        wl.release();
    }

    public void setAlarm( Context context ) {
        AlarmManager am = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
        Intent i = new Intent( context, Polling.class );
        PendingIntent pi = PendingIntent.getBroadcast( context, 0, i, 0 );
        am.setRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24, pi );
    }

    public void setEventAlarm( Context context, int time ) {
        AlarmManager am = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
        Intent i = new Intent( context, Polling.class );
        PendingIntent pi = PendingIntent.getBroadcast( context, 0, i, 0 );
        am.setRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pi );
    }

    public void cancelAlarm( Context context ) {
        Intent intent = new Intent( context, Polling.class );
        PendingIntent pi = PendingIntent.getBroadcast( context, 0, intent, 0 );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
        alarmManager.cancel( pi );
    }

    private class LocationListener implements GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener, GeoCodeListener {


        private LatLng end;

        /**
         * When the Google API client connection succeeds, lookup our current location and parse it
         * to set the start.
         * @param connectionHint unused.
         */
        @Override
        public void onConnected( Bundle connectionHint ) {
            Location myLoc = LocationServices.FusedLocationApi.getLastLocation( client );
            String lat;
            String lng;
            if( myLoc != null ) {
                lat = String.valueOf( myLoc.getLatitude() );
                lng = String.valueOf( myLoc.getLongitude() );
                double latDbl = Double.parseDouble(lat);
                double lngDbl = Double.parseDouble(lng);
                setStart( new LatLng(latDbl, lngDbl) );
                onLatLngComplete();
            }
        }

        @Override
        public void onConnectionSuspended (int cause) {
            Log.d( "Maps", "Connection suspended"  );
        }

        /**
         * If the Google API connection fails
         * @param result The result of the connection.
         */
        @Override
        public void onConnectionFailed ( ConnectionResult result ) {
            Log.d( "Maps", "Connection failed"  );
        }

        /**
         * Set the start value for the event being polled for.
         * @param start The start value to set to.
         */
        @Override
        public void setStart(LatLng start) {
            Polling.this.start = start;
        }

        /**
         * Set the destination for the even being polled for.
         * @param end The destination to route to.
         */
        @Override
        public void setEnd(LatLng end) {
            this.end = end;
        }

        /**
         * When the start and end are known, make the request for the timing values.
         */
        @Override
        public void onLatLngComplete() {
            if (this.end != null && start != null) {
                Directions.makeTimeRequest(start, end, this);
            }
        }

        /**
         * Get the travel time for the first event from the directions lookup.
         */
        @Override
        public void onGeocodeListenerComplete() {
            time = Directions.getStaticTime();
        }

        /**
         * If the address lookup or the directions lookup fails, set the time to two hours by
         * default. May also want to indicate an error here, or verify location at event creation.
         */
        @Override
        public void onGeocodeListenerFail() {
            // Set time to be two hours if there was an error.
            time = 60*60*2*100;
        }
    }
}
