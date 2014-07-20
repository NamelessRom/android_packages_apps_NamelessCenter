package org.namelessrom.center.database;

/**
 * A base table to extend from
 */
public abstract class BaseTable {

    protected static final String TABLE = "base";

    protected static final String KEY_ID    = "id";
    protected static final String KEY_NAME  = "name";
    protected static final String KEY_VALUE = "value";

    public static String dropTable() { return String.format("DROP TABLE IF EXISTS %s", TABLE); }

}
