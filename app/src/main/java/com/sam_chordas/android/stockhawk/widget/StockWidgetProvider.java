package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

/**
 * Created by DELL on 9/26/2016.
 */

public class StockWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        int N = appWidgetIds.length;

        for (int i = 0; i<N; ++i) {

            RemoteViews remoteViews = updateWidgetListView(context, appWidgetIds[i],appWidgetManager);
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds[i], R.id.listViewWidget);

        }

    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId, AppWidgetManager appWidgetManager) {

        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_stock);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(R.id.listViewWidget, svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.listViewWidget, R.id.empty_view);


        //setting start DetailActivity
        Intent toastIntent = new Intent(context, StockDetailActivity.class);

        PendingIntent pIntentTemplate = TaskStackBuilder.create(context).addNextIntentWithParentStack(toastIntent).getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.listViewWidget, pIntentTemplate);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);



        // Create an Intent to launch MainActivity
        Intent launchIntent = new Intent(context, MyStocksActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
        return remoteViews;
    }

}