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

package com.bx5a.minstrel;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bx5a.minstrel.utils.MinstrelApplication;

public class LocalSQLiteOpenHelper extends SQLiteOpenHelper {
    public LocalSQLiteOpenHelper() {
        super(MinstrelApplication.getContext(), "minstrel.db", null, 1);
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
