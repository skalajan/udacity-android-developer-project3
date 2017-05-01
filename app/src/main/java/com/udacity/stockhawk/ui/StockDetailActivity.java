package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.Constants;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utils;
import com.udacity.stockhawk.data.Contract;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    @BindView(R.id.tv_stock_symbol)
    protected TextView mStockNameTextView;

    @BindView(R.id.tv_stock_price)
    protected TextView mStockPriceTextView;

    @BindView(R.id.tv_stock_dollars_change)
    protected TextView mStockDollarsChangeTextView;

    @BindView(R.id.tv_stock_percentage_change)
    protected TextView mStockPercentageChangeTextView;

    @BindView(R.id.line_chart)
    protected LineChart mLineChart;

    private String mStockSymbol;
    private Cursor mCursor;
    private static final int STOCK_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        ButterKnife.bind(this);

        mStockSymbol = getIntent().getStringExtra(Constants.STOCK_SYMBOL_EXTRA);
        mStockNameTextView.setText(mStockSymbol);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Contract.Quote.makeUriForStock(mStockSymbol), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        updateView();
    }

    private void updateView(){
        if(mCursor != null && mCursor.getCount() > 0){
            mCursor.moveToPosition(0);
            mStockPriceTextView.setText(Utils.getDollarsFormat().format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));

            float dollarsChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            mStockDollarsChangeTextView.setText(Utils.getDollarsChangeFormat().format(dollarsChange));
            if(dollarsChange > 0)
                mStockDollarsChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            else
                mStockDollarsChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);

            float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE) / 100;
            mStockPercentageChangeTextView.setText(Utils.getPercentageChangeFormat().format(percentageChange));
            if(percentageChange > 0)
                mStockPercentageChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            else
                mStockPercentageChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);

            prepareChart(mCursor.getString(Contract.Quote.POSITION_HISTORY), mLineChart);
        }
    }

    private void prepareChart(String historyString, LineChart chart) {
        ArrayList<Entry> entriesDataSet = new ArrayList<>();
        String[] records = historyString.split("\n");

        long beginingTimestamp = 0;
        for(int i = records.length - 1; i >= 0; i--){
            String record = records[i];
            String[] values = record.split(", ");
            if(values.length == 2){
                try {
                    long timestamp = Long.parseLong(values[0]);
                    if(beginingTimestamp == 0)
                        beginingTimestamp = timestamp;

                    timestamp -= beginingTimestamp;
                    float value = Float.parseFloat(values[1]);

                    Entry entry = new Entry(timestamp, value);
                    entriesDataSet.add(entry);
                }catch (NumberFormatException e){
                    Timber.w("Historic data parsing failed");
                }
            }
        }
        LineDataSet dataSet = new LineDataSet(entriesDataSet, "");
        dataSet.setColor(getResources().getColor(R.color.chart_line));
        dataSet.setLineWidth(4.5f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleColor(getResources().getColor(R.color.chart_circles));
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(false);
        dataSet.setDrawValues(false);
        dataSet.setFillColor(getResources().getColor(R.color.chart_fill));
        dataSet.setDrawFilled(true);
        chart.getLegend().setEnabled(false);



        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ChartDateFormatter(beginingTimestamp));
        xAxis.setLabelCount(6, false);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(getResources().getColor(R.color.chart_labels));
        xAxis.setDrawGridLines(false);

        LineData data = new LineData(dataSet);

        YAxis axisLeft = chart.getAxisLeft();
        axisLeft.setTextColor(getResources().getColor(R.color.chart_labels));
        axisLeft.setValueFormatter(new DollarAxisValueFormatter());

        chart.getAxisRight().setDrawLabels(false);
        chart.getDescription().setEnabled(false);

        chart.setData(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

    private class ChartDateFormatter implements IAxisValueFormatter{
        DateFormat mFormat;
        long beginningTimestamp;

        ChartDateFormatter(long beginningTimestamp){
            mFormat = new SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(Locale.ENGLISH, getString(R.string.chart_date_label_skeleton)), Locale.ENGLISH);
            this.beginningTimestamp = beginningTimestamp;
        }


        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long timestamp = (long) value + beginningTimestamp;
            Date date = new Date(timestamp);
            return mFormat.format(date);
        }
    }

    private class DollarAxisValueFormatter implements IAxisValueFormatter{
        DecimalFormat mFormat;

        DollarAxisValueFormatter(){
            mFormat = Utils.getDollarsFormat();
            mFormat.setMaximumFractionDigits(0);
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mFormat.format(value);
        }
    }
}
