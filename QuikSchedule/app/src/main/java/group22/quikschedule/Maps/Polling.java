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

import java.util.Calendar;
import java.util.Comparator;
import java.util.PriorityQueue;

import group22.quikschedule.Calendar.DatabaseContract;
import group22.quikschedule.Calendar.DatabaseHelper;
import group22.quikschedule.Calendar.EventView;
import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.Settings.AlertActivity;

/**
 * Class: Polling
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/14/16
 *
 * Description: Class that handles actively polling for user location to send notifications for
 * the user's events in a timely manner.
 *
 * @author Tynan Dewes
 * @author David Thomson
 * @author Christoph Steefel
 */
public class Polling extends BroadcastReceiver {

    public LatLng start;
    private GoogleApiClient client;
    private static boolean first = true;
    private static int counter = 1;
    private Context context;
    public int duration;
    public EventView curr = null;
    public static int id;
    public static final int BUFFER = 10;
    public static final int MINUTE = 60;
    public static final int MILLI = 1000;

    /**
     * Description: OnReceive function called when a broadcast is sent to this receiver.  Handles
     * which type of receiving job should be carried out based on boolean value
     * @param context current context
     * @param intent  intent passed to the broadcast
     * @return        void
     */
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
            counter = 1;
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

    /**
     * Description: Helper function used to set alarms two hours before each event starts.  This
     * is called once a day at midnight to set relevant alarms for the following day
     * @param context current context
     * @param pq      PriorityQueue of events used to see events for the day
     * @return        void
     */
    public void setTwoHourAlarms(Context context, PriorityQueue<EventView> pq) {
        Toast.makeText(context, "Setting alarms", Toast.LENGTH_SHORT).show();
        for (EventView ev : pq) {
            int start = ev.getTimeAsInt(EventView.STARTTIME);
            setEventAlarm(context, start);
        }
    }

    /**
     * Description: Helper function used to get duration time from current location to the
     * destination for the next upcoming event.  This function is called once two hours before
     * each event to determine when the notification should be sent for the event based on
     * duration time.
     * @param context current context
     * @param pq      PriorityQueue of events used to see events for the day
     * @return        void
     */
    public void getDir( Context context, PriorityQueue<EventView> pq ) {
        // Empty check
        if( pq.isEmpty() ) {
            first = true;
            return;
        }
        // Get next event based on global variables
        for( int i = 0; i < counter; i++ ) {
            if( pq.size() == 1 ) {
                first = true;
            }
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

        // Get duration of travel using nameToLatLng function
        String result = Directions.convertAddress(end);
        Geocode.nameToLatLng(result, listener, false);
    }

    /**
     * Description: Helper function used to set alarm once a day at midnight as described above.
     * @param context current context
     * @return        void
     */
    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Polling.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        // Set calendar to midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set( Calendar.HOUR_OF_DAY, 0 );
        calendar.set( Calendar.MINUTE, 00 );
        calendar.set( Calendar.SECOND, 00 );
        calendar.add( Calendar.HOUR_OF_DAY, 24 ); // Repeating

        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    /**
     * Description: Helper function used to set alarm based on inputted time.  Used to set alarm
     * two hours before each event starting time.  Subtracts 2 hours from each starting time when
     * called.
     * @param context current context
     * @param time    starting time of the event
     * @return        void
     */
    public void setEventAlarm(Context context, int time) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Polling.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, time, i, 0);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + time * MINUTE * MILLI -
                (2 * MINUTE * MINUTE * MILLI), pi);
    }

    /**
     * Class: LocationListener
     *
     * Bugs: None known
     * Version: 1.0
     * Date: 11/14/16
     *
     * Description: Class that handles Location listening for use with the active polling system.
     *
     * @author Tynan Dewes
     * @author Cristoph Steefel
     */
    public class LocationListener implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, GeoCodeListener {

        public LatLng end;

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
                myLoc = LocationServices.FusedLocationApi.getLastLocation( client );
            }
            String lat;
            String lng;
            // Get starting location
            if (myLoc != null) {
                lat = String.valueOf( myLoc.getLatitude() );
                lng = String.valueOf( myLoc.getLongitude() );
                double latDbl = Double.parseDouble( lat );
                double lngDbl = Double.parseDouble( lng );
                setStart( new LatLng( latDbl, lngDbl ) );
            }
            else {
                setStart( new LatLng( 32.880254, -117.237643 ) );  // This should never happen
            }
            // Call final function using current start location
            onLatLngComplete();
        }

        /**
         * When the Google API client connection is suspended, log it.
         *
         * @param cause unused
         */
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
            Polling.this.start = start;
        }

        /**
         * Set the destination for the even being polled for.
         *
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
                Directions.makeTimeRequest(start, end, curr.transportation, this);
            }
        }

        /**
         * Description: Helper function used to get time for starting alarm.  Should be sent 10
         * minutes before the user has to leave for the event.
         *
         * @param cal Calendar object to be modified for starting time
         * @return unused int
         */
        public int setAlarmTime( Calendar cal ) {
            int mins = 0, hours = 0;

            mins = ( curr.getTimeAsInt( EventView.STARTTIME ) - ( duration / MINUTE ) -
                    BUFFER ) % MINUTE;
            hours = ( curr.getTimeAsInt( EventView.STARTTIME ) - ( duration / MINUTE ) -
                    BUFFER ) / MINUTE;

            cal.set(Calendar.HOUR_OF_DAY, hours);
            cal.set(Calendar.MINUTE, mins);

            return curr.getTimeAsInt( EventView.STARTTIME ) - ( duration / MINUTE ) - BUFFER;
        }

        /**
         * Description: Helper function used to set notification for correct time.  Should be sent
         * 10 minutes before the user has to leave for the event.
         *
         * @param intent Intent broadcasted to setNotification
         * @return void
         */
        public void setNotification( Intent intent ) {
            Calendar c = Calendar.getInstance();
            //Set the alarm time for event i based on the start time and get the time back
            id = setAlarmTime( c );
            Toast.makeText( context, "Next Event: " + id/MINUTE + ":" + id%MINUTE + ".",
                    Toast.LENGTH_LONG ).show();

            //set a pending intent where the unique id is the time of the event
            //If you have two events with the same time then it wont notify you for second
            PendingIntent contentIntent = PendingIntent.getBroadcast(context, id, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            //Create an AlarmManager for each event
            AlarmManager alarmManager  = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            //Set the alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), contentIntent);
        }

        /**
         * Get the travel time for the first event from the directions lookup.
         */
        @Override
        public void onGeocodeListenerComplete() {
            duration = Directions.getStaticTime();

            Toast.makeText( context, "Duration: " + duration, Toast.LENGTH_LONG ).show();

            int toDisplay = curr.getTimeAsInt( EventView.STARTTIME ) - ( duration / MINUTE );

            Intent intent = new Intent(context, AlertActivity.class);

            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Location", curr.location );
            intent.putExtra( "Start", curr.getTimeAsString( EventView.STARTTIME ) );
            intent.putExtra( "End", curr.getTimeAsString( EventView.ENDTIME ) );
            intent.putExtra( "Id", curr.id );
            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Materials", curr.materials );
            intent.putExtra( "Comments", curr.comments );
            intent.putExtra( "Calculate Minutes", toDisplay );
            intent.putExtra( "Time To Display", toDisplay - BUFFER );
            setNotification( intent );
        }

        /**
         * If the address lookup or the directions lookup fails, set the time to two hours by
         * default. May also want to indicate an error here, or verify location at event creation.
         */
        @Override
        public void onGeocodeListenerFail() {
            // Set time to be two hours if there was an error.
            duration = MINUTE * MINUTE * 2 * 100;

            Toast.makeText( context, "Duration: " + duration, Toast.LENGTH_LONG ).show();

            int toDisplay = curr.getTimeAsInt( EventView.STARTTIME ) - ( duration / MINUTE );

            Intent intent = new Intent(context, AlertActivity.class);

            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Location", curr.location );
            intent.putExtra( "Start", curr.getTimeAsString( EventView.STARTTIME ) );
            intent.putExtra( "End", curr.getTimeAsString( EventView.ENDTIME ) );
            intent.putExtra( "Id", curr.id );
            intent.putExtra( "Name", curr.name );
            intent.putExtra( "Materials", curr.materials );
            intent.putExtra( "Comments", curr.comments );
            intent.putExtra( "Calculate Minutes", toDisplay );
            intent.putExtra( "Time To Display", toDisplay - 10 );
            setNotification( intent );
        }
    }
}
