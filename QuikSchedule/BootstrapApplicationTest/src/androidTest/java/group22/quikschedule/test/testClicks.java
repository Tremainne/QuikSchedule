package group22.quikschedule.test;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;


@SuppressWarnings("rawtypes")
public class testClicks extends ActivityInstrumentationTestCase2 {
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
    public testClicks() throws ClassNotFoundException {
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
        //Take screenshot
        solo.takeScreenshot();
        //Sleep for 13361 milliseconds
		solo.sleep(13361);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Sleep for 1439 milliseconds
		solo.sleep(1439);
        //Click on Maps
		solo.clickInRecyclerView(2, 0);
        //Sleep for 6506 milliseconds
		solo.sleep(6506);
        //Click on ImageView
		solo.clickOnView(solo.getView(0x2));
        //Sleep for 5609 milliseconds
		solo.sleep(5609);
        //Press menu back key
		solo.goBack();
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Sleep for 3483 milliseconds
		solo.sleep(3483);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Sleep for 2520 milliseconds
		solo.sleep(2520);
        //Click on Schedule
		solo.clickInRecyclerView(1, 0);
        //Sleep for 1679 milliseconds
		solo.sleep(1679);
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Sleep for 1350 milliseconds
		solo.sleep(1350);
        //Click on Friends
		solo.clickInRecyclerView(3, 0);
	}
}
