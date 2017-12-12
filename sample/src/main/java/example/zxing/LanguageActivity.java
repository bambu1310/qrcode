package example.zxing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by BAMBU on 10/5/2017.
 */

public class LanguageActivity extends Activity {

    private View view;
    //private TextView textView;
    public SQLiteDatabase settingDB;
    public SettingDBHelper settingDBHelper;
    public String language;

    private ProgressDialog progress;

    private ListView listView;
    public View itemView;
    public Integer pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language);

        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();
        language = settingDBHelper.getLanguage(settingDB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        if (language.equals("vi")) {
            toolbar.setTitle(R.string.title_language);
        } else {
            pos = 1;
            toolbar.setTitle(R.string.title_language_en);
        }

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

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);

        // Defined Array values to show in ListView
        String[] values = new String[] {
                "Tiếng Việt",
                "English"
        };

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);

                if(position == pos)
                {
                    // do something change color
                    row.setBackgroundColor (getResources().getColor(R.color.white)); // some color
                    ((TextView) row).setTextColor(Color.WHITE);
                }
                else
                {
                    // default state
                    row.setBackgroundColor (Color.TRANSPARENT); // default color
                    ((TextView) row).setTextColor(Color.BLACK);
                }
                return row;
            }
        };

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        //listView.setItemChecked(0, true);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                for (int i = 0; i < listView.getChildCount(); i++) {
                    View item = listView.getChildAt(i);
                    item.setBackgroundColor(Color.TRANSPARENT);
                    ((TextView) item).setTextColor(Color.BLACK);
                }

                view.setBackgroundColor(getResources().getColor(R.color.white));
                ((TextView) view).setTextColor(Color.WHITE);

                // Show Alert


                String val = "vi";
                String info = getResources().getString(R.string.msg_update_language);

                if (position == 1) {
                    val = "en";
                    info = getResources().getString(R.string.msg_update_language_en);
                }

                Toast.makeText(getApplicationContext(),
                        info, Toast.LENGTH_LONG)
                        .show();

                try {
                    ContentValues cv = new ContentValues();
                    cv.put("value", val);

                    settingDB.update("setting", cv, "key=\"language\"", null);

                    more(view);
                }catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }

        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:
                    scanToolbar(view);
                    return true;

                case R.id.navigation_history:
                    history(view);
                    return true;

                case R.id.navigation_favorite:
                    favorite(view);
                    return true;

                case R.id.navigation_more:
                    more(view);
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

}

