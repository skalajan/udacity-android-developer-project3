package com.udacity.stockhawk.widgets;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.Constants;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utils;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;

/**
 * Created by kjs566 on 10.4.2017.
 */

public class StocksListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{
    private Context mContext;
    private Intent mIntent;
    private Cursor mCursor;

    DecimalFormat mPercentageFormat;
    DecimalFormat mDollarsFormat;

    public StocksListRemoteViewsFactory(Context context, Intent intent){
        this.mContext = context;
        this.mIntent = intent;

        mPercentageFormat = Utils.getPercentageChangeFormat();
        mDollarsFormat = Utils.getDollarsFormat();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(Contract.Quote.URI, null, null, null, null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        if(mCursor == null)
            return 0;
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv =  new RemoteViews(mContext.getPackageName(), R.layout.stocks_list_widget_item);

        mCursor.moveToPosition(position);
        mCursor.getString(Contract.Quote.POSITION_ID);
        rv.setTextViewText(R.id.tv_stock_name, mCursor.getString(Contract.Quote.POSITION_SYMBOL));

        float price = mCursor.getFloat(Contract.Quote.POSITION_PRICE);
        rv.setTextViewText(R.id.tv_stock_price, mDollarsFormat.format(price));

        float change = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE)/100;
        rv.setTextViewText(R.id.tv_stock_change, mPercentageFormat.format(change));

        if (change > 0) {
            rv.setInt(R.id.tv_stock_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.tv_stock_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        Bundle extras = new Bundle();
        extras.putString(Constants.STOCK_SYMBOL_EXTRA, mCursor.getString(Contract.Quote.POSITION_SYMBOL));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.rv_item, fillInIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


}
