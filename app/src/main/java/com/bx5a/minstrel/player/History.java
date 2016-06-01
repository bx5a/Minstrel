/*
 * Copyright Guillaume VINCKE 2016
 *
 * This file is part of Minstrel
 *
 * Minstrel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minstrel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Minstrel.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bx5a.minstrel.player;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bx5a.minstrel.LocalSQLiteOpenHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton that contains the history of played song
 */
public class History {
    private static History instance;
    private Context context;
    private int SONG_NUMBER = 20;

    public static History getInstance() {
        if (instance == null) {
            instance = new History();
        }
        return instance;
    }

    public History() {
        context = null;
    }

    /**
     * To operate correctly, the history needs to be initialized with a context
     * If not set, get and store function will throw a NullPointerException
     * @param context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Gives the maximum number of song the get() function will return
     * @return explained size
     */
    public int getMaximumSize() {
        return SONG_NUMBER;
    }

    /**
     * get at most getMaximumSize() previously played playable
     * @return the list of playables
     * @throws NullPointerException if context isn't set
     */
    public List<Playable> get() throws NullPointerException {
        if (context == null) {
            throw new NullPointerException("Context needs to be set in History before using it");
        }

        LocalSQLiteOpenHelper helper = new LocalSQLiteOpenHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        // we order from most recent to older. Get only the first 20 entries
        Cursor cursor = db.query(true, "History",
                new String[]{"id", "classType", "playableId", "date"},
                null, null, null, null, "date DESC", String.valueOf(SONG_NUMBER));

        ArrayList<Playable> playableList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String stringClass = cursor.getString(cursor.getColumnIndex("classType"));
            String playableId = cursor.getString(cursor.getColumnIndex("playableId"));
            try {
                Class<?> className = Class.forName(stringClass);
                Playable playable = (Playable) className.newInstance();
                playable.initFromId(playableId);
                playableList.add(playable);
            } catch (ClassNotFoundException e) {
                Log.i("History", "Class not found : " + stringClass);
            } catch (InstantiationException e) {
                Log.i("History", "Couldn't instantiate " + stringClass + " : " + e.getMessage());
            } catch (IllegalAccessException e) {
                Log.i("History", "Illegal access to " + stringClass + " : " + e.getMessage());
            } catch (IOException e) {
                Log.i("History", "Couldn't init playable " + stringClass + " : " + e.getMessage());
            }
        }

        cursor.close();
        db.close();

        return playableList;
    }

    /**
     * Add a playable to the history
     * @param playable the song to be stored
     * @throws NullPointerException if context isn't set
     */
    public void store(Playable playable) throws NullPointerException {
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
