package com.bx5a.minstrel.player;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bx5a.minstrel.LocalSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guillaume on 21/04/2016.
 */
public class History {
    public static List<Playable> get(Context context) {
        LocalSQLiteOpenHelper helper = new LocalSQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(true, "History",
                new String[]{"id", "classType", "playableId", "date"},
                null, null, null, null, "date", null);

        ArrayList<Playable> playables = new ArrayList<>();
        while(cursor.moveToNext()) {
            String stringClass = cursor.getString(cursor.getColumnIndex("classType"));
            String playableId = cursor.getString(cursor.getColumnIndex("playableId"));
            try {
                Class<?> className = Class.forName(stringClass);
                Playable playable = (Playable) className.newInstance();
                playable.initFromId(playableId, context);
                playables.add(playable);
            } catch (ClassNotFoundException e) {
                Log.i("History", "Class not found : " + stringClass);
            } catch (InstantiationException e) {
                Log.i("History", "Couldn't instantiate " + stringClass + " : " + e.getMessage());
            } catch (IllegalAccessException e) {
                Log.i("History", "Illegal access to " + stringClass + " : " + e.getMessage());
            }
        }

        cursor.close();
        db.close();

        return playables;
    }

    public static void store(Playable playable) {
        // TODO: develop !
    }
}
