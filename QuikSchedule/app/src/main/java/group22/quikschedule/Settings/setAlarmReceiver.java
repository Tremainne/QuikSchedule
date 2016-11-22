package group22.quikschedule.Settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import org.json.JSONException;

import java.util.Calendar;
import java.util.Comparator;
import java.util.PriorityQueue;

import group22.quikschedule.Calendar.DatabaseContract;
import group22.quikschedule.Calendar.DatabaseHelper;
import group22.quikschedule.Calendar.EventView;
import group22.quikschedule.NavigationDrawerActivity;

import static group22.quikschedule.Settings.AlertActivity.setAlarmtime;

/**
 * Created by TyDewes on 11/21/16.
 */

public class setAlarmReceiver extends BroadcastReceiver {

    public static int id2;
    private static int timeToDisplay;

    @Override
    public void onReceive(Context context, Intent i) {
        System.err.println( "Setting time" );

        timeToDisplay = i.getExtras().getInt( "Time To Display" );

        Calendar c = Calendar.getInstance();
        //Set the alarm time for event i based on the start time and get the time back
        try {
            id2 = setAlarmtime(c); //GET SHIT FROM TY
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        //c.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        //c.set(Calendar.SECOND, c.get(Calendar.SECOND)+5);
        //id2 = 0;

        //Set a new alertIntent for the notification
        Intent alertIntent = new Intent(context, AlertActivity.class);

        alertIntent.putExtra( "Name", i.getExtras().getString( "Name" ) );
        alertIntent.putExtra( "Location", i.getExtras().getString( "Location" ) );
        alertIntent.putExtra( "Start", i.getExtras().getString( "Start" ) );
        alertIntent.putExtra( "End", i.getExtras().getString( "End" ) );
        alertIntent.putExtra( "Id", i.getExtras().getString( "Id" ) );
        alertIntent.putExtra( "Materials", i.getExtras().getString( "Materials" ) );
        alertIntent.putExtra( "Comments", i.getExtras().getString( "Comments" ) );
        alertIntent.putExtra( "Calculate Minutes", i.getExtras().getInt( "Calculate Minutes" ) );
        alertIntent.putExtra( "Time To Display", i.getExtras().getInt( "Time To Display" ) );

        //set a pending intent where the unique id is the time of the event
        //If you have two events with the same time then it wont notify you for second
        PendingIntent contentIntent = PendingIntent.getBroadcast(context, id2, alertIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Create an AlarmManager for each event
        AlarmManager alarmManager  = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //Set the alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), contentIntent);
    }

    public static int setAlarmtime(Calendar cal) throws JSONException {
        System.err.println("Setting Time");
        int mins = 0, hours = 0;

        mins = timeToDisplay % 60;
        hours = timeToDisplay / 60;
        System.err.println("Hours: " + mins + "Mins");

        cal.set(Calendar.HOUR_OF_DAY, hours);
        cal.set(Calendar.MINUTE, mins);

        return timeToDisplay;
    }
}
