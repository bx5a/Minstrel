package com.bx5a.minstrel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by guillaume on 21/04/2016.
 */
public class LocalSQLiteOpenHelper extends SQLiteOpenHelper {
    public LocalSQLiteOpenHelper(Context context) {
        super(context, "minstrel.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlFilTable = "CREATE TABLE History(id INTEGER PRIMARY KEY, classType STRING, playableId STRING, date INTEGER)";
        db.execSQL(sqlFilTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i < newVersion; i++) {
            int versionToUpdate = i+1;
            if (versionToUpdate == 2) {
                // update to version 2
            }
        }
    }
}
