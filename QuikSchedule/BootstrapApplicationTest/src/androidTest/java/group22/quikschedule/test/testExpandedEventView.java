package group22.quikschedule.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class testExpandedEventView extends ActivityInstrumentationTestCase2 {
  	private Solo solo;
  	
  	private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "group22.quikschedule.InitialActivity";

    private static Class<?> launcherActivityClass;
    static{
        try {
            launcherActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
        } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
        }
    }
  	
  	@SuppressWarnings("unchecked")
    public testExpandedEventView() throws ClassNotFoundException {
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
        //Wait for activity: 'group22.quikschedule.InitialActivity'
		solo.waitForActivity("InitialActivity", 2000);
        //Click on Sign in with Google
		solo.clickOnText(java.util.regex.Pattern.quote("Sign in with Google"));
        //Wait for activity: 'com.google.android.gms.auth.api.signin.internal.SignInHubActivity'
		assertTrue("SignInHubActivity is not found!", solo.waitForActivity("SignInHubActivity"));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on ImageView
		solo.clickOnView(solo.getView("addButton"));
        //Wait for activity: 'group22.quikschedule.Calendar.ExpandedEventActivity'
		assertTrue("ExpandedEventActivity is not found!", solo.waitForActivity("ExpandedEventActivity"));
        //Enter the text: 'Test2'
		solo.clearEditText((android.widget.EditText) solo.getView("eventName"));
		solo.enterText((android.widget.EditText) solo.getView("eventName"), "Test2");
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
        //Click on AM
		solo.clickOnView(solo.getView("am_label"));
        //Click on PM
		solo.clickOnView(solo.getView("pm_label"));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on Empty Text View
		solo.clickOnView(solo.getView("location"));
        //Enter the text: 'WLH'
		solo.clearEditText((android.widget.EditText) solo.getView("location"));
		solo.enterText((android.widget.EditText) solo.getView("location"), "WLH");
        //Click on Done
		solo.clickOnView(solo.getView("edit"));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Set default small timeout to 14161 milliseconds
		Timeout.setSmallTimeout(14161);
        //Click on  Test2  6:35 PM-7:00 PM  WLH
		solo.clickOnText(java.util.regex.Pattern.quote(" Test2\n 6:35 PM-7:00 PM\n WLH"));
        //Wait for activity: 'group22.quikschedule.Calendar.ExpandedEventActivity'
		assertTrue("ExpandedEventActivity is not found!", solo.waitForActivity("ExpandedEventActivity"));
	}
}
