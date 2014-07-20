/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.center.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.namelessrom.center.AppInstance;
import org.namelessrom.center.Logger;

import java.io.File;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int    DATABASE_VERSION = 2;
    private static final String DATABASE_NAME    = "NamelessCenter.db";

    public static final String DB_DOWNGRADE = ".dbdowngrade";

    private static DatabaseHandler sDatabaseHandler = null;
    private static SQLiteDatabase sDb;

    private DatabaseHandler(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        sDb = getWritableDatabase();
    }

    public static synchronized DatabaseHandler getInstance() {
        if (sDatabaseHandler == null) {
            sDatabaseHandler = new DatabaseHandler(AppInstance.applicationContext);
        }
        return sDatabaseHandler;
    }

    public static synchronized void tearDown() {
        Logger.i(DatabaseHandler.class, "tearDown()");
        closeDatabase();
        sDatabaseHandler = null;
    }

    public static synchronized SQLiteDatabase getDatabase() {
        if (sDb == null) sDb = getInstance().getWritableDatabase();
        return sDb;
    }

    public static synchronized void closeDatabase() {
        if (sDb != null) {
            sDb.close();
            sDb = null;
        }
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL(NamelessTable.createTable());
        db.execSQL(UpdateTable.createTable());
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Logger.i(this, "onUpgrade"
                + " | oldVersion: " + String.valueOf(oldVersion)
                + " | newVersion: " + String.valueOf(newVersion));
        int currentVersion = oldVersion;

        if (currentVersion < 1) {
            db.execSQL(NamelessTable.dropTable());
            db.execSQL(NamelessTable.createTable());
            currentVersion = 1;
        }

        if (currentVersion < 2) {
            db.execSQL(UpdateTable.dropTable());
            db.execSQL(UpdateTable.createTable());
            currentVersion = 2;
        }

        if (currentVersion != DATABASE_VERSION) {
            wipeDb(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.i(this, "onDowngrade"
                + " | oldVersion: " + String.valueOf(oldVersion)
                + " | newVersion: " + String.valueOf(newVersion));
        wipeDb(db);
        try {
            Logger.v(this, String.format("creating downgrade file: %s",
                    new File(AppInstance.getFilesDirectory() + DB_DOWNGRADE).createNewFile()));
        } catch (Exception ignored) { }
    }

    private void wipeDb(final SQLiteDatabase db) {
        db.execSQL(NamelessTable.dropTable());
        onCreate(db);
    }

    //==============================================================================================
    // All CRUD(Create, Read, Update, Delete) Operations
    //==============================================================================================

    public String getValueByName(final String name, final String tableName) {
        if (sDb == null) return null;

        final Cursor cursor = sDb.query(tableName, new String[]{BaseTable.KEY_VALUE},
                BaseTable.KEY_NAME + "=?", new String[]{name}, null, null, null, null);
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        final String result = ((cursor.getCount() <= 0)
                ? null : cursor.getString(cursor.getColumnIndex(BaseTable.KEY_VALUE)));

        cursor.close();
        return result;
    }

    public boolean insertOrUpdate(final String name, final String value, final String tableName) {
        if (sDb == null) return false;

        final ContentValues values = new ContentValues();
        values.put(BaseTable.KEY_NAME, name);
        values.put(BaseTable.KEY_VALUE, value);

        sDb.delete(tableName, BaseTable.KEY_NAME + " = ?", new String[]{name});
        sDb.insert(tableName, null, values);
        return true;
    }

    public boolean deleteItemByName(final String name, final String tableName) {
        if (sDb == null) return false;

        sDb.delete(tableName, BaseTable.KEY_NAME + " = ?", new String[]{name});
        return true;
    }

}
