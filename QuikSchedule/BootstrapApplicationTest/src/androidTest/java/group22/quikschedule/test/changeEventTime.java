package group22.quikschedule.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class changeEventTime extends ActivityInstrumentationTestCase2 {
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
    public changeEventTime() throws ClassNotFoundException {
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
        //Set default small timeout to 301699 milliseconds
		Timeout.setSmallTimeout(301699);
        //Click on Sign in with Google
		solo.clickOnText(java.util.regex.Pattern.quote("Sign in with Google"), 2);
        //Wait for activity: 'com.google.android.gms.auth.api.signin.internal.SignInHubActivity'
		assertTrue("SignInHubActivity is not found!", solo.waitForActivity("SignInHubActivity"));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on Yes
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Wait for activity: 'group22.quikschedule.Settings.WebregActivity'
		assertTrue("WebregActivity is not found!", solo.waitForActivity("WebregActivity"));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on My TritonLink
		solo.clickOnWebElement(By.textContent("My TritonLink"));
        //Click on Yes please
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on 28
		solo.clickOnText(java.util.regex.Pattern.quote("28"), 2);
        //Click on  CSE 105 - Lecture  9:00 AM-9:50 AM  CENTR 109
		solo.clickOnText(java.util.regex.Pattern.quote(" CSE 105 - Lecture\n 9:00 AM-9:50 AM\n CENTR 109"), 4);
        //Wait for activity: 'group22.quikschedule.Calendar.ExpandedEventActivity'
		assertTrue("ExpandedEventActivity is not found!", solo.waitForActivity("ExpandedEventActivity"));
        //Click on 9:00 AM
		solo.clickOnView(solo.getView("startTimePicker"));
        //Assert that: 'ImageView' is shown
		assertTrue("'ImageView' is not shown!", solo.waitForView(solo.getView("TimeImage")));
        //Click on 9:00 AM
		solo.clickOnView(solo.getView("startTimePicker"));
        //Click on Cancel
		solo.clickOnView(solo.getView(android.R.id.button2));
        //Click on 9:00 AM
		solo.clickOnView(solo.getView("startTimePicker"));
        //Click on 55
		solo.clickOnView(solo.getView("minutes"));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on Done
		solo.clickOnView(solo.getView("edit"));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on  CSE 105 - Lecture  8:55 AM-9:50 AM  CENTR 109
		solo.clickOnText(java.util.regex.Pattern.quote(" CSE 105 - Lecture\n 8:55 AM-9:50 AM\n CENTR 109"), 12);
        //Wait for activity: 'group22.quikschedule.Calendar.ExpandedEventActivity'
		assertTrue("ExpandedEventActivity is not found!", solo.waitForActivity("ExpandedEventActivity"));
        //Click on Done
		solo.clickOnView(solo.getView("edit", 1));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
	}
}
