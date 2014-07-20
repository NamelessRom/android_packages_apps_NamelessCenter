package org.namelessrom.center.database;

/**
 * Represents a database table for common entries like preferences
 */
public class NamelessTable extends BaseTable {

    public static final String TABLE = "nameless";

    public static String createTable() {
        return String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY,%s TEXT,%s TEXT)",
                TABLE, KEY_ID, KEY_NAME, KEY_VALUE);
    }

}
