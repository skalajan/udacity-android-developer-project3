package com.udacity.stockhawk.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by kjs566 on 10.4.2017.
 */

public class StocksListWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StocksListRemoteViewsFactory(getApplicationContext(), intent);
    }
}
