package com.example.demo;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private View view;
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private SQLiteDatabase settingDB;
    private SettingDBHelper settingDBHelper;
    public Helper help;
    public TextView txt;
    private ProgressDialog progress;
    private String appName, privateKey, deviceId, osName, osVersion, tokenKey;
    private String jsonStr;

    public HttpURLConnection urlConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt = (TextView) findViewById(R.id.textView);

        init();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 3000ms
                scan(view);

            }
        }, 1500);

    }

    public void init() {
        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();
        help = new Helper();

        tokenKey = help.getTokenKey(settingDB);
        appName = help.getAppName();
        privateKey = help.getPrivateKey();
        deviceId = help.getDeviceId(getApplicationContext());
        osName = help.getOsName();
        osVersion = help.getOsVersion();

        if (tokenKey == null && help.isNetworkConnected(getApplicationContext())) {
            requestToken(view);
        }

        String sql = "SELECT value FROM " + "setting WHERE key=\"language\"";

        Cursor c = settingDB.rawQuery(sql, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            long id;
            ContentValues cv = new ContentValues();
            cv.put("key", "language");
            cv.put("value", "vi");
            id = settingDB.insert("setting", null, cv);
        }

        /*txt.setText(tokenKey + "\n" + appName + "\n" + privateKey + "\n" + deviceId + "\n" + osName + "\n" + osVersion);*/
    }

    public void requestToken(View View) {
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
                String uri = help.getUriApiRegister();
                String urlParameters = "ApplicationName=" + appName + "&ApplicationPrivateKey=" + privateKey + "&DeviceID=" + deviceId + "&OSName=" + osName + "&OSVersion=" + osVersion;
                final URL url = new URL(uri + urlParameters);

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
                            Integer responseCode = Integer.parseInt(obj.getString("ResponseCode").toString());

                            if (responseCode == 0) {
                                Toast.makeText(getApplicationContext(), obj.getString("ResponseMessage").toString() , Toast.LENGTH_LONG).show();
                                txt.setText(url.toString() + obj.getString("ResponseMessage").toString());
                            } else {
                                tokenKey = obj.getString("ApplicationTokenKey").toString();

                                String sql = "SELECT value FROM " + "setting WHERE key=\"token_key\"";

                                Cursor c = settingDB.rawQuery(sql, null);
                                c.moveToFirst();

                                if (c.getCount() == 0) {
                                    long id;
                                    ContentValues cv = new ContentValues();
                                    cv.put("key", "token_key");
                                    cv.put("value", tokenKey);
                                    id = settingDB.insert("setting", null, cv);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progress.dismiss();

                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public void scan(View view) {
        IntentIntegrator intent = new IntentIntegrator(this);
        intent.setCaptureActivity(ScanActivity.class);
        intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intent.initiateScan();
    }

    public void scanTest(View view) {
        IntentIntegrator intent = new IntentIntegrator(this);
        intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intent.initiateScan();
    }

    /** Called when the user taps the Send button */
    public void detail(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void history(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void favorite(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, FavoriteActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                scan(view);
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Sample of scanning from a Fragment
     */
    public static class ScanFragment extends Fragment {
        private String toast;

        public ScanFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            displayToast();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_scan, container, false);
            Button scan = (Button) view.findViewById(R.id.scan_from_fragment);
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanFromFragment();
                }
            });
            return view;
        }

        public void scanFromFragment() {
            IntentIntegrator.forSupportFragment(this).initiateScan();
        }

        private void displayToast() {
            if(getActivity() != null && toast != null) {
                Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
                toast = null;
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {
                    toast = "Cancelled from fragment";
                } else {
                    toast = "Scanned from fragment: " + result.getContents();
                }

                // At this point we may or may not have a reference to the activity
                displayToast();
            }
        }
    }
}