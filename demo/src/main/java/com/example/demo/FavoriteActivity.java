package com.example.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.zxing.integration.android.IntentIntegrator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sample Activity extending from ActionBarActivity to display a Toolbar.
 */
public class FavoriteActivity extends AppCompatActivity {

    private View view;
    //private TextView textView;
    public SQLiteDatabase settingDB, favoriteDB;
    public SettingDBHelper settingDBHelper;
    public FavoriteDBHelper favoriteDBHelper;
    public String language;
    public Helper help;

    private ProgressDialog progress;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite);

        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();
        favoriteDBHelper = new FavoriteDBHelper(this);
        favoriteDB = favoriteDBHelper.getWritableDatabase();
        help = new Helper();

        language = settingDBHelper.getLanguage(settingDB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        if (language.equals("vi")) {
            toolbar.setTitle(R.string.title_favorite);
        } else {
            toolbar.setTitle(R.string.title_favorite_en);
        }
        /*setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.getMenu().getItem(2).setChecked(true);

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

        //textView = (TextView) findViewById(R.id.textView);
        listView = (ListView) findViewById(R.id.list);

        SqlQuery("SELECT * FROM " + "favorite ORDER BY _id DESC");
    }

    public void SqlQuery(String sql) {

        String[] colNames;
        String str = "";
        ArrayList<String> codeList = new ArrayList<String>();

        Cursor c = favoriteDB.rawQuery(sql, null);
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
                detailFavorite(code);
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
                    //favorite(view);
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

    public void detailFavorite(String str) {
        // Do something in response to button
        Intent intent = new Intent(this, DetailActivity.class);

        ArrayList<String> put = new ArrayList<String>();
        put.add(str);
        put.add("favorite");

        intent.putStringArrayListExtra(help.EXTRA_CODE, put);

        startActivity(intent);
    }
}