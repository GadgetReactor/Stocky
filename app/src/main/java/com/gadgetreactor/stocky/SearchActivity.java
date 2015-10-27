package com.gadgetreactor.stocky;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS on 12/1/2015.
 */
public class SearchActivity extends ActionBarActivity implements View.OnClickListener {

    Button mainButton;
    EditText mainEditText;
    ListView mainListView;
    ProgressDialog mDialog;
    ArrayAdapter<String> arrayAdapter;
    List<String> listContents;
    List<String> listValues;
    private static final String PREFS = "prefs";
    SharedPreferences mSharedPreferences;
    JSONArray jArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        mainEditText = (EditText) findViewById(R.id.main_edittext);

        mainListView = (ListView) findViewById(R.id.main_listview);

// 10. Create a JSONAdapter for the ListView
        listContents = new ArrayList<String>();
        listValues = new ArrayList<String>();

        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listContents);
        mainListView.setAdapter(arrayAdapter);

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                addStock (listContents.get(position), listValues.get(position));
            }
        });

        arrayAdapter.notifyDataSetChanged();
        mainEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_SEARCH) {
                    Log.e("Stocky", mainEditText.getText().toString());
                    queryStockName(mainEditText.getText().toString());
                }
                return false;
            }
        });

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Searching");
        mDialog.setCancelable(true);

        // 7. Greet the user, or ask for their name if new
    }

    private void addStock(String name, final String s) {

        try {
            jArray = new JSONArray(mSharedPreferences.getString("jArray", ""));
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Stock - Add");
        alert.setMessage(name+"\\nWould you like to add " + s +"?");

        // Make an "OK" button to save the name
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {

                SharedPreferences.Editor editor = mSharedPreferences.edit();

                jArray.put(s);

                editor.putString("jArray", jArray.toString());
                editor.commit();
                Toast.makeText(getApplicationContext(), s + " added", Toast.LENGTH_LONG).show();
            }
        });

        // Make a "Cancel" button
        // that simply dismisses the alert
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu.
        // Adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    private void queryStockName(String searchString) {
        listContents.clear();
        listValues.clear();

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            // if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        // Show ProgressDialog to inform user that a task in the background is occurring
        mDialog.show();
        // Have the client get a JSONArray of data
        // and define how to respond

        client.get("http://d.yimg.com/autoc.finance.yahoo.com/autoc?query="+urlString+"&callback=YAHOO.Finance.SymbolSuggest.ssCallback",

                new TextHttpResponseHandler() {

                @Override
                public void onSuccess(String responseBody) {
                        mDialog.dismiss();
                        // Display a "Toast" message
                        // to announce your success

                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
                        // update the data in your custom method.
                        int strlen = responseBody.length();
                        responseBody = responseBody.substring(39, strlen-1);

                        try {
                            JSONObject response = new JSONObject(responseBody);
                            JSONObject resultSet = response.getJSONObject("ResultSet");
                            JSONArray results = resultSet.getJSONArray("Result");

                            for(int i=0;i<results.length();i++){
                                JSONObject result = results.getJSONObject(i);
                                String symbol = result.optString("symbol");
                                if(!symbol.contains(":")) {
                                    listContents.add (result.optString("name") + "(" + result.optString("exchDisp")+")");
                                    listValues.add(result.optString("symbol"));
                                }

                            }
                            arrayAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                @Override
                public void onFailure(String responseBody, Throwable e) {
                        mDialog.dismiss();
                        // Display a "Toast" message
                        // to announce the failure
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

                        // Log error message
                        // to help solve any problems
                        Log.e("Stocky", e.getMessage());
                    }
                });
    }

    @Override
    public void onClick(View v) {
        queryStockName(mainEditText.getText().toString());

    }
}