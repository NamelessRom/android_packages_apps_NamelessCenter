package org.namelessrom.center.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;

import org.namelessrom.center.Logger;
import org.namelessrom.center.items.UpdateInfo;

import java.util.ArrayList;

/**
 * Represents a database table for common entries like preferences
 */
public class UpdateTable extends BaseTable {

    public static final String TABLE = "updates";

    public static String createTable() {
        return String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY,%s TEXT,%s TEXT);",
                TABLE, KEY_ID, KEY_NAME, KEY_VALUE);
    }

    public static synchronized ArrayList<UpdateInfo> getAll() {
        final ArrayList<UpdateInfo> updateInfos = new ArrayList<UpdateInfo>();

        if (DatabaseHandler.getDatabase() == null) {
            return updateInfos;
        }

        final Cursor cursor = DatabaseHandler.getDatabase().query(TABLE, new String[]{KEY_VALUE},
                KEY_NAME + "=?", new String[]{name}, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        if (cursor == null) {
            return updateInfos;
        }

        if (!cursor.moveToFirst()) {
            return updateInfos;
        }

        return updateInfos;
    }

    public static synchronized UpdateInfo getValueByName(final String name) {
        if (DatabaseHandler.getDatabase() == null) return null;

        final Cursor cursor = DatabaseHandler.getDatabase().query(TABLE, new String[]{KEY_VALUE},
                KEY_NAME + "=?", new String[]{name}, null, null, null, null);
        if (cursor != null) { cursor.moveToFirst(); }
        if (cursor == null) return null;

        final String result = ((cursor.getCount() <= 0)
                ? null : cursor.getString(cursor.getColumnIndex(KEY_VALUE)));
        Logger.v(UpdateTable.class, result);

        cursor.close();
        return (result == null ? new UpdateInfo() : new Gson().fromJson(result, UpdateInfo.class));
    }

    public static synchronized boolean insertOrUpdate(final String name, final UpdateInfo info) {
        if (DatabaseHandler.getDatabase() == null) return false;

        final String value = new Gson().toJson(info);

        final ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_VALUE, value);
        Logger.v(UpdateTable.class, String.format("name: %s | value: %s", name, value));

        DatabaseHandler.getDatabase().delete(TABLE, KEY_NAME + " = ?", new String[]{name});
        DatabaseHandler.getDatabase().insert(TABLE, null, values);
        return true;
    }

    public static synchronized boolean deleteItemByName(final String name) {
        if (DatabaseHandler.getDatabase() == null) return false;

        DatabaseHandler.getDatabase().delete(TABLE, KEY_NAME + " = ?", new String[]{name});
        return true;
    }

}
