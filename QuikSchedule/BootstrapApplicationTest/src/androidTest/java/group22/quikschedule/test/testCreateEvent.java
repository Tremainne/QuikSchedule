package group22.quikschedule.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class testCreateEvent extends ActivityInstrumentationTestCase2 {
  	private Solo solo;
  	
  	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "group22.quikschedule.NavigationDrawerActivity";

    private static Class<?> launcherActivityClass;
    static{
        try {
            launcherActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
        } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
        }
    }
  	
  	@SuppressWarnings("unchecked")
    public testCreateEvent() throws ClassNotFoundException {
        super(LAUNCHER_ACTIVITY_FULL_CLASSNAME, launcherActivityClass);
    }

  	public void setUp() throws Exception {
        super.setUp();
		solo = new Solo(getInstrumentation());
		getActivity();
  	}
  
   	@Override
   	public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
  	}
  
	public void testRun() {
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		solo.waitForActivity("NavigationDrawerActivity", 2000);
        //Set default small timeout to 64592 milliseconds
		Timeout.setSmallTimeout(64592);
        //Click on ImageView
		solo.clickOnView(solo.getView("addButton"));
        //Wait for activity: 'group22.quikschedule.Calendar.ExpandedEventActivity'
		assertTrue("ExpandedEventActivity is not found!", solo.waitForActivity("ExpandedEventActivity"));
        //Enter the text: 'test'
		solo.clearEditText((android.widget.EditText) solo.getView("eventName"));
		solo.enterText((android.widget.EditText) solo.getView("eventName"), "test");
        //Click on Empty Text View
		solo.clickOnView(solo.getView("datePicker"));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("startTimePicker"));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("endTimePicker"));
        //Assert that: 'RadialTimePickerView' is shown
		assertTrue("'RadialTimePickerView' is not shown!", solo.waitForView(solo.getView("radial_picker")));
        //Assert that: 'RadialTimePickerView' is shown
		assertTrue("'RadialTimePickerView' is not shown!", solo.waitForView(solo.getView("radial_picker")));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("location"));
        //Enter the text: 'WLH'
		solo.clearEditText((android.widget.EditText) solo.getView("location"));
		solo.enterText((android.widget.EditText) solo.getView("location"), "WLH");
        //Click on Route
		solo.clickOnView(solo.getView("openMaps"));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on ImageView
		solo.clickOnView(solo.getView(0x2));
        //Press menu back key
		solo.goBack();
        //Click on Done
		solo.clickOnView(solo.getView("edit"));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Click on Settings
		solo.clickInRecyclerView(6, 0);


	}
}
