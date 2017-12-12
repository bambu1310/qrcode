package example.zxing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ibelieveican on 2015/7/7.
 */
public class FavoriteDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favorite";
    private static final int DATABASE_VERSION = 1;
    public FavoriteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE favorite (" +
                " _id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " code TEXT, content TEXT, created_at DATETIME)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS favorite");
        onCreate(db);
    }

    public void clear(SQLiteDatabase db) {
        db.execSQL("DELETE FROM favorite");
    }
}
