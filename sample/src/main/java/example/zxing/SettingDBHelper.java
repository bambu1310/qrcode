package example.zxing;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ibelieveican on 2015/7/7.
 */
public class SettingDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "setting";
    private static final int DATABASE_VERSION = 1;
    private String tokenKey, language, type;

    public SettingDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE setting (" +
                " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " key TEXT, value TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS setting");
        onCreate(db);
    }

    public void delete(SQLiteDatabase db) {
        db.execSQL("DELETE FROM setting");
    }

    public String getApplicationToken(SQLiteDatabase db) {
        String sql = "SELECT value FROM " + "setting WHERE key=\"token_key\"";

        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        if (c.getCount() > 0) {
            tokenKey = c.getString(0);
        }

        return tokenKey;
    }

    public String getLanguage(SQLiteDatabase db) {
        String sql = "SELECT value FROM " + "setting WHERE key=\"language\"";

        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        if (c.getCount() > 0) {
            language = c.getString(0);
        }

        return language;
    }

    public String getUserType(SQLiteDatabase db) {
        String sql = "SELECT value FROM " + "setting WHERE key=\"user\"";

        Cursor c = db.rawQuery(sql, null);
        c.moveToFirst();

        if (c.getCount() > 0) {
            type = c.getString(0);
        }

        return type;
    }
}
