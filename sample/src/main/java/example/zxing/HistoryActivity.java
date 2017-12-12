package example.zxing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sample Activity extending from ActionBarActivity to display a Toolbar.
 */
public class HistoryActivity extends AppCompatActivity {

    private View view;
    //private TextView textView;
    public SQLiteDatabase settingDB, historyDB;
    public SettingDBHelper settingDBHelper;
    public String language;
    public HistoryDBHelper historyDBHelper;
    public Helper help;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();
        help = new Helper();
        language = settingDBHelper.getLanguage(settingDB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        if (language.equals("vi")) {
            toolbar.setTitle(R.string.title_history);
        } else {
            toolbar.setTitle(R.string.title_history_en);
        }
        /*setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);

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

        historyDBHelper = new HistoryDBHelper(this);
        historyDB = historyDBHelper.getWritableDatabase();

        SqlQuery("SELECT * FROM " + "history ORDER BY _id DESC");
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

    /** Called when the user taps the Send button */
    public void detailHistory(String str) {
        // Do something in response to button
        Intent intent = new Intent(this, DetailActivity.class);

        ArrayList<String> put = new ArrayList<String>();
        put.add(str);
        put.add("history");

        intent.putStringArrayListExtra(help.EXTRA_CODE, put);

        startActivity(intent);
    }

    public void SqlQuery(String sql) {

        String[] colNames;
        String str = "";
        ArrayList<String> codeList = new ArrayList<String>();

        Cursor c = historyDB.rawQuery(sql, null);
        c.moveToFirst();

        if (c.getCount() == 0) {
            String noData = getResources().getString(R.string.msg_no_data);

            if (language.equals("en")) {
                noData = getResources().getString(R.string.msg_no_data_en);
            }

            codeList.add(noData);
        } else {
            for (int i = 0; i < c.getCount(); i++) {
                codeList.add(c.getString(1) + "\n" + c.getString(3));
                c.moveToNext();
            }
        }

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
                List<String> myList = new ArrayList<String>(Arrays.asList(itemValue.split("\n")));
                String code = myList.get(0);

                // Show Alert
                /*Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();*/
                detailHistory(code);

            }

        });
    }

    public void clear(View view) {
        historyDBHelper.clear(historyDB);
        //scanToolbar(view);
    }

}
