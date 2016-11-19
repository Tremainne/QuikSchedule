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
 * Created by RudrTandon on 10/30/16.
 */

public class AlertActivity extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.err.println("Received Broadcast");

        createNotification(context,"TEST1", "TEST2", "TEST3");

    }

    private void createNotification(Context context, String msg, String msgText, String msgAlert) {
        System.err.println("Creating Notification");

        Intent i = new Intent(context, ExpandedEventActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

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
