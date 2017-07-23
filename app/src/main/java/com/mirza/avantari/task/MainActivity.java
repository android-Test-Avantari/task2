package com.mirza.avantari.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements Runnable {

    TextView text;
    ListView list;
    DatabaseHandler dbHandler;
    Button btn1, btn2;
    int timer = 0;
    Thread thread;
    boolean counting = true;
    int exc_time = 0;
    boolean nextCount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView) findViewById(R.id.textView);
        list = (ListView) findViewById(R.id.list);
        btn1 = (Button) findViewById(R.id.button);
        btn2 = (Button) findViewById(R.id.button2);
        dbHandler = new DatabaseHandler(this);

        thread = new Thread(this);
        thread.start();
    }

    public void startUpload(View v) {

        //I truncate table if data exist because it takes time to check for unique constraints
        dbHandler.removeData();
        counting = true;
        timer = 0;

        //In each iteration all words of the current character will be retrieved from html file and will stored in table
        for (char i = 'a'; i <= 'z'; i++) {
            DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
            downloadFilesTask.execute("http://unreal3112.16mb.com/wb1913_" + i + ".html");
        }
    }

    public void retriveData(View v) {

        timer = 0;
        counting = false;

        List<String> retrievedData = dbHandler.getdData();

        ArrayAdapter adapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_list_item_1, retrievedData);
        list.setAdapter(adapter);
        text.setText("Time in seconds to retrieve all data: " + timer);
        counting = true;

    }


    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                if (!counting) {
                    //for calculating time required to do operation
                    timer++;
                    if (nextCount) {
                        if(exc_time==0){
                            exc_time=timer;
                        }else if(exc_time>timer){
                            exc_time=timer;
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }


    private class DownloadFilesTask extends AsyncTask<String, Void, String> {

        ProgressDialog load;

        @Override
        protected void onPreExecute() {
            load = new ProgressDialog(MainActivity.this);
            load.setMessage("loading...");
            load.show();
            load.setCancelable(false);

        }

        protected String doInBackground(String... urls) {
            URL mUrl = null;
            String content = "";
            try {
                mUrl = new URL(urls[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                assert mUrl != null;
                URLConnection connection = mUrl.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    content += line;
                }

                br.close();

                List<String> matchedWords = new ArrayList<String>();
                List<String> matchedSpeeches = new ArrayList<String>();
                List<String> matchedMean = new ArrayList<String>();

                //Patterns to match words, speeches , meaning of words from html file.

                Matcher wrd = Pattern.compile("(<P><B>)(.*?)(<\\/B>)").matcher(content);
                Matcher sp = Pattern.compile("(\\(<I>)(.*?)(<\\/I>\\))").matcher(content);
                Matcher mn = Pattern.compile("(<\\/I>\\))(.*?)(<\\/P>)").matcher(content);


                while (wrd.find() && sp.find() && mn.find()) {
                    matchedWords.add(wrd.group(2));
                    matchedSpeeches.add(sp.group(2));
                    matchedMean.add(mn.group(2));
                }

                String words[] = matchedWords.toArray(new String[0]);
                String speeches[] = matchedSpeeches.toArray(new String[0]);
                String meaning[] = matchedMean.toArray(new String[0]);
                counting = false;
                nextCount=true;
                dbHandler.addWords(words, speeches, meaning);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "success";
        }

        protected void onPostExecute(String result) {
            counting = true;
            nextCount=false;
            text.setText("Minimum time among all submission in seconds: " + exc_time);
            load.dismiss();
        }
    }
}


