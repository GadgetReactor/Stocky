package com.gadgetreactor.stocky;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS on 21/12/2014.
 */
public class StockViewAdapter extends RecyclerView.Adapter<StockViewAdapter.ViewHolder> {

    private static JSONArray mDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView stockName;
        private final TextView symbol;
        private final TextView price;
        private final TextView change;
        private final TextView percentage;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("StockyViewAdapter", "Test Element " + getPosition() + " clicked.");
                    Context context = itemView.getContext();
                    Intent detailIntent = new Intent(context, DetailActivity.class);
                    JSONObject stock = mDataSet.optJSONObject(getPosition());
                    detailIntent.putExtra("stock", stock.toString());

                    context.startActivity(detailIntent);
                }
            });

            stockName = (TextView) v.findViewById(R.id.stockName);
            symbol =  (TextView) v.findViewById(R.id.symbol);
            price =  (TextView) v.findViewById(R.id.price);
            change =  (TextView) v.findViewById(R.id.change);
            percentage = (TextView) v.findViewById(R.id.change_percent);
        }
    }

    public StockViewAdapter(JSONArray dataSet) {
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.stock_view_main, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        Context context;
        JSONObject stock = mDataSet.optJSONObject(position);
        viewHolder.stockName.setText(stock.optString("Name"));
        viewHolder.symbol.setText(stock.optString("symbol"));
        viewHolder.price.setText(stock.optString("LastTradePriceOnly"));
        viewHolder.change.setText(stock.optString("Change"));
        viewHolder.percentage.setText(" ["+stock.optString("ChangeinPercent")+"]");
        String changeamt = stock.optString("Change");
        if (changeamt.substring(0, 1).equals("+")) {
            viewHolder.percentage.setTextColor(Color.parseColor("#00BA00"));
        } else if (changeamt.substring(0, 1).equals("-")) {
            viewHolder.percentage.setTextColor(Color.parseColor("#E00000"));
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.length();
    }
}
