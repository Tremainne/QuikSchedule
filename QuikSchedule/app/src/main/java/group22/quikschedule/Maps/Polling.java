package group22.quikschedule.Maps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

/**
 * Created by Ty Dewes and David Thomson on 11/14/16.
 */

public class Polling extends BroadcastReceiver {

    @Override
    public void onReceive( Context context, Intent intent )
    {
        PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
        PowerManager.WakeLock wl = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, "" );
        wl.acquire();

        Toast.makeText( context, "Alarm", Toast.LENGTH_LONG ).show();

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
}
