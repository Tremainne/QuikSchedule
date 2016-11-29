package group22.quikschedule;

import android.content.Context;
import android.test.mock.MockContext;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

import org.junit.Test;

import group22.quikschedule.Calendar.EventView;
import group22.quikschedule.Maps.Polling;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Class: group22.quikschedule.PollingTest
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/25/16
 *
 * Description: Class used for testing functionality of Polling
 *
 * @author Tynan Dewes
 */
public class PollingTest extends Polling {

    final static double ZERO = 0.0;
    final static int DUR = 3000;
    final static String START_TIME = "2016-11-25T13:00:00.000";
    final static int ALARM_START = 770;
    final static int ALARM_START2 = 720;
    final static int HOURS = 12;
    final static int MINUTES = 0;

    @Test
    public void testSetStart() {
        Polling polling = new Polling();
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setStart( new LatLng( ZERO, ZERO ) );
        assertThat( polling.start, is( new LatLng( ZERO, ZERO ) ) );
    }

    @Test
    public void testSetEnd() {
        Polling polling = new Polling();
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setEnd( new LatLng( ZERO, ZERO ) );
        assertThat( listener.end, is( new LatLng( ZERO, ZERO ) ) );
    }

    @Test
    public void testOnLatLngCompleteWithNullEnd() {
        Polling polling = new Polling();
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setStart( new LatLng( ZERO, ZERO ) );
        listener.setEnd( null );
        listener.onLatLngComplete();
    }

    @Test
    public void testOnLatLngCompleteWithNullStart() {
        Polling polling = new Polling();
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setStart( null );
        listener.setEnd( new LatLng( ZERO, ZERO ) );
        listener.onLatLngComplete();
    }

    @Test
    public void testSetAlarmTime() {
        Calendar cal = Calendar.getInstance();
        Context context = new MockContext();
        EventView ev = new EventView( context );
        ev.startTime = START_TIME;
        Polling polling = new Polling();
        polling.curr = ev;
        Polling.LocationListener listener = polling.new LocationListener();
        assertThat( listener.setAlarmTime( cal ), is( ALARM_START ) );
    }

    @Test
    public void testSetAlarmTimeWithDuration() {
        Calendar cal = Calendar.getInstance();
        Context context = new MockContext();
        EventView ev = new EventView( context );
        ev.startTime = START_TIME;
        Polling polling = new Polling();
        polling.curr = ev;
        polling.duration = DUR;
        Polling.LocationListener listener = polling.new LocationListener();
        assertThat( listener.setAlarmTime( cal ), is( ALARM_START2 ) );
    }

    @Test(expected=NullPointerException.class)
    public void testSetAlarmTimeWithNullEV() {
        Calendar cal = Calendar.getInstance();
        Context context = new MockContext();
        EventView ev = new EventView( context );
        ev.startTime = START_TIME;
        Polling polling = new Polling();
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setAlarmTime( cal );
    }

    @Test(expected=NullPointerException.class)
    public void testSetAlarmTimeWithNullStartTime() {
        Calendar cal = Calendar.getInstance();
        Context context = new MockContext();
        EventView ev = new EventView( context );
        Polling polling = new Polling();
        polling.curr = ev;
        polling.duration = DUR;
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setAlarmTime( cal );
    }

    @Test
    public void testSetAlarmTimeCalendarUpdate() {
        Calendar cal = Calendar.getInstance();
        Context context = new MockContext();
        EventView ev = new EventView( context );
        ev.startTime = START_TIME;
        Polling polling = new Polling();
        polling.curr = ev;
        polling.duration = DUR;
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setAlarmTime( cal );
        assertThat( cal.get( Calendar.MINUTE ), is( MINUTES ) );
        assertThat( cal.get( Calendar.HOUR_OF_DAY ), is ( HOURS ) );
    }

    @Test(expected=NullPointerException.class)
    public void testSetAlarmTimeCalendarWithNullCalendar() {
        Calendar cal = null;
        Context context = new MockContext();
        EventView ev = new EventView( context );
        ev.startTime = START_TIME;
        Polling polling = new Polling();
        polling.curr = ev;
        polling.duration = DUR;
        Polling.LocationListener listener = polling.new LocationListener();
        listener.setAlarmTime( cal );
    }
}
