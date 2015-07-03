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
public class DBManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "locations.db";
    public static final int VERSION = 1;

    public static final String TABLE_NAME = "locationsdb";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_CITY = "city";
    public static final String COLUMN_NAME_STATE = "state";

    public static final String dbCreate = "CREATE TABLE "
            + TABLE_NAME
            + " ("+ COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOIMCREMENT, "
            + COLUMN_NAME_CITY + " TEXT NOT NULL, "
            + COLUMN_NAME_STATE + " TEXT NOT NULL);";

    public DBManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
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

    //////////////////DB Access methods below////////////////////////////
}
