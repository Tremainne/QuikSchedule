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
        System.err.println("WORK");

        createNotificationsForTheDay(context);

    }

    private void createNotificationsForTheDay(Context context) {
        HashMap<String, String> stringMap = new HashMap<String, String>();

        JSONObject jsonObj = null;// = ""; //GET THE STRING
        try {
            stringMap = getDataFromEvent(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //createNotification(context, stringMap.get("title"), stringMap.get("location") +
        //      "at" + stringMap.get("start"), stringMap.get("title"));
        createNotification(context, "CSE 110", "Centre 119 at 6:30pm ", "CSE 110");
    }

    private void createNotification(Context context, String msg, String msgText, String msgAlert) {
        System.err.println(" BEEITITITITTTTCHHH");

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

        mNotificationManager.notify(1, builder.build());
        System.err.println("WORK BEEITITITITTTTCHHH");
    }

    public static HashMap<String, String> getDataFromEvent(JSONObject jsonObj) throws JSONException {

        HashMap<String, String> strMap = new HashMap<String, String>();

        //final JSONObject jsonObj = new JSONObject(jsonStr);

        JSONObject endTime = jsonObj.getJSONObject("end").getJSONObject("dateTime");
        String endTimeString = endTime.toString();
        endTimeString = endTimeString.substring(24);
        strMap.put("end", endTimeString);

        JSONObject startTime = jsonObj.getJSONObject("start").getJSONObject("dateTime");
        String startTimeString = startTime.toString();
        startTimeString = startTimeString.substring(24);
        strMap.put("start", startTimeString);

        JSONObject summary = jsonObj.getJSONObject("summary");
        String summaryString = summary.toString();
        strMap.put("title", summaryString);

        JSONObject location = jsonObj.getJSONObject("location");
        String locationString = location.toString();
        strMap.put("location", locationString);

        return strMap;
    }

    public void getEventsFromDay()
    {

    }

}
