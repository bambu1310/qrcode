package com.example.demo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ibelieveican on 2015/7/7.
 */
public class HistoryDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "history";
    private static final int DATABASE_VERSION = 1;
    public HistoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE history (" +
                " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " code TEXT, content TEXT, created_at DATETIME)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    public void clear(SQLiteDatabase db) {
        db.execSQL("DELETE FROM history");
    }
}
