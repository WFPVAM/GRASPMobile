package it.fabaris.wfp.provider;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.database.ODKSQLiteOpenHelper;
import it.fabaris.wfp.provider.MessageProviderAPI.MessageColumns;

/**
 * Class that defines the database that hold the new form
 *
 */

public class MessageProvider extends ContentProvider {

    public static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "message.db";
    public static final String FORMS_TABLE_NAME = "message";
    public static final String t = "MessageProvider";
    private static HashMap<String, String> sMessageProjectionMap;
    private static final UriMatcher sUriMatcher;
    private static final int MESSAGE = 1;
    private static final int MESSAGE_ID = 2;

    public static class DatabaseHelper extends ODKSQLiteOpenHelper{
        public DatabaseHelper(String databaseName) {
            super(Collect.METADATA_PATH, databaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + FORMS_TABLE_NAME + " ("
                    + MessageColumns._ID + " integer primary key autoincrement, "
                    + MessageColumns.FORM_ID + " text, "
                    + MessageColumns.FORM_NAME + " text, "
                    + MessageColumns.FORM_IMPORTED + " text, "
                    + MessageColumns.FORM_ENCODED_TEXT + " text, "
                    + MessageColumns.FORM_TEXT + " text, "
                    + MessageColumns.DATE + " text);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(t, "Upgrading database from version " + oldVersion + " to " + newVersion
                    + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS forms");
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            // id = SQLiteDatabase.NO_LOCALIZED_COLLATORS;
        }
    }
    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(DATABASE_NAME);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(FORMS_TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case MESSAGE:
                qb.setProjectionMap(sMessageProjectionMap);
                break;

            case MESSAGE_ID:
                qb.setProjectionMap(sMessageProjectionMap);
                qb.appendWhere(MessageColumns._ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /**
         *  Get the database and run the query
         */
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        /**
         *  Tell the cursor what uri to watch, so it knows when its source data changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MESSAGE:
                return MessageColumns.CONTENT_TYPE;

            case MESSAGE_ID:
                return MessageColumns.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(FormProviderAPI.AUTHORITY, "message", MESSAGE);
        sUriMatcher.addURI(FormProviderAPI.AUTHORITY, "message/#", MESSAGE_ID);
        sMessageProjectionMap = new HashMap<String, String>();
        sMessageProjectionMap.put(MessageColumns._ID, MessageColumns._ID);
        sMessageProjectionMap.put(MessageColumns.FORM_ID, MessageColumns.FORM_ID);
        sMessageProjectionMap.put(MessageColumns.FORM_NAME, MessageColumns.FORM_NAME);
        sMessageProjectionMap.put(MessageColumns.FORM_IMPORTED, MessageColumns.FORM_IMPORTED);
        sMessageProjectionMap.put(MessageColumns.FORM_TEXT, MessageColumns.FORM_TEXT);
        sMessageProjectionMap.put(MessageColumns.FORM_ENCODED_TEXT, MessageColumns.FORM_ENCODED_TEXT);
        sMessageProjectionMap.put(MessageColumns.DATE, MessageColumns.DATE);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
