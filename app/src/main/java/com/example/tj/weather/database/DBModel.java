package com.example.tj.weather.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tj on 7/17/2015.
 */
public class DBModel extends DataSource<DBLocation> {

    private SQLiteDatabase db;

    public DBModel(SQLiteDatabase db) {
        super(db);

        this.db = db;
    }

    @Override
    public boolean insert(DBLocation location) {
        if (location == null) {
            return false;
        }

        return db.insert(DBManager.TABLE_NAME, null, getContentValuesFromObject(location)) != -1;
    }

    @Override
    public boolean delete(DBLocation location) {
        return db.delete(DBManager.TABLE_NAME, DBManager.COLUMN_CITY + " =? AND " +
                DBManager.COLUMN_STATE + " =?", new String[] {location.getCity(), location.getStateOrCountry()}) != 0;
    }

    /* Pretty self-explanatory.  Query the db for all results, iterate over the cursor, create locations,
    add them to a list of locations, then return it.   It is possible that this list could grow large enough
    that doing it asynchronously might be helpful. */
    @Override
    public List<DBLocation> getAll() {
        List<DBLocation> locations = new ArrayList();

        Cursor c = db.query(
                DBManager.TABLE_NAME,
                new String[]{DBManager.COLUMN_CITY,
                        DBManager.COLUMN_STATE},
                null,
                null,
                null,
                null,
                null);

        if (c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                DBLocation location = new DBLocation();
                location.setCity(c.getString(0));
                location.setStateOrCountry(c.getString(1));

                locations.add(location);

                c.moveToNext();
            }
        }

        c.close();

        return locations;
    }

    public boolean deleteAll() {
        return db.delete(DBManager.TABLE_NAME, "1", null) != -1;
    }

    private ContentValues getContentValuesFromObject(DBLocation location) {
        ContentValues values = new ContentValues();
        values.put(DBManager.COLUMN_CITY, location.getCity());
        values.put(DBManager.COLUMN_STATE, location.getStateOrCountry());

        return values;
    }
}
