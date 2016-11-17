package group22.quikschedule.Maps;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Tynan Dewes and David Thomson on 11/14/16.
 */

public class PollingService extends Service {
    Polling polling = new Polling();

    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        polling.setAlarm( this );
        return START_STICKY;
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }
}
