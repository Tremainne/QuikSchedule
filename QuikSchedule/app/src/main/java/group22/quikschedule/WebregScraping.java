package group22.quikschedule;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

public class WebregScraping extends AsyncTask<String, Void, Document>{
    protected Document doInBackground(String... strings){

        String url = "http://mytritonlink.ucsd.edu/";
        //String url = Resources.getSystem().getString(R.string.webregLogin);
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36"
         + " (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36";
        String referrer = "https://a4.ucsd.edu/tritON/profile/SAML2/Redirect/SSO";

        String pid = strings[0];
        String password = strings[1];

        Document doc = null;

        try{
            Connection.Response res = Jsoup
                    .connect(url)
                    .data("ssousername", pid)
                    .data("ssopassword", password)
                    .method(Connection.Method.POST)
                    .followRedirects(true)
                    .execute();

            System.err.println(res.url());

            Map<String, String> loginCookies = res.cookies();

            System.err.println(loginCookies.toString());

            doc = Jsoup
                    .connect("https://act.ucsd.edu/myTritonlink20/display.htm")
                    .referrer(referrer)
                    .userAgent(userAgent)
                    .cookies(loginCookies)
                    .followRedirects(true)
                    .get();

            String title = doc.title();
            System.err.println("This is a title: " + title);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return doc;
    }


}
