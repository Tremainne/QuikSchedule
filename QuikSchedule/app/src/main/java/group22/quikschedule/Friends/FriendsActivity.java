package group22.quikschedule.Friends;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import static java.security.AccessController.getContext;

/**
 * Class: FriendsActivity
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 11/5/16
 *
 * Description: Activity from which the FriendsFragment can extend so that super class methods may
 *              be overwritten in the fragment.
 *
 * @author Tynan Dewes
 * @author David Thomson
 */
public class FriendsActivity extends FragmentActivity {

   /* private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void toFacebook(View view) {

        Intent i;
        try {
            this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            i = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://"));
        } catch (Exception e) {
            i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"));
        }

        startActivity(i);

        PackageManager packageManager = getContext().getPackageManager();
        boolean messengerInstalled = isPackageInstalled("com.facebook.orca", packageManager);

        String FACEBOOK_URL = "https://www.facebook.com/";
        String url;

        if(messengerInstalled) {

        }
        else {

        }
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return "fb://";
            } else { //older versions of fb app
                    return "fb://";
                }
            } catch (PackageManager.NameNotFoundException e) {
                return FACEBOOK_URL; //normal web url
            }

        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        String facebookUrl = getFacebookPageURL(this);
        facebookIntent.setData(Uri.parse(facebookUrl));
        startActivity(facebookIntent);
    }*/
}
