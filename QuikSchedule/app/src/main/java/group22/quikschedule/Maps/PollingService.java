package group22.quikschedule.Maps;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Class: PollingService
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/14/16
 *
 * Description:
 *
 * @author David Thomson
 * @author Tynan Dewes
 */
public class PollingService extends Service {
    Polling polling = new Polling();

    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        polling.setAlarm(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
