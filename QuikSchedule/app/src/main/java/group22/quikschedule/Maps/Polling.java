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

import java.util.Comparator;
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
    private static int counter = 1;
    private Context context;
    int duration;
    EventView curr = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        String sql = "SELECT " + DatabaseContract.DatabaseEntry.COLUMN_DATA + " FROM " +
                DatabaseContract.DatabaseEntry.TABLE_NAME + " WHERE " +
                DatabaseContract.DatabaseEntry.COLUMN_DAY + " IS '" +
                NavigationDrawerActivity.getDayString() + "'";

        PriorityQueue<EventView> pq = DatabaseHelper.getEvents(context, sql);

        // Workaround, only need this the first time of every day
        if (first) {
            setTwoHourAlarms(context, pq);
            first = false;
        }
        // Set alarms based on distances
        else {
            // Get map of evs w/ start time
            PriorityQueue<EventView> events = new PriorityQueue<>(10, new Comparator<EventView>() {
                public int compare(EventView event1, EventView event2) {
                    int start1 = event1.getTimeAsInt(EventView.STARTTIME);
                    int start2 = event2.getTimeAsInt(EventView.STARTTIME);
                    return (start1 < start2) ? -1 : 1;
                }
            });
            // Go through EventView PQ and add to new PQ based on start time
            for (EventView ev : pq) {
                events.add(ev);
            }
            getDir(context, events);
        }

        wl.release();
    }

    public void setTwoHourAlarms(Context context, PriorityQueue<EventView> pq) {
        Toast.makeText(context, "Setting alarms", Toast.LENGTH_SHORT).show();
        for (EventView ev : pq) {
            int start = ev.getTimeAsInt(0);
            setEventAlarm(context, start - 120);
        }
    }

    public void getDir( Context context, PriorityQueue<EventView> pq ) {
        if( pq.isEmpty() ) {
            first = true;
            return;
        }
        for( int i = 0; i < counter; i++ ) {
            curr = pq.peek();
            pq.remove();
        }
        ++counter;

        LocationListener listener = new LocationListener();
        client = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(listener)
                .addOnConnectionFailedListener(listener)
                .build();
        client.connect();

        String end = curr.location;

        String result = Directions.convertAddress(end);
        // try to get rid of room numbers, but keep potential zip codes

        Geocode.nameToLatLng(result, listener, false);
    }

    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Polling.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 60 * 60, pi);
    }

    public void setEventAlarm(Context context, int time) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Polling.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + time * 60 * 1000, pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, Polling.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
    }

    private class LocationListener implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, GeoCodeListener {

        private LatLng end;

        /**
         * When the Google API client connection succeeds, lookup our current location and parse it
         * to set the start.
         *
         * @param connectionHint unused.
         */
        @Override
        public void onConnected(Bundle connectionHint) {
            Location myLoc = LocationServices.FusedLocationApi.getLastLocation( client );
            if (myLoc == null) {
                myLoc = LocationServices.FusedLocationApi.getLastLocation(client);
            }
            String lat;
            String lng;
            if (myLoc != null) {
                lat = String.valueOf(myLoc.getLatitude());
                lng = String.valueOf(myLoc.getLongitude());
                double latDbl = Double.parseDouble(lat);
                double lngDbl = Double.parseDouble(lng);
                setStart( new LatLng(latDbl, lngDbl) );
            }
            else {
                setStart( new LatLng( 32.880254, -117.237643 ) );
            }
            onLatLngComplete();

        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d("Maps", "Connection suspended");
        }

        /**
         * If the Google API connection fails
         *
         * @param result The result of the connection.
         */
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d("Maps", "Connection failed");
        }

        /**
         * Set the start value for the event being polled for.
         *
         * @param start The start value to set to.
         */
        @Override
        public void setStart(LatLng start) {
            //Toast.makeText( context, "Start: " + start.toString(), Toast.LENGTH_LONG ).show();
            Polling.this.start = start;
        }

        /**
         * Set the destination for the even being polled for.
         *
         * @param end The destination to route to.
         */
        @Override
        public void setEnd(LatLng end) {
            //Toast.makeText( context, "End: " + end.toString(), Toast.LENGTH_LONG ).show();
            this.end = end;
        }

        /**
         * When the start and end are known, make the request for the timing values.
         */
        @Override
        public void onLatLngComplete() {
            if (this.end != null && start != null) {
                Directions.makeTimeRequest(start, end, curr.transportation, this);
            }
        }

        /**
         * Get the travel time for the first event from the directions lookup.
         */
        @Override
        public void onGeocodeListenerComplete() {
            duration = Directions.getStaticTime();

            Toast.makeText( context, "Duration: " + duration, Toast.LENGTH_LONG ).show();

            int toDisplay = curr.getTimeAsInt( EventView.STARTTIME ) - ( duration / 60 ) - 10;

            Intent intent = new Intent(context, AlertActivity.class);

            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Location", curr.location );
            intent.putExtra( "Start", curr.getTimeAsString( EventView.STARTTIME ) );
            intent.putExtra( "End", curr.getTimeAsString( EventView.ENDTIME ) );
            intent.putExtra( "Id", curr.id );
            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Materials", curr.materials );
            intent.putExtra( "Comments", curr.comments );
            intent.putExtra( "Calculate Minutes", curr.getTimeAsInt( EventView.STARTTIME ) - duration );
            intent.putExtra( "Time To Display", toDisplay );
            context.sendBroadcast( intent );
        }

        /**
         * If the address lookup or the directions lookup fails, set the time to two hours by
         * default. May also want to indicate an error here, or verify location at event creation.
         */
        @Override
        public void onGeocodeListenerFail() {
            // Set time to be two hours if there was an error.
            duration = 60 * 60 * 2 * 100;

            Toast.makeText( context, "Duration: " + duration, Toast.LENGTH_LONG ).show();

            int toDisplay = curr.getTimeAsInt( EventView.STARTTIME ) - ( duration / 60 ) - 10;

            Intent intent = new Intent(context, AlertActivity.class);

            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Location", curr.location );
            intent.putExtra( "Start", curr.getTimeAsString( EventView.STARTTIME ) );
            intent.putExtra( "End", curr.getTimeAsString( EventView.ENDTIME ) );
            intent.putExtra( "Id", curr.id );
            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Materials", curr.materials );
            intent.putExtra( "Comments", curr.comments );
            intent.putExtra( "Calculate Minutes", curr.getTimeAsInt( EventView.STARTTIME ) - duration );
            intent.putExtra( "Time To Display", toDisplay );
            context.sendBroadcast( intent );
        }
    }
}
