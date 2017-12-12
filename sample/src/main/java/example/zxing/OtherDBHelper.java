package example.zxing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ibelieveican on 2015/7/7.
 */
public class OtherDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "other";
    private static final int DATABASE_VERSION = 1;
    public OtherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE other (" +
                " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " title TEXT, content TEXT, language TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS other");
        onCreate(db);
    }

    public void clear(SQLiteDatabase db, String language) {
        db.execSQL("DELETE FROM other WHERE language=\"" + language + "\"");
    }
}
