package group22.quikschedule.Settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.CallbackManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import group22.quikschedule.MainActivity;
import group22.quikschedule.R;

public class WebregActivity extends AppCompatActivity{

    private int count = 0;
    private int textbookCount = 0;
    private boolean capNext = false;
    private String android_id;
    private String[] page;
    private String[] lines;

    private DatabaseReference db;
    private WebView webview;

    private String day;
    private String classNum = "class_";

    private static Pattern classPattern = Pattern.compile("\\s+(\\w.* \\- \\w+)");
    private static Pattern dayPattern = Pattern.compile(">(.*)<");
    private static Pattern locPattern = Pattern.compile(">(.*)<");
    private static Pattern splitPattern = Pattern.compile("(.*) \\- (.*)");
    private static Pattern timePattern = Pattern.compile("\\s+(\\w.*)<");
    private static Pattern sectionPattern = Pattern.compile("\\((\\w+)\\) (\\d+), Section: (\\d+)");
    private static Pattern authorPattern = Pattern.compile(">(\\w+)</font>");
    private static Pattern bookPattern = Pattern.compile("-1\">(.*),");

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = MainActivity.class.getSimpleName();

    class JSInterface{
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) throws IOException{

            boolean parseWebreg = false;
            page = html.split("<!-- start of classes -->");
            if(page.length == 1){
                page = html.split( "//End -->" );
                if( page.length == 1 ) {
                    return;
                }
            }
            else {
                parseWebreg = true;
            }

            db = FirebaseDatabase.getInstance().getReference();
            /*
            String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid(); //Unique User ID

            if (!uuid.isEmpty()) {
                db.child(android_id).child("uuid").setValue(uuid);
            }
            */

            if( parseWebreg ) {
                html = page[1];
                page = html.split("<!-- end of classes -->");
                html = page[0];
                lines = html.split("\n");

                for (String line : lines) {
                    if (line.contains("<h4>")) { //New day
                        Matcher matcher = dayPattern.matcher(line);
                        matcher.find();

                        day = matcher.group(1);
                    } else if (line.contains("<br")) { //New time
                        Matcher matcher = timePattern.matcher(line);
                        matcher.find();

                        Matcher splitter = splitPattern.matcher(matcher.group(1));
                        splitter.find();

                        db.child(android_id).child(classNum + count + "").child("day").setValue(day);
                        db.child(android_id).child(classNum + count + "").child("startTime")
                                .setValue(splitter.group(1));
                        db.child(android_id).child(classNum + count + "").child("endTime")
                                .setValue(splitter.group(2));
                    } else if (line.contains("smallprint")) { //Class on next line
                        capNext = true;
                    } else if (capNext) {
                        //New class
                        capNext = false;
                        Matcher matcher = classPattern.matcher(line);
                        matcher.find();

                        Matcher splitter = splitPattern.matcher(matcher.group(1));
                        splitter.find();

                        db.child(android_id).child(classNum + count + "").child("class")
                                .setValue(splitter.group(1));
                        db.child(android_id).child(classNum + count + "").child("classType")
                                .setValue(splitter.group(2));
                    } else if (line.contains("<a href=\"http://")) {
                        Matcher matcher = locPattern.matcher(line);
                        matcher.find();

                        db.child(android_id).child(classNum + count + "").child("location")
                                .setValue(matcher.group(1));
                        ++count;
                    }
                }
            }
            else {
                html = page[2];

                lines = html.split("\n");

                String currAuthor = "Not null";
                String currClassName = "Not null";
                String currSection = "Not null";
                boolean getBook = false;

                for (String line : lines) {
                    if( line.contains( "Section" ) ) {
                        Matcher matcher = sectionPattern.matcher(line);
                        matcher.find();
                        currClassName = matcher.group(1) + " " + matcher.group(2);
                        currSection = matcher.group(3);
                    }
                    else if( getBook ) {
                        Matcher matcher = bookPattern.matcher( line );
                        matcher.find();

                        final String className = currClassName;
                        final String section = currSection;
                        final String author = currAuthor;
                        final String textbook = matcher.group(1);
                        final int textCount = textbookCount;
                        db.addValueEventListener( new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String currClassName;
                                for( int i = 0; i < count; ++i ) {
                                    currClassName = (String) dataSnapshot.child(android_id).child("class_" + i)
                                            .child("class").getValue();
                                    if( className.equals( currClassName ) ) {
                                        db.child(android_id).child("class_" + i).child("section")
                                                .setValue(section);
                                        db.child(android_id).child("class_" + i).child("author_" + textCount)
                                                .setValue(author);
                                        db.child(android_id).child("class_" + i).child("textbook_" + textCount)
                                                .setValue(textbook);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) { }
                        });
                        ++textbookCount;
                        getBook = false;
                    }
                    else if( line.contains( "<td><font face=\"tahoma\"" ) && line.contains( "</td>" ) ) {
                        Matcher matcher = authorPattern.matcher( line );
                        matcher.find();
                        currAuthor = matcher.group(1);
                        getBook = true;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webreg);

        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID); //Device ID

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        webview = (WebView) findViewById(R.id.activity_webreg_main);
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface(new JSInterface(), "htmlout");
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView webview, String url){ //Injecting Javascript for HTML
                webview.loadUrl("javascript:window.htmlout.processHTML('<head>'+" +
                        "document.getElementsByTagName('html')[0].innerHTML+'</head>');");

                String webUrl = webview.getUrl();
                if( webUrl.equals( "https://act.ucsd.edu/myTritonlink20/mobile.htm") ) {
                    AlertDialog alertDialog = new AlertDialog.Builder(WebregActivity.this).create();
                    alertDialog.setTitle("You're classes have been added successfully!");
                    alertDialog.setMessage("Would you like to add your textbooks as well?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes please",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    webview.loadUrl( "https://act.ucsd.edu/myTritonlink20/bookstorelink.htm?term=FA16" );
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No thank you",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if( webUrl.equals( getString(R.string.webregLogin) ) ) {
                    AlertDialog alertDialog = new AlertDialog.Builder(WebregActivity.this).create();
                    alertDialog.setTitle("Log into myTritonlink");
                    alertDialog.setMessage("Please log into myTrtionlink to sync classes. No "
                        + "personal information will be collected besides your class schedule.");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else if( webUrl.contains( "https://ucsdbkst.ucsd.edu/wrtx/FullBookList?term=FA16" ) ) {
                    AlertDialog alertDialog = new AlertDialog.Builder(WebregActivity.this).create();
                    alertDialog.setTitle("Books added successfully!");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    setContentView(R.layout.activity_login);
                                }
                            });
                    alertDialog.show();
                }
            }
        });

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(getString(R.string.ua)); //Spoofing UA for data retrieval

        webview.loadUrl(getString(R.string.webregLogin));
        CookieManager.getInstance().setAcceptCookie(true);
    }

}