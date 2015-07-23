package com.example.tj.weather.database;

import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import com.example.tj.weather.R;

/**
 * Created by tom on 7/23/2015.
 */
public class DBAdapter extends SimpleCursorAdapter {
    public DBAdapter(Context context, Cursor c) {
        super(context, R.layout.database_listview_row, c, new String[] {DBManager.COLUMN_CITY,
            DBManager.COLUMN_STATE}, new int[] {R.id.dbCityView, R.id.dbStateView}, 0);
    }
}
