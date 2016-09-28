package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

import java.util.ArrayList;

/**
 * Created by DELL on 9/27/2016.
 */
public class ListProvider implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList listItemList = new ArrayList();
    private Context context = null;
    private Cursor cursor = null;


    int mWidgetId;

    public ListProvider(Context context, Intent intent) {

        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        this.context = context;

    }


    @Override
    public void onCreate() {

    }


    @Override
    public void onDataSetChanged() {

        if (cursor != null) {
            cursor.close();
        }

        /*
         http://stackoverflow.com/questions/13187284/android-permission-denial-in-widget-remoteviewsfactory-for-content
        * */

        long token = Binder.clearCallingIdentity();
        try {

            cursor = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);

            populateListItem();

        } finally {
            Binder.restoreCallingIdentity(token);
        }


    }

    @Override
    public void onDestroy() {

        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
        ListItem listItem = (ListItem) listItemList.get(position);

        remoteView.setTextViewText(R.id.stock_symbol, listItem.symbol);
        remoteView.setTextViewText(R.id.bid_price, listItem.bid);
        remoteView.setTextViewText(R.id.change, listItem.change);

        Bundle extras = new Bundle();
        extras.putString(StockDetailActivity.GET_SYMBOL, listItem.symbol);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteView.setOnClickFillInIntent(R.id.llItem, fillInIntent);

        //int sdk = Build.VERSION.SDK_INT;

        if (listItem.is_up == 1) {
           /* if (sdk < Build.VERSION_CODES.JELLY_BEAN){

                remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);

            }else {*/

            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);


            //}
        } else {
           /* if (sdk < Build.VERSION_CODES.JELLY_BEAN) {

                remoteView.setInt(R.id.change, "setBackgroundDrawable", R.drawable.percent_change_pill_red);

            } else{*/

            remoteView.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);

            //}
        }


        return remoteView;
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


    private void populateListItem() {

        listItemList.clear();

        if (cursor.moveToFirst()) {

            do {

                ListItem listItem = new ListItem();
                listItem.symbol = cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL));
                listItem.bid = cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE));
                listItem.change = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));

                listItem.is_up = cursor.getInt(cursor.getColumnIndex(QuoteColumns.ISUP));
                listItem.percent_change = cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));

                listItemList.add(listItem);

            } while (cursor.moveToNext());

        }

    }


}
