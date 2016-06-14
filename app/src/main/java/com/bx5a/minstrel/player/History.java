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
import com.bx5a.minstrel.exception.NotInitializedException;
import com.bx5a.minstrel.exception.PageNotAvailableException;
import com.bx5a.minstrel.exception.PlayableCreationException;
import com.bx5a.minstrel.utils.PlayableFactory;
import com.bx5a.minstrel.utils.SearchList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton that contains the history of played song
 */
public class History {
    private static History instance;
    private Context context;
    private int MAX_RESULT_NUMBER = 20;

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
     *
     * @return boolean whether the context is or isn't set
     */
    public boolean isInitialized() {
        return context != null;
    }

    /**
     * get at most getMaximumSize() previously played playable
     * @return the list of playables
     * @throws NotInitializedException if context isn't set
     */
    public SearchList<Playable> get() throws NotInitializedException {
        if (context == null) {
            throw new NotInitializedException("Context needs to be set in History before using it");
        }

        return new HistoryList();
    }

    /**
     * Add a playable to the history
     * @param playable the song to be stored
     * @throws NotInitializedException if context isn't set
     */
    public void store(Playable playable) throws NotInitializedException {
        if (context == null) {
            throw new NotInitializedException("Context needs to be set in History before using it");
        }

        String classType = playable.getClassName();
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

    private class HistoryList implements SearchList<Playable> {
        private long maxResults;
        private int indexOffset;
        private boolean nextPageAvailable;
        public HistoryList() {
            maxResults = MAX_RESULT_NUMBER;
            indexOffset = 0;
            nextPageAvailable = true;
        }
        @Override
        public void setMaxResults(long maxResults) {
            this.maxResults = maxResults;
        }
        @Override
        public boolean hasNextPage() {
            return nextPageAvailable;
        }
        @Override
        public List<Playable> getNextPage() throws IOException {
            if (!hasNextPage()) {
                throw new PageNotAvailableException("No next page available");
            }

            LocalSQLiteOpenHelper helper = new LocalSQLiteOpenHelper(context);
            SQLiteDatabase db = helper.getReadableDatabase();

            // we order from most recent to older
            Cursor cursor = db.query(true, "History",
                    new String[]{"id", "classType", "playableId", "date"},
                    null, null, null, null, "date DESC", String.valueOf(maxResults + indexOffset));

            // skip the first indexOffset elements
            cursor.move(indexOffset);

            // if that page was found and full, we suppose their is another one
            nextPageAvailable = cursor.getCount() == maxResults + indexOffset;

            // retrieve !
            Map<String, List<String>> classOrderedPlayables = new HashMap<>();
            while (cursor.moveToNext()) {
                String stringClass = cursor.getString(cursor.getColumnIndex("classType"));
                String playableId = cursor.getString(cursor.getColumnIndex("playableId"));
                if (!classOrderedPlayables.containsKey(stringClass)) {
                    classOrderedPlayables.put(stringClass, new ArrayList<String>());
                }
                List<String> classList = classOrderedPlayables.get(stringClass);
                classList.add(playableId);
                classOrderedPlayables.put(stringClass, classList);
                indexOffset++;
            }

            cursor.close();
            db.close();

            ArrayList<Playable> playableList = new ArrayList<>();
            for (String key : classOrderedPlayables.keySet()) {
                try {
                    List<Playable> playables = PlayableFactory.getInstance().createList(key,
                            classOrderedPlayables.get(key));
                    playableList.addAll(playables);
                } catch (PlayableCreationException e) {
                    Log.e("History", e.getMessage());
                    e.printStackTrace();
                }
            }

            return playableList;
        }
    }
}
