package group22.quikschedule.Maps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import group22.quikschedule.Calendar.DatabaseContract;
import group22.quikschedule.Calendar.DatabaseHelper;
import group22.quikschedule.Calendar.EventView;
import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.Settings.AlertActivity;

/**
 * Created by Ty Dewes and David Thomson on 11/14/16.
 */

public class Polling extends BroadcastReceiver {

    private LatLng start;
    private GoogleApiClient client;
    private static boolean first = true;
    int time;
    private int transitMode;

    @Override
    public void onReceive( Context context, Intent intent )
    {
        PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
        PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "" );
        wl.acquire();

        String sql = "SELECT " + DatabaseContract.DatabaseEntry.COLUMN_DATA + " FROM " +
                DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE " +
                DatabaseContract.DatabaseEntry.COLUMN_DAY + " IS '" +
                NavigationDrawerActivity.getDayString() + "'";

        PriorityQueue<EventView> pq = DatabaseHelper.getEvents( context, sql );

        // Workaround, only need this the first time of every day
        if( first ) {
            setTwoHourAlarms( context, pq );
        }
        // Set alarms based on distances
        else {
            Toast.makeText( context, pq.toString(), Toast.LENGTH_LONG ).show();
            // Get map of evs w/ start time
            Map<Integer, EventView> map = new HashMap<Integer, EventView>();
            // Get PriorityQueue of start times
            PriorityQueue<Integer> timePQ = new PriorityQueue<Integer>(10);
            // Go through EventView PQ and add to map/PQ
            for ( EventView ev : pq ) {
                int start = ev.getTimeAsInt( EventView.STARTTIME );
                timePQ.add( start );
                map.put( start, ev );
            }
            getDir( context, map, timePQ );
        }

        wl.release();
    }

    public void setTwoHourAlarms( Context context, PriorityQueue<EventView> pq ) {
        for ( EventView ev : pq ) {
            int start = ev.getTimeAsInt(0);
            setEventAlarm(context, start - 120);
        }
        first = false;
    }

    public void getDir( Context context, Map<Integer, EventView> map, PriorityQueue<Integer> timePQ ) {
        // TODO
        // For now, just get the first event until we know it works, better way around this?
        // Will need a counter to figure out how many we have been through in the PQ
        Integer start = timePQ.peek();
        timePQ.remove();

        EventView curr = map.get( start );

        if( curr == null ) {
            Log.d( "Error", "Null object" );
        }

        LocationListener listener = new LocationListener();

        client = new GoogleApiClient.Builder( context )
                .addApi( LocationServices.API )
                .addConnectionCallbacks( listener )
                .addOnConnectionFailedListener( listener )
                .build();

        String end = curr.location;

        // try to get rid of room numbers, but keep potential zip codes
        String[] arr = end.split("\\w");
        StringBuilder result = new StringBuilder();
        for (String str : arr) {
            boolean isNum = str.matches("\\d+");
            // If its a number or  its a Zip code (length 5), put it back in the address.
            if (!isNum || str.length() == Directions.ZIP_LENGTH) {
                result.append(str);
            }
        }

        Geocode.nameToLatLng(result.toString(), listener, false);
        int duration = Directions.getStaticTime();

        Toast.makeText( context, duration, Toast.LENGTH_LONG ).show();

        int toDisplay = curr.getTimeAsInt( EventView.STARTTIME ) - duration - 10;

        Intent intent = new Intent(context, AlertActivity.class);
        intent.putExtra( "Name", curr.name );
        intent.putExtra( "Location", curr.location );
        intent.putExtra( "Start", curr.getTimeAsString( EventView.STARTTIME ) );
        intent.putExtra( "End", curr.getTimeAsString( EventView.ENDTIME ) );
        intent.putExtra( "Id", curr.id );
        intent.putExtra( "Name", curr.name );
        intent.putExtra( "Calculate Minutes", curr.getTimeAsInt( EventView.STARTTIME ) - duration );
        intent.putExtra( "Time To Display", toDisplay );
        context.startActivity( intent );
    }

    public void setAlarm( Context context ) {
        AlarmManager am = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
        Intent i = new Intent( context, Polling.class );
        PendingIntent pi = PendingIntent.getBroadcast( context, 0, i, 0 );
        am.setRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi );
    }

    public void setEventAlarm( Context context, int time ) {
        AlarmManager am = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
        Intent i = new Intent( context, Polling.class );
        PendingIntent pi = PendingIntent.getBroadcast( context, 0, i, 0 );
        am.setRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi );
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
                Directions.makeTimeRequest(start, end, transitMode, this);
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
