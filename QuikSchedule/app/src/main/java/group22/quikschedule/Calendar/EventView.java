package group22.quikschedule.Calendar;

import android.content.Context;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Class: EventView
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/30/16
 *
 * Description: This class is a custom TextView that holds all the information of each event and
 *              is shown on the UI in the form of a TextView. It contains data fields holding
 *              event information such as event name, start time, end time, etc. so that it can be
 *              accessed by other classes. It also contains two methods getTimeAsInt and
 *              getTimeAsString that format the times as ints and Strings to be used when
 *              displaying the event on the UI.
 *
 * @author Rohan Chhabra
 */
public class EventView extends TextView {

    public static final int STARTTIME = 0; // constant used for the getTimeAsInt, getTimeAsString
    public static final int ENDTIME = 1; // constant used for the getTimeAsInt, getTimeAsString
    public String name; // name of event
    public String location; // location of event
    public String startTime; // startTime of event
    public String endTime; // endTime of event
    public String id; // event id within the SQL database
    public String comments; // user comments about the event
    public String materials; // materials user needs to take for event
    // formatter for the DateTime String
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public int transportation; //0=transit, 1=driving, 2=cycling, 3=walking

    /**
     * Description: Initializes EventView as TextView
     *
     * @param context where the TextView is initialized
     * @return none
     */
    public EventView(Context context) {
        super(context);
    }

    /**
     * Description: Returns the given time as an int, which is used when formatting the EventView
     * for displaying.
     *
     * @param timeType defines whether to convert the start or end time
     * @return int, time returned as int
     */
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

    /**
     * Description: Returns the given time as an String, which is used when formatting the
     * EventView for displaying.
     *
     * @param timeType defines whether to convert the start or end time
     * @return String, time returned as String
     */
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
