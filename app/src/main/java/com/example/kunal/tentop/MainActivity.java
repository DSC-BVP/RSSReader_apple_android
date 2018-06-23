package com.example.kunal.tentop;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView ListApps;
    private String feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit=10;
    private String feedCachedUrl="INVALIDATED";
    public static final String STATE_URL = "feedURL";
    public static final String STATE_LIMIT="feeLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListApps= findViewById(R.id.xmlListView);
        if(savedInstanceState!=null){
            feedURL=savedInstanceState.getString(STATE_URL);
            feedLimit=savedInstanceState.getInt(STATE_LIMIT);
        }
        downloadURL(String.format(feedURL,feedLimit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_menu,menu);
        if (feedLimit == 25) {
            menu.findItem(R.id.topTF).setChecked(true);
        }else{
            menu.findItem(R.id.topTen).setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.refresh:
                feedCachedUrl="INVALIDATED";
                break;
            case R.id.menuPaid:
                feedURL = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                Log.d(TAG, "onOptionsItemSelected: "+ feedURL);
                break;
            case R.id.menuFree:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menu_songs:
                feedURL="http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.topTen:
            case R.id.topTF:
                if(!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " changing limit to " + feedLimit + ".");
                } else{
                    Log.d(TAG, "onOptionsItemSelected: "+ item.getTitle() + " feed limit unchanged.");
                }
                break;

            default:
                return  super.onOptionsItemSelected(item);
        }
        Log.d(TAG, "onOptionsItemSelected:" + feedURL);
        downloadURL(String.format(feedURL,feedLimit));
        return true;
    }

    private void downloadURL(String feedUrl){
        if(!feedUrl.equalsIgnoreCase(feedCachedUrl)) {
            Log.d(TAG, "onCreate: Starting Async Task");
            DownloadActivity downloadActivity = new DownloadActivity();
            Log.d(TAG, "downloadURL: downloading " + feedUrl);
            downloadActivity.execute(feedUrl);
            feedCachedUrl=feedUrl;
            Log.d(TAG, "OnCreate: Async task completed");
        }else{
            Log.d(TAG, "downloadURL: URL didn't change.");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL,feedURL);
        outState.putInt(STATE_LIMIT,feedLimit);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class DownloadActivity extends AsyncTask<String,Void,String> {
        private static final String TAG = "DownloadActivity";
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseApplication parseApplication = new ParseApplication();
            parseApplication.parse(s);
            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this,R.layout.list_record,parseApplication.getApplications());
            ListApps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            String RSSfeed = DownloadXML(strings[0]);
            if (RSSfeed==null){
                Log.e(TAG, "doInBackground: Error Downloading" );
            }
            return RSSfeed;
        }

        private String DownloadXML (String urlPath){
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                int response = (int)httpURLConnection.getResponseCode();
                Log.d(TAG, "DownloadXML: The response was: "+ response);
//                InputStream inputStream = httpURLConnection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while(true){
                    charsRead=bufferedReader.read(inputBuffer);
                    if(charsRead<=0){
                        break;
                    }
                    if(charsRead>0){
                        xmlResult.append(String.copyValueOf(inputBuffer,0,charsRead));
                    }
                }bufferedReader.close();

                return xmlResult.toString();

            } catch (MalformedURLException e){
                Log.e(TAG, "DownloadXML: wrong url passed: "+e.getMessage());
            } catch (IOException e){
                Log.e(TAG, "DownloadXML: IO exception reading data: "+e.getMessage());
            } return null;

        }
    }
}
