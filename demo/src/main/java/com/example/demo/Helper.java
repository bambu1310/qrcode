package com.example.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by BAMBU on 10/18/2017.
 */

public class Helper extends AppCompatActivity {
    private String uriApiRegister = "http://api.gs1.org.vn:96/api/authentication/register?";
    private String uriApiGetItem = "http://api.gs1.org.vn:96//api/mobileapp/getitembygtin?";
    private String uriApiGetContent = "http://api.gs1.org.vn:96//api/mobileapp/getcontents?";

    private String appName = "ScanAndCheck";
    private String privateKey = "7B8F1F5B-0E55-49CB-A1D5-0214887F1A51";
    public SettingDBHelper settingDBHelper;
    public SQLiteDatabase db, settingDB;
    public Activity activity;

    private ProgressDialog progress;
    private String deviceId, osName, osVersion, tokenKey;
    private String jsonStr;
    private JSONObject reader;
    private View view;
    private Context conTxt;


    public static final String EXTRA_CODE = "EXTRA_CODE";

    public String getUriApiRegister() {
        return uriApiRegister;
    }

    public String getUriApiGetItem() {
        return uriApiGetItem;
    }

    public String getUriApiGetContent() {
        return uriApiGetContent;
    }

    public String getAppName() {
        return appName;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getDeviceId(Context c) {
        String deviceId = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);

        return deviceId;
    }

    public String getDeviceId1() {
        String deviceId = "sdfasdfads";//Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);

        return deviceId;
    }

    public String getOsName() {
        String osName = Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

        return osName;
    }

    public String getOsVersion() {
        String osVersion = Build.VERSION.RELEASE;

        return osVersion;
    }

    public String getTokenKey(SQLiteDatabase db) {
        settingDBHelper = new SettingDBHelper(this);
        String tokenKey = settingDBHelper.getApplicationToken(db);

        return tokenKey;//"121BAE45-20B5-4497-9D0F-7039337303CC";
    }

    public String getPhoneNumber(Context c, Integer checkPhonePermission) {
        String phoneNo = "";

        /*if (checkPhonePermission == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tMgr = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            phoneNo = tMgr.getLine1Number().toString();
        }*/

        return phoneNo;
    }

    public String getLatitude() {
        String latitude = "";

        return latitude;
    }

    public String getLatitude1(Context c) {
        String lat = "";

        /*try {
            LocationManager lm = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double latitude = location.getLatitude();
            lat = Double.toString(latitude);
        } catch (SecurityException e) {

        }*/

        return lat;
    }

    public String getLongitude() {
        String longitude = "";

        return longitude;
    }

    public String getLongitude1() {
        String longi = "";

        /*try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            longi = Double.toString(longitude);
        } catch (SecurityException e) {

        }*/

        return longi;
    }

    public String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        return dateFormat.format(date);
    }

    /**
     * Check network
     * @return
     */
    public boolean isNetworkConnected(Context c) {
        //return true;

        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

   public void init_token(SQLiteDatabase dbIn, Context cTxt) {
       conTxt = cTxt;
       settingDB = dbIn;
       tokenKey = getTokenKey(settingDB);
       deviceId = getDeviceId(conTxt);
       osName = getOsName();
       osVersion = getOsVersion();

       if (tokenKey == null) {
           requestToken();
       }
   }

    public void requestToken() {
        new GetClass(this).execute();
    }

    private class GetClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public GetClass(Context c){
            this.context = c;
        }

        protected void onPreExecute(){}

        @Override
        protected Void doInBackground(String... params) {
            try {
                String uri = "http://api.gs1.org.vn:96/api/authentication/register?";
                String urlParameters = "ApplicationName=" + appName + "&ApplicationPrivateKey=" + privateKey + "&DeviceID=" + deviceId + "&OSName=" + osName + "&OSVersion=" + osVersion;
                final URL url = new URL(uri + urlParameters);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
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

                reader = new JSONObject(jsonStr);

                Helper.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Integer responseCode = Integer.parseInt(reader.getString("ResponseCode").toString());

                            if (responseCode == 0) {
                                Toast.makeText(context, reader.getString("ResponseMessage").toString() , Toast.LENGTH_LONG).show();
                            } else {
                                tokenKey = reader.getString("ApplicationTokenKey").toString();
                                Toast.makeText(context, tokenKey, Toast.LENGTH_LONG).show();

                                String sql = "SELECT value FROM " + "setting WHERE key=\"token_key\"";

                                Cursor c = settingDB.rawQuery(sql, null);
                                c.moveToFirst();

                                if (c.getCount() == 0) {
                                    long id;
                                    ContentValues cv = new ContentValues();
                                    cv.put("key", "token_key");
                                    cv.put("value", tokenKey);
                                    //id = settingDB.insert("setting", null, cv);
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
}
