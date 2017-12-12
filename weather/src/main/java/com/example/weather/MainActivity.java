package com.example.weather;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public ProgressDialog progress;
    public String jsonStr;
    public View view;
    public HttpURLConnection urlConnection;

    public ImageView imgToday;
    public TextView txtToday;
    public TextView tempToday;

    public ImageView imgDay1;
    public TextView dateDay1;
    public TextView txtDay1;
    public TextView tempDay1;

    public ImageView imgDay2;
    public TextView dateDay2;
    public TextView txtDay2;
    public TextView tempDay2;

    public ImageView imgDay3;
    public TextView dateDay3;
    public TextView txtDay3;
    public TextView tempDay3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.today);
        setSupportActionBar(toolbar);

        imgToday = (ImageView) findViewById(R.id.img_today);
        txtToday = (TextView) findViewById(R.id.txt_today);
        tempToday = (TextView) findViewById(R.id.temp_today);

        imgDay1 = (ImageView) findViewById(R.id.img_day1);
        dateDay1 = (TextView) findViewById(R.id.date_day1);
        txtDay1 = (TextView) findViewById(R.id.txt_day1);
        tempDay1 = (TextView) findViewById(R.id.temp_day1);

        imgDay2 = (ImageView) findViewById(R.id.img_day2);
        dateDay2 = (TextView) findViewById(R.id.date_day2);
        txtDay2 = (TextView) findViewById(R.id.txt_day2);
        tempDay2 = (TextView) findViewById(R.id.temp_day2);

        imgDay3 = (ImageView) findViewById(R.id.img_day3);
        dateDay3 = (TextView) findViewById(R.id.date_day3);
        txtDay3 = (TextView) findViewById(R.id.txt_day3);
        tempDay3 = (TextView) findViewById(R.id.temp_day3);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //if (isNetworkConnected()) {
            sendGetRequest(view);
        //}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Check network
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    public void sendGetRequest(View View) {
        new GetClass(this).execute();
    }

    private class GetClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public GetClass(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
            progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            final StringBuilder result = new StringBuilder();

            try {
                URL url = new URL("http://qr.dico.vn/api/get-weather-data/");
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                StringBuilder output = new StringBuilder("");
                output.append(result.toString());
                jsonStr = output.toString();
                final JSONObject obj = new JSONObject(jsonStr);

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            JSONArray jsonArray = obj.getJSONArray("data");
                            JSONObject todayObject = jsonArray.getJSONObject(0);
                            String icon = "w_";

                            if (todayObject.getString("icon").length() == 1) {
                                icon += "0";
                            }

                            icon += todayObject.getString("icon");
                            int resID = getResources().getIdentifier(icon, "drawable", getPackageName());

                            imgToday.setImageResource(resID);
                            txtToday.setText(todayObject.getString("text").toString());
                            tempToday.setText(todayObject.getString("temp").toString());

                            for (int i = 1; i < jsonArray.length(); i++) {
                                JSONObject dayObject = jsonArray.getJSONObject(i);
                                icon = "w_" ;

                                if (dayObject.getString("icon").length() == 1) {
                                    icon += "0";
                                }

                                icon += dayObject.getString("icon");
                                resID = getResources().getIdentifier(icon, "drawable", getPackageName());

                                if (i == 1) {
                                    imgDay1.setImageResource(resID);
                                    dateDay1.setText(dayObject.getString("date").toString());
                                    txtDay1.setText(dayObject.getString("text").toString());
                                    tempDay1.setText(dayObject.getString("temp").toString());
                                } else if (i == 2) {
                                    imgDay2.setImageResource(resID);
                                    dateDay2.setText(dayObject.getString("date").toString());
                                    txtDay2.setText(dayObject.getString("text").toString());
                                    tempDay2.setText(dayObject.getString("temp").toString());
                                } else if (i == 3) {
                                    imgDay3.setImageResource(resID);
                                    dateDay3.setText(dayObject.getString("date").toString());
                                    txtDay3.setText(dayObject.getString("text").toString());
                                    tempDay3.setText(dayObject.getString("temp").toString());
                                }
                            }

                            //Toast.makeText(getApplicationContext(), a.toString(), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        progress.dismiss();
                    }
                });

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }

            return null;
        }
    }
}
