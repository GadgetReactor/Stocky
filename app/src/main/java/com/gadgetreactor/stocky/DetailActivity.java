package com.gadgetreactor.stocky;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ASUS on 19/12/2014.
 */
public class DetailActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String IMAGE_URL_BASE = "http://chart.finance.yahoo.com/z?s=";
    JSONObject stock;
    String mImageURL;
    String symbol;
    String timeframe = "&t=3m";
    String extra = "";
    TextView[] buttons = new TextView[4];
    CheckBox checkBoxMACD;
    CheckBox checkBoxBol;
    CheckBox checkBoxRSI;
    CheckBox checkBoxVol;
    String MACD, Bol, RSI, Vol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        buttons[0] = (Button) findViewById(R.id.button1);
        buttons[1] = (Button) findViewById(R.id.button2);
        buttons[2] = (Button) findViewById(R.id.button3);
        buttons[3] = (Button) findViewById(R.id.button4);
        buttons[0].setOnClickListener(this);
        buttons[1].setOnClickListener(this);
        buttons[2].setOnClickListener(this);
        buttons[3].setOnClickListener(this);

        buttons[2].setTextColor(getResources().getColor(R.color.accent));

        checkBoxMACD = (CheckBox) findViewById(R.id.macd);
        checkBoxBol = (CheckBox) findViewById(R.id.checkBox);
        checkBoxRSI = (CheckBox) findViewById(R.id.checkBox2);
        checkBoxVol= (CheckBox) findViewById(R.id.checkBox3);
        checkBoxMACD.setOnCheckedChangeListener(new myCheckBoxChangeClicker());
        checkBoxBol.setOnCheckedChangeListener(new myCheckBoxChangeClicker());
        checkBoxRSI.setOnCheckedChangeListener(new myCheckBoxChangeClicker());
        checkBoxVol.setOnCheckedChangeListener(new myCheckBoxChangeClicker());

        MACD = Bol = RSI = Vol = "";

        mImageURL = IMAGE_URL_BASE + "GOOG";
        try {
            stock = new JSONObject(getIntent().getStringExtra("stock"));
            TextView textView = (TextView) findViewById(R.id.stockDetailName);
            textView.setText(stock.optString("Name"));
            textView = (TextView) findViewById(R.id.change_percent);
            textView.setText(stock.optString("ChangeinPercent"));

            textView = (TextView) findViewById(R.id.change);
            textView.setText(stock.optString("Change"));
            textView = (TextView) findViewById(R.id.price);
            textView.setText(stock.optString("LastTradePriceOnly"));

            List<String> listContents = new ArrayList<String>();

            for(Iterator<String> iter = stock.keys();iter.hasNext();) {
                String key = iter.next();
                String value = stock.get(key).toString();
                listContents.add(key + "  :      " + value);

                ListView myListView = (ListView) findViewById(R.id.listView);
                myListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listContents));
            }

            symbol = stock.optString("symbol");
            mImageURL = IMAGE_URL_BASE + symbol;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getChart(mImageURL);
    }
    public void getChart (String url) {
        ImageView imageView = (ImageView) findViewById(R.id.stock_chart);
        Picasso.with(this).load(url).placeholder(R.drawable.loading).into(imageView);
    }

    @Override
    public void onClick(View v) {
        for (TextView button : buttons) {
            if (button==v) {
                button.setTextColor(getResources().getColor(R.color.accent));
            }
            else {
                button.setTextColor(R.drawable.color_state_button);
            }
        }

        switch(v.getId())
        {
            case R.id.button1 :
                timeframe="&t=1d";
                break;
            case R.id.button2 :
                timeframe="&t=5d";
                break;
            case R.id.button3 :
                timeframe="&t=3m";
                break;
            case R.id.button4 :
                timeframe="&t=1y";
                break;
        }
        getChart(mImageURL+timeframe+extra);
    }

    class myCheckBoxChangeClicker implements CheckBox.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {


            if(isChecked) {
                if(buttonView==checkBoxMACD) {
                    MACD = "e50,e200,";
                }

                if(buttonView==checkBoxBol) {
                    Bol = "b,";
                }

                if(buttonView==checkBoxRSI) {
                    RSI = "r,";
                }

                if(buttonView==checkBoxVol) {
                    Vol = "v,";
                }
                extra = "&p=" + MACD + Bol + "&a=" + RSI + Vol;
                getChart(mImageURL+timeframe+extra);
            }

            else {
                if(buttonView==checkBoxMACD) {
                    MACD = "";
                }

                if(buttonView==checkBoxBol) {
                    Bol = "";
                }

                if(buttonView==checkBoxRSI) {
                    RSI = "";
                }

                if(buttonView==checkBoxVol) {
                    Vol = "";
                }

                extra = "&p=" + MACD + Bol + "&a=" + RSI;
                getChart(mImageURL+timeframe+extra);
            }


        }
    }

}
