package com.udacity.stockhawk.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;

import com.udacity.stockhawk.Constants;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class StocksListWidgetProvider extends AppWidgetProvider{

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stocks_list_widget);
        Intent intent = new Intent(context, StocksListWidgetService.class);
        views.setRemoteAdapter(R.id.applistwidget_listview, intent);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Constants.MAIN_ACTIVITY_PENDING_INTENT_REQUEST_CODE, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);

        Intent detailIntent = new Intent(context, StockDetailActivity.class);
        detailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent detailPendingIntent = PendingIntent.getActivity(context, Constants.STOCK_DETAIL_ACTIVITY_PENDING_INTENT_REQUEST_CODE, detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.applistwidget_listview, detailPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        context.getContentResolver().registerContentObserver(Contract.Quote.URI, true, new ContentObserver(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        })) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }
        });
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private void registerContentObserver(Context context){

    }

    private void unregisterContentObserver(Context context){

    }

    class StocksContentObserver extends ContentObserver{

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public StocksContentObserver(Handler handler) {
            super(handler);
        }
    }
}

