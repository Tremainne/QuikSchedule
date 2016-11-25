package group22.quikschedule.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import group22.quikschedule.Calendar.EventView;
import group22.quikschedule.Calendar.ExpandedEventActivity;
import group22.quikschedule.R;

/**
 * Class: SettingsFragment
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/7/16
 *
 * Description: Activity that implements various settings buttons such as the connection to WebReg
 *              and SQLite database. Used primarily for testing and for connecting WebReg to the
 *              agenda. It also has a button that allows the user to go to Android's settings
 *              and edit the notifications for the app.
 *
 * @author David Thomson
 * @author Rohan Chhabra
 */
public class SettingsFragment extends Fragment {

    /**
     * Description: Default constructor, required as per instructions for Fragment class
     */
    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Description: Default starting method called when starting a new SettingsFragment
     * @param inflater - indicates which layer is to be brought to the front
     * @param container - indicates the current context of the layer
     * @param savedInstanceState - indicates the context from which the method was called
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        AppCompatButton notifications = (AppCompatButton) v.findViewById(R.id.notifications);
        notifications.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(i);
            }
        });

        return v;
    }
}
