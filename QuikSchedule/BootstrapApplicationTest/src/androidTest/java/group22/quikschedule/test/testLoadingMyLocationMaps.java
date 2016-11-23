package group22.quikschedule.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class testLoadingMyLocationMaps extends ActivityInstrumentationTestCase2 {
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
    public testLoadingMyLocationMaps() throws ClassNotFoundException {
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
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Click on Maps
		solo.clickInRecyclerView(2, 0);
        //Click on ImageView
		solo.clickOnView(solo.getView(0x2));
	}
}
