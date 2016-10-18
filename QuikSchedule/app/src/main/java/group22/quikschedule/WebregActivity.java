package group22.quikschedule;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebregActivity extends AppCompatActivity{

    private int count = 0;
    private boolean capNext = false;

    private String android_id;
    private String[] page;
    private String[] lines;

    private DatabaseReference db;
    private WebView webview;

    private static String day;
    private static String classNum = "class_";

    private static Pattern classPattern = Pattern.compile("\\s+(\\w.* \\- \\w+)");
    private static Pattern dayPattern = Pattern.compile(">(.*)<");
    private static Pattern locPattern = Pattern.compile(">(.*)<");
    private static Pattern splitPattern = Pattern.compile("(.*) \\- (.*)");
    private static Pattern timePattern = Pattern.compile("\\s+(\\w.*)<");

    class JSInterface{
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) throws IOException{

            page = html.split("<!-- start of classes -->");
            if(page.length == 1){return;}
            html = page[1];
            page = html.split("<!-- end of classes -->");
            html = page[0];
            lines = html.split("\n");

            db = FirebaseDatabase.getInstance().getReference();

            for(String line : lines){
                if(line.contains("<h4>")){ //New day
                    Matcher matcher = dayPattern.matcher(line);
                    matcher.find();
                    day = matcher.group(1);
                }
                else if(line.contains("<br")){ //New time
                    Matcher matcher = timePattern.matcher(line);
                    matcher.find();
                    Matcher splitter = splitPattern.matcher(matcher.group(1));
                    splitter.find();

                    db.child(android_id).child(classNum + count + "").child("day").setValue(day);
                    db.child(android_id).child(classNum + count + "").child("startTime")
                            .setValue(splitter.group(1));
                    db.child(android_id).child(classNum + count + "").child("endTime")
                            .setValue(splitter.group(2));
                }
                else if(line.contains("smallprint")){ //Class on next line in HTML
                    capNext = true;
                }
                else if(capNext){ //New class
                    capNext = false;
                    Matcher matcher = classPattern.matcher(line);
                    matcher.find();
                    Matcher splitter = splitPattern.matcher(matcher.group(1));
                    splitter.find();

                    db.child(android_id).child(classNum + count + "").child("class")
                            .setValue(splitter.group(1));
                    db.child(android_id).child(classNum + count + "").child("classType")
                            .setValue(splitter.group(2));
                }
                else if(line.contains("<a href=\"http://")){
                    Matcher matcher = locPattern.matcher(line);
                    matcher.find();

                    db.child(android_id).child(classNum + count + "").child("location")
                            .setValue(matcher.group(1));
                    ++count;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webreg);

        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID); //Unique ID

        webview = (WebView) findViewById(R.id.activity_webreg_main);
        webview.addJavascriptInterface(new JSInterface(), "htmlout");
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webview, String url){ //Injecting Javascript for HTML
                webview.loadUrl("javascript:window.htmlout.processHTML('<head>'+" +
                        "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        });

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(getString(R.string.ua)); //Spoofing UA for data retrieval

        webview.loadUrl(getString(R.string.webregLogin));
        CookieManager.getInstance().setAcceptCookie(true);
    }

}