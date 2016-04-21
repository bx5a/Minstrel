package com.bx5a.minstrel.player;

import android.content.ContentValues;
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
    private static History instance;
    private Context context;

    public static History getInstance() {
        if (instance == null) {
            instance = new History();
        }
        return instance;
    }

    public History() {
        context = null;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<Playable> get() throws NullPointerException {
        if (context == null) {
            throw new NullPointerException("Context needs to be set in History before using it");
        }

        LocalSQLiteOpenHelper helper = new LocalSQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(true, "History",
                new String[]{"id", "classType", "playableId", "date"},
                null, null, null, null, "date", null);

        ArrayList<Playable> playableList = new ArrayList<>();
        while(cursor.moveToNext()) {
            String stringClass = cursor.getString(cursor.getColumnIndex("classType"));
            String playableId = cursor.getString(cursor.getColumnIndex("playableId"));
            try {
                Class<?> className = Class.forName(stringClass);
                Playable playable = (Playable) className.newInstance();
                playable.initFromId(playableId, context);
                playableList.add(playable);
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

        return playableList;
    }

    public void store(Playable playable)  throws NullPointerException {
        if (context == null) {
            throw new NullPointerException("Context needs to be set in History before using it");
        }

        String classType = playable.getClass().getName();
        String id = playable.getId();

        LocalSQLiteOpenHelper helper = new LocalSQLiteOpenHelper(context);
        SQLiteDatabase readableDb = helper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("classType", classType);
        values.put("playableId", id);
        values.put("date", System.currentTimeMillis());

        String where = "classType=\"" + classType + "\" and playableId=\"" + id + "\"";
        boolean entryExists = exists(readableDb, where);
        readableDb.close();

        SQLiteDatabase writableDb = helper.getWritableDatabase();
        if (entryExists) {
            writableDb.update("History", values, where, null);
        } else {
            writableDb.insert("History", null, values);
        }
        writableDb.close();
    }

    private boolean exists(SQLiteDatabase db, String where) {
        Cursor cursor = db.query(true, "History", new String[]{"id"}, where, null, null, null, "date", null);

        boolean exists = (cursor.getCount() != 0);

        cursor.close();
        db.close();
        return exists;
    }
}
