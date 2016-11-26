package group22.quikschedule.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Class: FragmentPageAdapter
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/12/2016
 *
 * Description: Handles switching between tabs and instantiating each fragment with the
 *              appropriate DayFragment content. Gets an array of dates that will be used as titles
 *              for each tab.
 *
 * @author RohanChhabra
 */
public class FragmentPageAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 7; //number of tab pages
    private String tabTitles[] = new String[7]; //titles for the tabs
    private final Bundle fragmentBundle; // bundle that gets the input data

    /**
     * Description: constructor for the FragmentPageAdapter, doesn't do anything but gets the data
     * from the input bundle.
     *
     * @param fm helps manage the fragments
     * @param data input dates for the tabs
     */
    public FragmentPageAdapter(FragmentManager fm, Bundle data) {
        super(fm);
        fragmentBundle = data;
        tabTitles = fragmentBundle.getStringArray("Dates");
    }

    /**
     * Description: Gets number of pages, used for changing the fragments.
     *
     * @return int, number of pages
     */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    /**
     * Description: Returns a fragment for the FragmentPagerAdapter to switch between.
     *
     * @param position current tab opened
     * @return Fragment, DayFragment that is on the tab layout
     */
    @Override
    public Fragment getItem(int position) {

        return DayFragment.newInstance(position+1, tabTitles);
    }
}