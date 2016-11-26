package group22.quikschedule.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import group22.quikschedule.R;

/**
 * Class: WeekFragment
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/12/2016
 *
 * Description: Fragment that contains the agenda tabs, the add button for adding events, and a
 *              button that allows the user to pick a date to go to. It sets up these three
 *              components and invokes other classes that implement them.
 *
 * @author Rohan Chhabra
 */
public class WeekFragment extends Fragment implements View.OnClickListener{

    private boolean fullAgenda = true; // used to determine if coming from agenda
    private View weekView; // View that contains the stuff in the WeekFragment
    private String[] dates; // dates for the week

    /**
     * Description: Default constructor.
     */
    public WeekFragment() {
    }

    /**
     * Description: Default starting method called when starting a new WeekFragment
     *
     * @param inflater - indicates which layer is to be brought to the front
     * @param container - indicates the current context of the layer
     * @param savedInstanceState - indicates the context from which the method was called
     * @return View, where the WeekFragment is created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        weekView = inflater.inflate(R.layout.fragment_week, container, false);

        AppCompatButton button = (AppCompatButton) weekView.findViewById(R.id.month);
        button.setOnClickListener(this);

        ImageButton addButton = (ImageButton) weekView.findViewById(R.id.addButton);
        addButton.setOnClickListener(this);

        createTabs();

        return weekView;
    }

    /**
     * Description: OnClick method that handles when the user clicks on the add button or the
     * "Go to Calendar" button.
     *
     * @param v where the buttons are
     * @return void
     */
    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.month:
                Intent i = new Intent(getActivity(), CalendarActivity.class);
                startActivity(i);
                break;
            case R.id.addButton:
                Intent i2 = new Intent(getActivity(), ExpandedEventActivity.class);
                i2.putExtra("Dates", dates);
                startActivity(i2);
                break;
        }
    }

    /**
     * Description: Creates and initializes the tabs that hold the agenda for the week.
     *
     * @return void
     */
    public void createTabs() {

        Calendar cal = Calendar.getInstance();

        if(getArguments() != null) {

            cal.set(getArguments().getInt("Year"), getArguments().getInt("Month"),
                    getArguments().getInt("Day"));
        }

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        //gets the dates for every day of the week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        DateFormat formatter = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        dates = new String[7];
        for(int i = 0; i < 7; i++) {
            dates[i] = formatter.format(cal.getTime());
            cal.roll(Calendar.DAY_OF_WEEK, 1); // increments the days to get each date
        }

        Bundle bundle = new Bundle();
        bundle.putStringArray("Dates", dates);
        bundle.putBoolean("Agenda", fullAgenda);

        ViewPager viewPager = (ViewPager) weekView.findViewById(R.id.pager);
        FragmentPageAdapter f = new FragmentPageAdapter(getChildFragmentManager(), bundle);
        viewPager.setAdapter(f);
        viewPager.setCurrentItem(dayOfWeek-1);

        TabLayout tabLayout = (TabLayout) weekView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        // converts the dates to a string format in order to initialize the tab titles
        for(int i = 0; i < 7; i++) {
            TabLayout.Tab t = tabLayout.getTabAt(i);
            t.setText(dates[i].substring(dates[i].length()-8, dates[i].length()-6));
        }
    }

}
