package com.mirza.avantari.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements Runnable {

    TextView text;
    ListView list;
    DatabaseHandler dbHandler;
    Button btn1, btn2;
    boolean stop = false;
    int timer = 0;
    Thread thread;
    boolean counting = true;

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

        for (char i = 'a'; i <= 'b'; i++) {
            DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
            downloadFilesTask.execute("http://unreal3112.16mb.com/wb1913_" + i + ".html");
        }
    }

    public void retriveData(View v) {
        List<String> allMatches1 = dbHandler.getdData();
        ArrayAdapter adapter = new ArrayAdapter<String>(getBaseContext(),
                android.R.layout.simple_list_item_1, allMatches1);
        list.setAdapter(adapter);


    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!counting) {
                    Thread.sleep(1000);
                    timer++;
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

                List<String> allMatches1 = new ArrayList<String>();
                List<String> allMatches2 = new ArrayList<String>();
                List<String> allMatches3 = new ArrayList<String>();
                Matcher m = Pattern.compile("(<P><B>)(.*?)(<\\/B>)").matcher(content);
                Matcher n = Pattern.compile("(\\(<I>)(.*?)(<\\/I>\\))").matcher(content);
                Matcher o = Pattern.compile("(<\\/I>\\))(.*?)(<\\/P>)").matcher(content);


                while (m.find() && n.find() && o.find()) {
                    allMatches1.add(m.group(2));
                    allMatches2.add(n.group(2));
                    allMatches3.add(o.group(2));
                }

                String words[] = allMatches1.toArray(new String[0]);
                String speeches[] = allMatches2.toArray(new String[0]);
                String meaning[] = allMatches3.toArray(new String[0]);
                counting = false;
                dbHandler.addWords(words, speeches, meaning);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return content;
        }

        protected void onPostExecute(String result) {
            if (!TextUtils.isEmpty(result)) {
                counting = true;
                text.setText("Time in seconds: " + timer);

//                List<String> allMatches1 = new ArrayList<String>();
//                Matcher m = Pattern.compile("(<P><B>)(.*?)(<\\/B>)").matcher(result);
//                while (m.find()) {
//                    allMatches1.add(m.group(2));
//                    ArrayAdapter adapter = new ArrayAdapter<String>(getBaseContext(),
//                            android.R.layout.simple_list_item_1, allMatches1);
//                    list.setAdapter(adapter);
                load.dismiss();
            }
        }
    }
}


