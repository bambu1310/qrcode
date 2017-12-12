package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sample Activity extending from ActionBarActivity to display a Toolbar.
 */
public class ScanActivity extends Activity {
    private static final String TAG = ScanActivity.class.getSimpleName();
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private EditText txt;
    private Button scanBtn;
    private View view;

    public SQLiteDatabase settingDB;
    public SettingDBHelper settingDBHelper;
    public String language;
    public Helper help;

    public static final int RequestPermissionCode = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scan);

        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();
        help = new Helper();
        language = settingDBHelper.getLanguage(settingDB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        if (language.equals("vi")) {
            toolbar.setTitle(R.string.title_name);
        } else {
            toolbar.setTitle(R.string.title_name_en);
        }
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);

        if (language.equals("vi")) {
            barcodeView.setStatusText(getString(R.string.zxing_msg_default));
        }

        barcodeView.decodeContinuous(callback);

        //beepManager = new BeepManager(this);

        txt = (EditText) findViewById(R.id.txt);
        scanBtn = (Button) findViewById(R.id.scanBtn);
        //historyList = (ListView) findViewById(R.id.history_list);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);

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

        if(!CheckingPermissionIsEnabledOrNot())
        {
            //Calling method to enable permission.
            RequestMultiplePermission();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }

        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            //barcodeView.setStatusText(result.getText());
            String s = result.getText();
            List<String> myList = new ArrayList<String>(Arrays.asList(s.split("/")));
            String code = myList.get(myList.size() - 1);
            txt.setText(code);
            //beepManager.playBeepSoundAndVibrate();
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);

            detail(view);

            /*if (URLUtil.isValidUrl(result.getText()) && isNetworkConnected()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getText()));
                startActivity(browserIntent);
            }*/
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    /** Called when the user taps the Send button */
    public void detail(View view) {
        // Do something in response to button
        if (!lastText.trim().equals("")) {
            Intent intent = new Intent(this, DetailActivity.class);

            ArrayList<String> put = new ArrayList<String>();
            put.add(lastText.trim());
            put.add("scan");

            intent.putStringArrayListExtra(help.EXTRA_CODE, put);

            startActivity(intent);
        }
    }

    public void search(View view) {
        // Do something in response to button
        if (txt.getText().toString().trim() != null && !txt.getText().toString().trim().equals("")) {
            Intent intent = new Intent(this, DetailActivity.class);

            ArrayList<String> put = new ArrayList<String>();
            put.add(txt.getText().toString().trim());
            put.add("scan");

            intent.putStringArrayListExtra(help.EXTRA_CODE, put);

            startActivity(intent);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:
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

    private void RequestMultiplePermission() {

        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(ScanActivity.this, new String[]
                {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, RequestPermissionCode);

    }

    // Calling override method.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean CameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPhoneStatePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean AccessFineLocation = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (CameraPermission && ReadPhoneStatePermission && AccessFineLocation) {
                        Toast.makeText(ScanActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(ScanActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    public boolean CheckingPermissionIsEnabledOrNot() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        return  FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;
    }
}