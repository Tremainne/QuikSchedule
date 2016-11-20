package group22.quikschedule.test;

import com.robotium.solo.*;
import android.test.ActivityInstrumentationTestCase2;


@SuppressWarnings("rawtypes")
public class TestLoadingWebreg extends ActivityInstrumentationTestCase2 {
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
    public TestLoadingWebreg() throws ClassNotFoundException {
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
        //Set default small timeout to 12968 milliseconds
		Timeout.setSmallTimeout(12968);
        //Click on Sign in with Google
		solo.clickOnText(java.util.regex.Pattern.quote("Sign in with Google"));
        //Wait for activity: 'com.google.android.gms.auth.api.signin.internal.SignInHubActivity'
		assertTrue("SignInHubActivity is not found!", solo.waitForActivity("SignInHubActivity"));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on ImageView
		solo.clickOnView(solo.getView(android.widget.ImageButton.class, 0));
        //Click on Settings
		solo.clickInRecyclerView(6, 0);
        //Click on Go to Webreg Activity
		solo.clickOnView(solo.getView("toWebreg"));
        //Wait for activity: 'group22.quikschedule.Settings.WebregActivity'
		assertTrue("WebregActivity is not found!", solo.waitForActivity("WebregActivity"));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on My TritonLink
		solo.clickOnWebElement(By.textContent("My TritonLink"));
        //Set default small timeout to 18613 milliseconds
		Timeout.setSmallTimeout(18613);
        //Click on Sign on
		solo.clickOnWebElement(By.textContent("Sign on"));
        //Click on Sign on
		solo.clickOnWebElement(By.textContent("Sign on"));
        //Click on Yes please
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Click on OK
		solo.clickOnView(solo.getView(android.R.id.button1));
        //Wait for activity: 'group22.quikschedule.NavigationDrawerActivity'
		assertTrue("NavigationDrawerActivity is not found!", solo.waitForActivity("NavigationDrawerActivity"));
        //Click on 17
		solo.clickOnText(java.util.regex.Pattern.quote("17"));
	}
}
