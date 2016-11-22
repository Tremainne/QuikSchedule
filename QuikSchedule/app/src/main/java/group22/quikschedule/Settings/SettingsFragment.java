package group22.quikschedule.Settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
 *              agenda.
 *
 * @author David Thomson
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

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}
