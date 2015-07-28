package com.example.tj.weather.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by tom on 6/15/2015.
 * License: Public Domain.
 *
 * This class creates and manages access to the database of previous locations that users have
 * searched.  This is the DAO and DB manager class.  I don't see a need to separate them.
 */
public final class DBManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "dblocations.db";
    public static final int VERSION = 1;

    public static final String TABLE_NAME = "dblocationsdb";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_STATE = "state";

    public static final String dbCreate = "create table "
            + TABLE_NAME + " ( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_CITY + " text not null, "
            + COLUMN_STATE + " text not null);";

    public DBManager(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(dbCreate);

        Log.i("DB", "created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }
}
