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
 * Description: PollingService that can be used to set up notifications to be run at midnight.
 *
 * @author David Thomson
 * @author Tynan Dewes
 */
public class PollingService extends Service {
    Polling polling = new Polling();

    /**
     * Description: OnCreate function called when this service is started.  No modifications made.
     */
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Description: OnStartCommand called when the Service is started.  Simply calls setAlarm
     * on the Polling object to set the alarms
     * @param intent  unused
     * @param flags   unused
     * @param startId unused
     * @return integer value based on start
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        polling.setAlarm(this);
        return START_STICKY;
    }

    /**
     * Description: onBind command overridden for necessity
     * @param intent  unused
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
