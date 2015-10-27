package com.gadgetreactor.stocky;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ASUS on 23/12/2014.
 */
public class Widget extends AppWidgetProvider {

    private static final String ACTION_UPDATE_CLICK =
            "com.gadgetreactor.stocky.action.UPDATE_CLICK";
    public static JSONArray widgetArray = new JSONArray();
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor editor;
    public static JSONArray jArray = new JSONArray();

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        // An explicit intent directed at the current class (the "self").
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager,
                         final int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mSharedPreferences = context.getSharedPreferences("prefs", 0);
        editor = mSharedPreferences.edit();
        try {
            jArray = new JSONArray(mSharedPreferences.getString("jArray", ""));
            widgetArray = new JSONArray(mSharedPreferences.getString("savedQuery", ""));
            //TODO: Use data
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String stockPreference = "";

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
        }

        urlString = "?q=select%20*%20from%20yahoo.finance.quotes%20where%20symbol%20in%20(%27"+urlString+"%27)&format=json&env=store://datatables.org/alltableswithkeys";
        String QUERY_URL = "https://query.yahooapis.com/v1/public/yql";
        // Create a client to perform networking


        // Loop for every App Widget instance that belongs to this provider.
        // a user might have multiple instances of the same widget

        for (final int appWidgetID : appWidgetIds) {
            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

            Intent serviceIntent = new Intent(context, WidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME))); // embed extras so they don't get ignored
            remoteViews.setRemoteAdapter(R.id.stackWidgetView, serviceIntent);

            // set intent for item click (opens main activity)
            Intent viewIntent = new Intent(context, DetailActivity.class);
            viewIntent.setAction("widget_call");
            viewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
            //
            //JSONObject stock = mDataSet.optJSONObject(getPosition());
            //detailIntent.putExtra("stock", stock.toString());

            viewIntent.setData(Uri.parse(viewIntent.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent viewPendingIntent = PendingIntent.getActivity(context, 0, viewIntent, 0);
            remoteViews.setPendingIntentTemplate(R.id.stackWidgetView, viewPendingIntent);

            remoteViews.setOnClickPendingIntent(R.id.button,
                    getPendingSelfIntent(context,
                            ACTION_UPDATE_CLICK)
            );
            remoteViews.setTextViewText(R.id.stackWidgetEmptyView, "Update in progress");
            remoteViews.setViewVisibility(R.id.button, View.INVISIBLE);
            appWidgetManager.updateAppWidget(appWidgetID, remoteViews);

            AsyncHttpClient client = new AsyncHttpClient();
            client.get(QUERY_URL + urlString,
                    new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            // mDialog.dismiss();
                            // Display a "Toast" message
                            // to announce your success
                            // update the data in your custom method.
                            try {
                                JSONObject query = jsonObject.getJSONObject("query");
                                JSONObject results = query.getJSONObject("results");
                                widgetArray = results.getJSONArray("quote");
                                editor.putString("savedQuery", widgetArray.toString());
                                editor.commit();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                            remoteViews.setTextViewText(R.id.stackWidgetEmptyView, "Updated: " + currentDateTimeString);
                            remoteViews.setViewVisibility(R.id.button, View.VISIBLE);

                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stackWidgetView);
                            appWidgetManager.updateAppWidget(appWidgetID, remoteViews);
                        }

                        @Override
                        public void onFailure(int statusCode, Throwable throwable, JSONObject error) {
                            // Log error message
                            // to help solve any problems
                            Log.e("Stocky", statusCode + " " + throwable.getMessage());
                            remoteViews.setTextViewText(R.id.stackWidgetEmptyView, "Update failed.");
                            remoteViews.setViewVisibility(R.id.button, View.VISIBLE);

                            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stackWidgetView);
                            appWidgetManager.updateAppWidget(appWidgetID, remoteViews);
                        }
                    });
        }
    }

    private void onUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance
                (context);

        // Uses getClass().getName() rather than MyWidget.class.getName() for
        // portability into any App Widget Provider Class
        ComponentName thisAppWidgetComponentName =
                new ComponentName(context.getPackageName(),getClass().getName()
                );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE_CLICK.equals(intent.getAction())) {
            Log.e ("Stocky", "Widget clicked");
            onUpdate(context);
        }
    }



    public static JSONArray getData() {
        return widgetArray;
    }

}