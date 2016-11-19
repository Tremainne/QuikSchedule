package group22.quikschedule.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class moreTests extends ActivityInstrumentationTestCase2 {
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
    public moreTests() throws ClassNotFoundException {
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
        //Sleep for 2518 milliseconds
		solo.sleep(2518);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Take screenshot
        solo.takeScreenshot();
        //Sleep for 2132 milliseconds
		solo.sleep(2132);
        //Click on Settings
		solo.clickInRecyclerView(6, 0);
        //Take screenshot
        solo.takeScreenshot();
        //Sleep for 1718 milliseconds
		solo.sleep(1718);
        //Click on Go to Webreg Activity
		solo.clickOnView(solo.getView("toWebreg"));
        //Take screenshot
        solo.takeScreenshot();
        //Wait for activity: 'group22.quikschedule.Settings.WebregActivity'
		assertTrue("WebregActivity is not found!", solo.waitForActivity("WebregActivity"));
        //Sleep for 7733 milliseconds
		solo.sleep(7733);
        //Press menu back key
		solo.goBack();
        //Sleep for 2371 milliseconds
		solo.sleep(2371);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Take screenshot
        solo.takeScreenshot();
        //Sleep for 1535 milliseconds
		solo.sleep(1535);
        //Click on Maps
		solo.clickInRecyclerView(2, 0);
        //Take screenshot
        solo.takeScreenshot();
        //Sleep for 2774 milliseconds
		solo.sleep(2774);
        //Click on ImageView
		solo.clickOnView(solo.getView(0x2));
        //Take screenshot
        solo.takeScreenshot();
	}
}
