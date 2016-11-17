package group22.quikschedule.Maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ty Dewes and David Thomson on 11/14/16.
 */

public class AutoStart extends BroadcastReceiver {

    Polling polling = new Polling();

    @Override
    public void onReceive( Context context, Intent intent )
    {
        if (intent.getAction().equals( Intent.ACTION_BOOT_COMPLETED ) )
        {
            polling.setAlarm( context );
        }
    }
}
