package group22.quikschedule.Settings;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.AccessControlContext;
import java.util.Calendar;
import java.util.HashMap;

import group22.quikschedule.Calendar.ExpandedEventActivity;
import group22.quikschedule.InitialActivity;
import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.R;

import static com.facebook.FacebookSdk.getApplicationContext;
import static java.security.AccessController.getContext;

/**
 * Class: AlertActivity
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/5/16
 *
 * Description: Activity that deals with the efficiently showing notification for the users events
 *
 *
 * @author Ishjot Suri
 * @author Rudr Tandon
 */

public class AlertActivity extends BroadcastReceiver {

    private int mPage;
    private String[] dates;
    private String name;
    private String location;
    private String startTime;
    private String endTime;
    private String id;

    public static int id2;

    private int calculateminutes;
    private int timeToDisplay;
    /**
     * Description: OnCreate function called on create of the Activity utilized to navigate the user
     * through a series of Webviews
     * @param savedInstanceState - current SavedInstanceState
     */
    @Override
    public void onReceive(Context context, Intent i) {
        System.err.println("Received Broadcast");

        mPage = i.getExtras().getInt("Page");
        dates = i.getExtras().getStringArray("Dates");
        name = i.getExtras().getString("Name");
        location = i.getExtras().getString("Location");
        startTime = i.getExtras().getString("Start");
        endTime = i.getExtras().getString("End");
        id = i.getExtras().getString("Id");
        calculateminutes = i.getExtras().getInt("Calculate Minutes");
        timeToDisplay = i.getExtras().getInt("Time To Display");

       // createNotification(context, name, location + " at " + startTime + " - " + endTime , "");
        createNotification(context, name, location + " at " + startTime + " - " + endTime + "\nLeave in " +
                            calculateminutes + "mins" , "");
    }

    private void createNotification(Context context, String msg, String msgText, String msgAlert) {
        System.err.println("Creating Notification");

        Intent i = new Intent(context, ExpandedEventActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Parse the string here


         // Code to populate Event from notification.
        i.putExtra("Date", dates[mPage - 1]);
        i.putExtra("Name", name);
        i.putExtra("Location", location);
        i.putExtra("Start Time", startTime);
        i.putExtra("End Time", endTime);
        i.putExtra("ID", id);

        PendingIntent notificIntent = PendingIntent.getActivity(context, 1,
                i, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(msg)
                .setContentText(msgText)
                .setTicker(msgAlert)
                .setSmallIcon(R.drawable.qs_icon);

        builder.setContentIntent(notificIntent);

        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id2, builder.build());
        System.err.println("Notified");
    }

    public static int setAlarmtime(JSONObject jsonObj, Calendar cal) throws JSONException {

        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt("")); //GET SHIT FROM TY
        cal.set(Calendar.MINUTE, Integer.parseInt("")); //GET SHIT FROM TY

        return Integer.parseInt(""); //GET SHIT FROM TY
    }

    public static void setAlarm(View view) throws JSONException {
        System.err.println("Setting Alarm");

        Calendar c = Calendar.getInstance();
        //Set the alarm time for event i based on the start time and get the time back
        //id = setAlarmtime(null, c); //GET SHIT FROM TY

        id2 = 0;
        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.get(Calendar.SECOND)+5);

        //Set a new alertIntent for the notification
        Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);

        //set a pending intent where the unique id is the time of the event
        //If you have two events with the same time then it wont notify you for second
        PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), id2, alertIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //Create an AlarmManager for each event
        AlarmManager alarmManager  = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        //Set the alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), contentIntent);

    }

}
