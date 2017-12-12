package example.zxing;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.zxing.integration.android.IntentIntegrator;

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
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Sample Activity extending from ActionBarActivity to display a Toolbar.
 */
public class MoreActivity extends AppCompatActivity {
    public HttpURLConnection urlConnection;
    private ProgressDialog progress;
    private String jsonStr;
    private JSONObject reader;

    public View view_activity;
    public SQLiteDatabase settingDB, otherDB;
    public SettingDBHelper settingDBHelper;
    public OtherDBHelper otherDBHelper;
    public String language;

    private ListView listView;

    public static final String MORE_CONTENT = "com.example.myfirstapp.MESSAGE";
    public static final String TITLE = "com.example.myfirstapp.MESSAGE";

    public Helper help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more);

        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();

        otherDBHelper = new OtherDBHelper(this);
        otherDB = otherDBHelper.getWritableDatabase();

        language = settingDBHelper.getLanguage(settingDB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        if (language.equals("vi")) {
            toolbar.setTitle(R.string.title_more);
        } else {
            toolbar.setTitle(R.string.title_more_en);
        }
        /*setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.getMenu().getItem(3).setChecked(true);

        if (language.equals("en")) {
            MenuItem scanItem = bottomNavigationView.getMenu().findItem(R.id.navigation_scan);
            MenuItem hisItem = bottomNavigationView.getMenu().findItem(R.id.navigation_history);
            MenuItem favItem = bottomNavigationView.getMenu().findItem(R.id.navigation_favorite);
            MenuItem otherItem = bottomNavigationView.getMenu().findItem(R.id.navigation_more);
            scanItem.setTitle(R.string.title_scan_en);
            hisItem.setTitle(R.string.title_history_en);
            favItem.setTitle(R.string.title_favorite_en);
            otherItem.setTitle(R.string.title_more_en);
        }

        help = new Helper();

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);

        if (isNetworkConnected()) {
            String tokenKey = help.getTokenKey(settingDB);

            if (tokenKey == null) {
                main_init(view_activity);
            } else {
                sendGetRequest(view_activity);
            }
        } else {
            SqlQuery("SELECT * FROM " + "other WHERE language=\"" + language + "\" ORDER BY _id");
        }
    }

    public void SqlQuery(String sql) {

        String[] colNames;
        String str = "";
        ArrayList<String> codeList = new ArrayList<String>();
        final ArrayList<String> contentList = new ArrayList<String>();

        if (language.equals("vi")) {
            codeList.add(getResources().getString(R.string.title_language));
        } else {
            codeList.add(getResources().getString(R.string.title_language_en));
        }

        codeList.add("Logout");

        //contentList.add("language");

        /*Cursor c = otherDB.rawQuery(sql, null);
        c.moveToFirst();

        for(int i = 0; i < c.getCount(); i++) {
            codeList.add(c.getString(1));
            contentList.add(c.getString(2));
            c.moveToNext();
        }*/

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, codeList);


        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                String  content = "";

                if (itemPosition == 0) {
                    language();
                } else if (itemPosition == 1) {
                    main_init(view_activity);
                } else {
                    content = contentList.get(position);
                    moreDetail(content, itemValue);
                }
            }

        });
    }

    /**
     * Check network
     * @return
     */
    private boolean isNetworkConnected() {
        return false;
        /*ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();*/

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:
                    scanToolbar(view_activity);
                    return true;

                case R.id.navigation_history:
                    history(view_activity);
                    return true;

                case R.id.navigation_favorite:
                    favorite(view_activity);
                    return true;

                case R.id.navigation_more:
                    //more(view);
                    return true;
            }
            return false;
        }

    };

    public void scanToolbar(View view) {
        IntentIntegrator intent = new IntentIntegrator(this);
        intent.setCaptureActivity(ScanActivity.class);
        intent.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intent.initiateScan();
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

    /** Called when the user taps the Send button */
    public void more(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MoreActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void moreDetail(String content, String title) {
        // Do something in response to button
        Intent intent = new Intent(this, MoreDetailActivity.class);

        ArrayList<String> put = new ArrayList<String>();
        put.add(title);
        put.add(content);

        intent.putStringArrayListExtra(MORE_CONTENT, put);
        startActivity(intent);
    }

    public void language() {
        Intent intent = new Intent(this, LanguageActivity.class);
        startActivity(intent);
    }

    public void main_init(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
            try {
                String uri = help.getUriApiGetContent();
                String appName = help.getAppName();
                String tokenKey = help.getTokenKey(settingDB);
                String deviceId = help.getDeviceId(getApplicationContext());
                String phoneNo = help.getPhoneNumber(getApplicationContext(), ContextCompat.checkSelfPermission(MoreActivity.this, Manifest.permission.READ_PHONE_STATE));
                String latitude = help.getLatitude();
                String longitude = help.getLongitude();
                String urlParameters = "DeviceID=" + deviceId + "&PhoneNo=" + phoneNo + "&Latitude=" + latitude + "&Longitude=" + longitude + "&ApplicationName=" + appName + "&ApplicationTokenKey=" + tokenKey + "&Language=" + language;
                final URL url = new URL(uri + urlParameters);

                /*HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = connection.getResponseCode();

                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post parameters : " + urlParameters);
                System.out.println("Response Code : " + responseCode);

                final StringBuilder output = new StringBuilder("");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();

                while((line = br.readLine()) != null ) {
                    responseOutput.append(line);
                }
                br.close();

                output.append(responseOutput.toString());
                jsonStr = output.toString();

                reader = new JSONObject(jsonStr);*/

                final StringBuilder result = new StringBuilder();
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
                final JSONObject object = new JSONObject(jsonStr);

                MoreActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //txtView.setText(jsonStr);
                        ArrayList<String> codeList = new ArrayList<String>();

                        if (language.equals("vi")) {
                            codeList.add(getResources().getString(R.string.title_language));
                        } else {
                            codeList.add(getResources().getString(R.string.title_language_en));
                        }

                        try {
                            Integer responseCode = Integer.parseInt(object.getString("ResponseCode").toString());

                            if (responseCode == 1) {
                                JSONObject obj = object.getJSONObject("ObjectData");
                                otherDBHelper.clear(otherDB, language);

                                Iterator<String> iter = obj.keys();

                                while (iter.hasNext()) {
                                    String key = iter.next();

                                    try {
                                        JSONObject item = obj.getJSONObject(key);
                                        codeList.add(item.getString("Title").toString());

                                        long id;
                                        ContentValues cv = new ContentValues();
                                        cv.put("title", item.getString("Title").toString());
                                        cv.put("content", item.getString("Content").toString());
                                        cv.put("language", language);
                                        id = otherDB.insert("other", null, cv);

                                    } catch (JSONException e) {
                                        // Something went wrong!
                                    }
                                }
                            } else {
                                codeList.add(object.getString("ResponseMessage"));
                            }
                        } catch (JSONException e) {
                            //codeList.add(e.getMessage());
                            e.printStackTrace();
                        }


                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MoreActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, codeList);
                        // Assign adapter to ListView
                        listView.setAdapter(adapter);

                        progress.dismiss();

                        // ListView Item Click Listener
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {

                                // ListView Clicked item index
                                int itemPosition     = position;

                                // ListView Clicked item value
                                String  itemValue    = (String) listView.getItemAtPosition(position);
                                String  content = "";

                                if (itemPosition == 0) {
                                    language();
                                } else {
                                    try {
                                        Integer responseCode = Integer.parseInt(object.getString("ResponseCode").toString());

                                        JSONObject obj = object.getJSONObject("ObjectData");

                                        Iterator<String> iter = obj.keys();

                                        while (iter.hasNext()) {
                                            String key = iter.next();

                                            try {
                                                JSONObject item = obj.getJSONObject(key);
                                                if (item.getString("Title").toString().equals(itemValue)) {
                                                    content = item.getString("Content").toString();
                                                }
                                            } catch (JSONException e) {
                                                // Something went wrong!
                                            }
                                        }
                                    } catch (JSONException e) {
                                        //codeList.add(e.getMessage());
                                        e.printStackTrace();
                                    }

                                    moreDetail(content, itemValue);
                                }

                                // Show Alert
                                /*Toast.makeText(getApplicationContext(),
                                    "Position :"+itemPosition+"  ListItem : " +itemValue + "Content: " + content , Toast.LENGTH_LONG)
                                    .show();*/

                            }

                        });


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

}
