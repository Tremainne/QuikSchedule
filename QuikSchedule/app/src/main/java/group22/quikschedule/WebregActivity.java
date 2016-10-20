package group22.quikschedule;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebregActivity extends AppCompatActivity{

    private WebView webview;
    private String[] page;
    private String[] lines;
    private boolean capNext = false;

    private static Pattern classPattern = Pattern.compile("\\s+(\\w.* \\- \\w+)");
    private static Pattern dayPattern = Pattern.compile(">(.*)<");
    private static Pattern locPattern = Pattern.compile(">(.*)<");
    private static Pattern timePattern = Pattern.compile("\\s+(\\w.*)<");

    class JSInterface{
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String html) throws IOException{

            page = html.split("<!-- start of classes -->");
            if(page.length == 1){
                return;
            }
            html = page[1];
            page = html.split("<!-- end of classes -->");
            html = page[0];

            lines = html.split("\n");

            for(String line : lines){
                if(line.contains("<h4>")){
                    //New day
                    Matcher matcher = dayPattern.matcher(line);
                    matcher.find();

                    System.err.println(matcher.group(1));
                }
                else if(line.contains("<br")){
                    //New time
                    Matcher matcher = timePattern.matcher(line);
                    matcher.find();

                    System.err.println(matcher.group(1));
                }
                else if(line.contains("smallprint")){
                    //Class on next line
                    capNext = true;
                }
                else if(capNext){
                    //New class
                    capNext = false;
                    Matcher matcher = classPattern.matcher(line);
                    matcher.find();

                    System.err.println(matcher.group(1));
                }
                else if(line.contains("<a href=\"http://")){
                    Matcher matcher = locPattern.matcher(line);
                    matcher.find();

                    System.err.println(matcher.group(1));
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webreg);

        webview = (WebView) findViewById(R.id.activity_webreg_main);
        webview.addJavascriptInterface(new JSInterface(), "htmlout");
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webview, String url){
                webview.loadUrl("javascript:window.htmlout.processHTML('<head>'+" +
                        "document.getElementsByTagName('html')[0].innerHTML+'</head>');");
            }
        });

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(getString(R.string.ua));

        webview.loadUrl("https://students.ucsd.edu/");
        CookieManager.getInstance().setAcceptCookie(true);
    }

}