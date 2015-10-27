package com.gadgetreactor.stocky;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends ActionBarActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String PREFS = "prefs";
    SharedPreferences mSharedPreferences;
    SharedPreferences sharedPref;
    JSONArray jArray = new JSONArray();
    JSONArray initArray = new JSONArray();
    private RecyclerView mRecyclerView;
    private StockViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    Button addButton;
    SwipeRefreshLayout swipeView;
    private Handler mHandler;
    int refreshInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeView.setOnRefreshListener(this);

        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        addButton = (Button) findViewById(R.id.add_button);

        addButton.setOnClickListener(this);

        // Read the stock list,
        // or an empty string if nothing found
        try {
            jArray = new JSONArray(mSharedPreferences.getString("jArray", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jArray.length() == 0) {
        // initialise jArray with default stock settings
            jArray.put("O39.SI");
            jArray.put("D05.SI");
            editor.putString("jArray", jArray.toString());
            editor.commit();
            Toast.makeText(getApplicationContext(), "Hello, thanks for using Stocky!", Toast.LENGTH_LONG).show();
        }
        // load existing query
        // todo: check last refresh time; if within 5 min, do not make API call
        try {
            initArray = new JSONArray(mSharedPreferences.getString("savedQuery", ""));
            mAdapter = new StockViewAdapter(initArray);
            mRecyclerView.setAdapter(mAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        queryStocks();

        String value = sharedPref.getString("refresh_interval", "300");
        refreshInterval = Integer.valueOf(value) * 1000;

        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable,refreshInterval);
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
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startActivity = new Intent(this, UserPreferenceActivity.class);
            startActivity(startActivity);
            return true;
        }

        if (id == R.id.refresh) {
            onRefresh();
        }

        if (id == R.id.stock_list_edit) {

            final ArrayList<String> listdata = new ArrayList<String>();
            final ArrayList<Integer> selList=new ArrayList();
            final JSONArray saveArray = new JSONArray();

            if (jArray != null) {
                for (int i=0;i<jArray.length();i++){
                    try {
                        listdata.add(jArray.get(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

//            final CharSequence myList[] = { "Tea", "Coffee", "Milk" };
            boolean bl[] = new boolean[listdata.size()];

            final CharSequence[] myList = listdata.toArray(new CharSequence[listdata.size()]);

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Select stocks to be removed");

            alert.setMultiChoiceItems(myList, bl, new DialogInterface.OnMultiChoiceClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
                    // TODO Auto-generated method stub

                    if (arg2) {
                        selList.add(arg1);
                    } else if (selList.contains(arg1)) {
                        selList.remove(Integer.valueOf(arg1));
                    }
                }
            });

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    String msg = "";
                    for (int i = 0; i < selList.size(); i++) {
                        int pos = selList.get(i);
                        listdata.set(pos, "");
                    }

                    for (int i = 0; i < listdata.size(); i++) {
                        if (listdata.get(i) != "") {
                            saveArray.put(listdata.get(i));
                        }
                    }

                    jArray = saveArray;
                    queryStocks();
                    Toast.makeText(getApplicationContext(),
                            "Item(s) deleted.", Toast.LENGTH_LONG)
                            .show();

                    editor.putString("jArray", jArray.toString());
                    editor.commit();

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        onRefresh();
    }

    private void queryStocks() {

        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        String stockPreference = "";
        final TextView textView = (TextView) findViewById(R.id.updatedTime);
        try {
            for (int i = 0; i < jArray.length(); i++) {
                stockPreference = stockPreference  + " " + jArray.getString(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";

        try {
            urlString = URLEncoder.encode(stockPreference, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        String QUERY_URL = "https://query.yahooapis.com/v1/public/yql";
        urlString = "?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%27"+urlString+"%27)&format=json&env=store://datatables.org/alltableswithkeys";
        // Create a client to perform networking

        AsyncHttpClient client = new AsyncHttpClient();
        // Show ProgressDialog to inform user that a task in the background is occurring
        // mDialog.show();
        // Have the client get a JSONArray of data
        // and define how to respond
        client.get(QUERY_URL + urlString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(JSONObject jsonObject) {

                        try {
                            JSONObject query = jsonObject.getJSONObject("query");
                            JSONObject results = query.getJSONObject("results");
                            JSONArray quote = results.getJSONArray("quote");
                            mAdapter = new StockViewAdapter(quote);
                            editor.putString("savedQuery", quote.toString());
                            editor.commit();
                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                            textView.setText("Updated: " + currentDateTimeString);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mRecyclerView.setAdapter(mAdapter);
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable throwable, JSONObject error) {
                        // mDialog.dismiss();

                        Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        // Log error message
                        Log.e("Stocky", statusCode + " " + throwable.getMessage());
                    }
                });
    }

    public void onClick(View v) {
        Intent myIntent = new Intent(MainActivity.this, SearchActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    @Override
    public void onRefresh() {
        swipeView.setRefreshing(true);

        try {
            jArray = new JSONArray(mSharedPreferences.getString("jArray", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeView.setRefreshing(false);
                queryStocks();
            }
        }, 3000);
    }

    private final Runnable m_Runnable = new Runnable() {
        public void run() {
            onRefresh();
            String value = sharedPref.getString("refresh_interval", "300");
            refreshInterval = Integer.valueOf(value) * 1000;
            MainActivity.this.mHandler.postDelayed(m_Runnable, refreshInterval);
        }
    };//runnable

}
