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
    final int PAGE_COUNT = 7;
    private String tabTitles[] = new String[7];
    private final Bundle fragmentBundle;

    public FragmentPageAdapter(FragmentManager fm, Bundle data) {
        super(fm);
        fragmentBundle = data;
        tabTitles = fragmentBundle.getStringArray("Dates");
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {

        return DayFragment.newInstance(position+1, tabTitles);
    }
}