package group22.quikschedule.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import group22.quikschedule.R;

public class DayFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String ARG_DATES = "ARG_DATES";

    private String[] dates;
    private int mPage;

    public static DayFragment newInstance(int page, String[] tabTitles) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putStringArray(ARG_DATES, tabTitles);

        DayFragment fragment = new DayFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        dates = getArguments().getStringArray(ARG_DATES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_day, container, false);
        TextView date = (TextView) view.findViewById(R.id.date);

        date.setText(dates[mPage-1]);
        return view;
    }
}