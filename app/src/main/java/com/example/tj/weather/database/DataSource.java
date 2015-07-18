package com.example.tj.weather.database;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by tj on 7/17/2015.
 *
 * Only three methods will be needed.   I didn't have to create this class, but it is a nice, simple
 * example of creating a generic class.
 */
public abstract class DataSource<T> {
    protected SQLiteDatabase database;

    /* I should probably check to see if the database is null, and throw a NullPointerException if so... */
    public DataSource(SQLiteDatabase database) {
            this.database = database;
    }

    public abstract boolean insert(T Entity);
    public abstract boolean delete(T Entity);
    public abstract List<T> getAll();
    public abstract boolean deleteAll();
}
