package example.zxing;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Sample Activity extending from ActionBarActivity to display a Toolbar.
 */
public class MoreDetailActivity extends AppCompatActivity {

    private View view;
    public SQLiteDatabase settingDB;
    public SettingDBHelper settingDBHelper;
    public String language;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();
        language = settingDBHelper.getLanguage(settingDB);

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

        textView = (TextView) findViewById(R.id.textView);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        ArrayList<String> content = intent.getStringArrayListExtra(MoreActivity.MORE_CONTENT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(content.get(1).toString(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(content.get(1).toString()));
        }

        String title = intent.getStringExtra(MoreActivity.TITLE);
        toolbar.setTitle(content.get(0));
        /*setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
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
