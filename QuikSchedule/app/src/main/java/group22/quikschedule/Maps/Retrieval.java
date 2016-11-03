package group22.quikschedule.Maps;

import android.os.AsyncTask;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Ty Dewes on 10/31/16.
 */

class Retrieval extends AsyncTask<String, Void, String> {
    private Exception exception;

    public String doInBackground(String... urls) {
        String request = urls[0];
        HttpsURLConnection con = null;
        System.err.println( request );
        try {
            URL url = new URL(request);
            con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod( "GET" );
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder result = new StringBuilder();
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            line = rd.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while( line != null ) {
            try {
                line = rd.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            result.append(line);
        }
        try {
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public interface AsyncResponse {
        void processFinish( String result );
    }

    public AsyncResponse delegate = null;

    public Retrieval( AsyncResponse delegate ) {
        this.delegate = delegate;
    }

    @Override
    protected void onPostExecute( String result ) {
        delegate.processFinish( result );
    }
}