package group22.quikschedule.Settings;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.AccessControlContext;
import java.util.HashMap;

import group22.quikschedule.Calendar.ExpandedEventActivity;
import group22.quikschedule.MainActivity;
import group22.quikschedule.NavigationDrawerActivity;
import group22.quikschedule.R;

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

        mNotificationManager.notify(NavigationDrawerActivity.id, builder.build());
        System.err.println("Notified");
    }

}
