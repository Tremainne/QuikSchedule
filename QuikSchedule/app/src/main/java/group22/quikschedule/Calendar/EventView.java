package group22.quikschedule.Calendar;

import android.content.Context;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by RohanChhabra on 10/30/16.
 */

public class EventView extends TextView {

    public static final int STARTTIME = 0;
    public static final int ENDTIME = 1;
    String name;
    String location;
    String startTime;
    String endTime;
    String id;
    String description;
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    int transportation; //0=walking, 1=bike, 2=car, 3=bus

    public EventView(Context context) {
        super(context);
    }

    public int getTimeAsInt(int timeType) {

        Calendar c = Calendar.getInstance();

        try {
            Date inputDate;
            switch(timeType) {
                case STARTTIME:
                    inputDate = formatter.parse(startTime);
                    break;
                default:
                    inputDate = formatter.parse(endTime);
                    break;

            }

            c.setTime(inputDate);
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

        return (c.get(Calendar.HOUR_OF_DAY)*60)+c.get(Calendar.MINUTE);
    }

    public String getTimeAsString (int timeType) {

        Date inputDate = null;
        try {
            switch(timeType) {
                case STARTTIME:
                    inputDate = formatter.parse(startTime);
                    break;
                default:
                    inputDate = formatter.parse(endTime);
                    break;

            }
        }
        catch(ParseException e) {
            e.printStackTrace();
        }

        DateFormat dateFormat = new SimpleDateFormat("h:mm a");

        return dateFormat.format(inputDate);
    }
}
