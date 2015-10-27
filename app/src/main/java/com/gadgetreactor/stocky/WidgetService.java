package com.gadgetreactor.stocky;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by ASUS on 26/12/2014.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private JSONArray mBuzzes;
    private Context mContext;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    public void onCreate() {

    }

    public void onDestroy() {
        mBuzzes = new JSONArray();
    }

    public int getCount() {
        return mBuzzes.length();
    }

    public RemoteViews getViewAt(int position) {
        String stockname;
        String symbol;
        String price;
        String change;
        String percentage;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.stock_view_widget);


        if (position <= getCount()) {
            JSONObject stock = mBuzzes.optJSONObject(position);
            stockname = stock.optString("Name");
            symbol = stock.optString("symbol");
            price = stock.optString("LastTradePriceOnly");
            change = stock.optString("Change");
            percentage = " [" + stock.optString("ChangeinPercent") + "]";

            rv.setTextViewText(R.id.stockName, stockname);
            rv.setTextViewText(R.id.symbol, symbol);
            rv.setTextViewText(R.id.price, price);
            rv.setTextViewText(R.id.change, change);
            rv.setTextViewText(R.id.change_percent, percentage);
            String changeamt = stock.optString("Change");
            if (changeamt.substring(0, 1).equals("+")) {
                rv.setTextColor(R.id.change_percent, Color.parseColor("#00BA00"));
            } else if (changeamt.substring(0, 1).equals("-")) {
                rv.setTextColor(R.id.change_percent, Color.parseColor("#E00000"));
            }

            // store the ID in the extras so the main activity can use it
            // Bundle extras = new Bundle();
            Intent fillInIntent = new Intent();
            fillInIntent.putExtra("stock", stock.toString());

            rv.setOnClickFillInIntent(R.id.widgetItem, fillInIntent);
        }

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        mBuzzes = Widget.getData();
    }
}