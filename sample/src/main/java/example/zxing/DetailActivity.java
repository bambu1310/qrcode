package example.zxing;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.zxing.integration.android.IntentIntegrator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
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
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by BAMBU on 10/5/2017.
 */

public class DetailActivity extends Activity {
    public HttpURLConnection urlConnection;
    private View view;
    //private TextView textView;


    private ProgressDialog progress;
    private String code;
    private String jsonStr;
    private JSONObject reader;
    public String statusColor;

    private ListView listView, listViewProduct, listViewStatusMessage;
    //private TextView txtView;

    public Helper help;
    public SQLiteDatabase favoriteDB, settingDB, historyDB;
    public SettingDBHelper settingDBHelper;
    public FavoriteDBHelper favoriteDBHelper;
    public HistoryDBHelper historyDBHelper;
    public String language;

    public long idFavorite;
    public ImageView img;
    public String productImage;
    public String productNoImageMs;
    private MainActivity main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list_content_business);
        listViewProduct = (ListView) findViewById(R.id.list_content_product);
        listViewStatusMessage = (ListView) findViewById(R.id.status_message);

        settingDBHelper = new SettingDBHelper(this);
        settingDB = settingDBHelper.getWritableDatabase();

        favoriteDBHelper = new FavoriteDBHelper(this);
        favoriteDB = favoriteDBHelper.getWritableDatabase();

        language = settingDBHelper.getLanguage(settingDB);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        String msg = getResources().getString(R.string.msg_no_internet);
        String title = getResources().getString(R.string.title_detail);
        String titleBusiness = getResources().getString(R.string.title_business_info);
        String titleProduct = getResources().getString(R.string.title_product_info);
        productNoImageMs = getResources().getString(R.string.msg_no_get_image);

        if (language.equals("en")) {
            title = getResources().getString(R.string.title_detail_en);
            msg = getResources().getString(R.string.msg_no_internet_en);

            titleBusiness = getResources().getString(R.string.title_business_info_en);
            titleProduct = getResources().getString(R.string.title_product_info_en);

            productNoImageMs = getResources().getString(R.string.msg_no_get_image_en);
        }

        toolbar.setTitle(title);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout); // get the reference of TabLayout
        tabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        //tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.white));
        tabLayout.setTabTextColors(getResources().getColor(R.color.gray), getResources().getColor(R.color.white));
        TabLayout.Tab productTab = tabLayout.newTab(); // Create a new Tab names
        productTab.setText(titleProduct); // set the Text for the first Tab
        tabLayout.addTab(productTab);
        TabLayout.Tab businessTab = tabLayout.newTab(); // Create a new Tab names
        businessTab.setText(titleBusiness); // set the Text for the first Tab
        tabLayout.addTab(businessTab);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // called when tab selected
                switch (tab.getPosition()) {
                    case 0:
                        listView.setVisibility(ListView.INVISIBLE);
                        listViewProduct.setVisibility(ListView.VISIBLE);
                        break;
                    case 1:
                        listView.setVisibility(ListView.VISIBLE);
                        listViewProduct.setVisibility(ListView.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // called when tab unselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // called when a tab is reselected
            }
        });

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();

        ArrayList<String> content = intent.getStringArrayListExtra(help.EXTRA_CODE);

        String result = content.get(0);
        String act = content.get(1);

        List<String> myList = new ArrayList<String>(Arrays.asList(result.split("/")));
        code = myList.get(myList.size() - 1);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (act.equals("scan")) {
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
        } else if (act.equals("history")) {
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
        } else if (act.equals("favorite")) {
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
        }

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

        if (code != null) {
            historyDBHelper = new HistoryDBHelper(this);
            historyDB = historyDBHelper.getWritableDatabase();
            help = new Helper();
            //help.init_token(settingDB, getApplicationContext());

            if (act.equals("scan")) {
                //historyDB.execSQL("DELETE FROM history WHERE code=\"" + code + "\"");

                long id;
                ContentValues cv = new ContentValues();
                cv.put("code", result.toString());
                cv.put("content", result.toString());
                cv.put("created_at", getDateTime());
                id = historyDB.insert("history", null, cv);
            }

            if (isNetworkConnected()) {
                String tokenKey = help.getTokenKey(settingDB);

                if (tokenKey == null) {
                    main_init(view);
                } else {
                    sendGetRequest(view);
                }
            } else {
                ArrayList<String> codeList = new ArrayList<String>();
                codeList.add(code);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(DetailActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, codeList);
                // Assign adapter to ListView
                listViewProduct.setAdapter(adapter);

                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }

        }
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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

    public void main_init(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
            /*progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show();*/
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                /*String uri = help.getUriApiGetItem();
                String appName = help.getAppName();
                String tokenKey = help.getTokenKey(settingDB);
                String deviceId = help.getDeviceId(getApplicationContext());
                String phoneNo = "";//help.getPhoneNumber(getApplicationContext(), ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.READ_PHONE_STATE));
                String latitude = help.getLatitude();
                String longitude = help.getLongitude();
                //String urlParameters = "DeviceID=" + deviceId + "&PhoneNo=" + phoneNo + "&Latitude=" + latitude + "&Longitude=" + longitude + "&ApplicationName=" + appName + "&ApplicationTokenKey=" + tokenKey + "&Language=" + language + "&GTIN=" + code;

                String type = settingDBHelper.getUserType(settingDB);

                String urlParameters = code + "/" + type;
                final URL url = new URL(uri + urlParameters);*/

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

                /*final StringBuilder result = new StringBuilder();
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
                final JSONObject object = new JSONObject(jsonStr);*/

                DetailActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //txtView.setText(jsonStr);
                       /* ArrayList<String> codeList = new ArrayList<String>();
                        ArrayList<String> codeListProduct = new ArrayList<String>();
                        String statusMessage = "";
                        Integer responseCode = 0;
                        String productImageLabel = "";

                        try {
                            responseCode = Integer.parseInt(object.getString("ResponseCode").toString());

                            statusMessage = object.getString("StatusMessage").toString();
                            statusColor = object.getString("StatusMessageColor").toString().replace("“", "");

                            if (responseCode == 1) {
                                JSONObject obj = object.getJSONObject("ObjectData");

                                Iterator<String> iter = obj.keys();
                                final ArrayList<String> listBusiness = new ArrayList<String>();
                                listBusiness.add("ManufacturerPartyName");
                                listBusiness.add("ManufacturerGLN");
                                listBusiness.add("CompanyName");
                                listBusiness.add("BizLicense_No");
                                listBusiness.add("Address");
                                listBusiness.add("Phone");
                                listBusiness.add("Fax");
                                listBusiness.add("Email");
                                listBusiness.add("Website");
                                final ArrayList<String> listProduct = new ArrayList<String>();
                                listProduct.add("ProductImageURL");
                                listProduct.add("ProductName");
                                listProduct.add("BrandName");
                                listProduct.add("ProductDescription");
                                listProduct.add("ProductSize");
                                listProduct.add("NetWeight");

                                while (iter.hasNext()) {
                                    String key = iter.next();

                                    try {
                                        JSONObject item = obj.getJSONObject(key);
                                        if (key.equals("ProductImageURL")) {
                                            productImage = item.getString("Value").toString();
                                            productImageLabel = item.getString("LabelName").toString();
                                        } else if (listBusiness.contains(key)) {
                                            codeList.add(item.getString("LabelName").toString() + ": " + item.getString("Value").toString());
                                        } else {
                                            codeListProduct.add(item.getString("LabelName").toString() + ": " + item.getString("Value").toString());
                                        }
                                    } catch (JSONException e) {
                                        // Something went wrong!
                                    }

                                }
                            } else {
                                //codeList.add(code);
                            }
                        } catch (JSONException e) {
                            //codeList.add(e.getMessage());
                            e.printStackTrace();
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DetailActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, codeList) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View row = super.getView(position, convertView, parent);

                                if (position == 0) {
                                    // do something change color
                                    row.setBackgroundColor(Color.parseColor(statusColor)); // some color
                                } else {
                                    // default state
                                    row.setBackgroundColor(Color.TRANSPARENT); // default coloe
                                }

                                return row;
                            }
                        };
                        // Assign adapter to ListView
                        listView.setAdapter(adapter);

                        ArrayAdapter<String> adapterProduct = new ArrayAdapter<String>(DetailActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, codeListProduct) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View row = super.getView(position, convertView, parent);

                                if (position == 0) {
                                    // do something change color
                                    row.setBackgroundColor(Color.parseColor(statusColor)); // some color
                                } else {
                                    // default state
                                    row.setBackgroundColor(Color.TRANSPARENT); // default coloe
                                }

                                return row;
                            }
                        };
                        listViewProduct.setAdapter(adapterProduct);

                        if (responseCode == 1) {
                            final ToggleButton btnFavorite = new ToggleButton(DetailActivity.this);
                            String txtOn = getResources().getString(R.string.title_favorite);
                            String txtOff = getResources().getString(R.string.title_favorite_off);

                            if (language.equals("en")) {
                                txtOn = getResources().getString(R.string.title_favorite_en);
                                txtOff = getResources().getString(R.string.title_favorite_off_en);
                            }

                            String txt = txtOn;
                            String sql = "SELECT code FROM " + "favorite WHERE code=\"" + code + "\"";

                            Cursor c = favoriteDB.rawQuery(sql, null);
                            c.moveToFirst();

                            if (c.getCount() > 0) {
                                txt = txtOff;
                                btnFavorite.setChecked(true);
                            }

                            btnFavorite.setText(txt);
                            btnFavorite.setTextOn(txtOff);
                            btnFavorite.setTextOff(txtOn);

                            // attach an OnClickListener
                            btnFavorite.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (btnFavorite.isChecked()) {
                                        ContentValues cv = new ContentValues();
                                        cv.put("code", code);
                                        cv.put("content", code);
                                        cv.put("created_at", getDateTime());
                                        idFavorite = favoriteDB.insert("favorite", null, cv);
                                    } else {
                                        favoriteDB.execSQL("DELETE FROM favorite WHERE code=\"" + code + "\"");
                                    }
                                }
                            });

                            listViewProduct.addFooterView(btnFavorite);
                            listView.addFooterView(btnFavorite);


                            ImageView imageHeaderView = new ImageView(DetailActivity.this);
                            Picasso.with(getApplicationContext()).load(productImage).into(imageHeaderView);

                            if (loadImageFromURL(productImage)) {
                                listViewProduct.addHeaderView(imageHeaderView);
                            } else {
                                if (URLUtil.isValidUrl(productImage)) {
                                    codeListProduct.add(0, productImageLabel + ": " + productNoImageMs);
                                } else {
                                    codeListProduct.add(0, productImageLabel + ": " + productImage);
                                }
                            }
                        }

                        codeList.add(0, statusMessage);
                        codeListProduct.add(0, statusMessage);*/

                        progress.dismiss();
                    }
                });


            /*} catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
            /*} catch (JSONException e) {
                e.printStackTrace();*/
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            return null;
        }

        public boolean loadImageFromURL(String fileUrl) {
            try {
                URLConnection connection = new URL(fileUrl).openConnection();
                String contentType = connection.getHeaderField("Content-Type");
                boolean image = contentType.startsWith("image/");

                return image;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    }

}

