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
        @Override
        public void onConnected( Bundle connectionHint ) {
            Location myLoc = LocationServices.FusedLocationApi.getLastLocation( client );
            String lat = null;
            String lng = null;
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

        @Override
        public void onConnectionFailed ( ConnectionResult result ) {
            Log.d( "Maps", "Connection failed"  );
        }

        @Override
        public void setStart(LatLng start) {
            Polling.this.start = start;
        }

        @Override
        public void setEnd(LatLng end) {
            this.end = end;
        }

        @Override
        public void onLatLngComplete() {
            if (this.end != null && start != null) {
                Directions.makeTimeRequest(start, end, this);
            }
        }

        public void onComplete() {
            time = Directions.getStaticTime();
        }

        public void onFail() {
            // Set time to be two hours if there was an error.
            time = 60*60*2*100;
        }
    }
}
