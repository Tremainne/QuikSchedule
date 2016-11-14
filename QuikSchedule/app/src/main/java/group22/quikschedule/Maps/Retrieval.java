package group22.quikschedule.Maps;

import android.os.AsyncTask;
import android.util.Log;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Class: Retrieval
 *
 * Bugs: None known
 * Version: 1.0
 * Date: 10/24/16
 *
 * Description: Class that extends the AsyncTask interface in order to call Google Directions API
 * URL to grab the JSON object returned by the call for directions information.
 *
 * @author Tynan Dewes
 */
class Retrieval extends AsyncTask<String, Void, String> {
    // Member variables
    private Exception exception;

    /**
     * Name: doInBackground
     * Description: Performs the bulk of the work by connecting to URL and going line by line
     * to create a singular string of the JSON object information.
     * @param urls - URLs to be requested to create string of JSON object
     */
    public String doInBackground(String... urls) {
        String request = urls[0];
        HttpsURLConnection con = null;
        Log.d("Retrieval", request );

        // Try HTTP GET request
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

        // Create String object from HTTP request using BufferedReader
        StringBuilder result = new StringBuilder();
        BufferedReader rd = null;
        try {
            int code = con.getResponseCode();
            if (code != 200) {
                throw new IOException("Response code was: " + code);
            }
            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String line = null;
        try {
            line = rd.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.append(line);
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
        Log.d("Request", result.toString());
        return result.toString();
    }

    /**
     * Interface: AsyncResponse
     *
     * Bugs: None known
     * Version: 1.0
     * Date: 10/24/16
     *
     * Description: Simple interface that is utilized to return String object back to the UI thread
     * activity.
     *
     * @author Tynan Dewes
     */
    public interface AsyncResponse {
        void processFinish( String result );
    }

    public AsyncResponse delegate = null;

    /**
     * Name: Retrieval
     * Description: Simple assignment for AsyncResponse delegate object
     * @param delegate - AsyncResponse object utilized to instantiated delegate for onPostExecute
     *                 method
     */
    public Retrieval( AsyncResponse delegate ) {
        this.delegate = delegate;
    }

    /**
     * Name: onPostExecute
     * Description: Simple method run on post execution of the doInBackground task that utilizes
     * the AsyncResponse interface above to return the string to the UI thread
     * @param result - Resulting string of JSON object used to return to UI thread
     */
    @Override
    protected void onPostExecute( String result ) {
        delegate.processFinish( result );
    }
}
