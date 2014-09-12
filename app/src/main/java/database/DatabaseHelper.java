package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*********************************************************** 
 *
 * QUESTA E' LA CLASSE CHE SI OCCUPA DI CREARE IL DB 
 * PER LA GESTIONE DEI DATI NELLE LISTE 
 *
 */

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "grasp.db";
    private static final int DATABASE_VERSION = 1;

    //Lo statement SQL di creazione del database 
    private static final String DATABASE_CREATE_SAVED_FORM = "CREATE TABLE grasp_saved_form (_id INTEGER PRIMARY KEY AUTOINCREMENT, saved_id TEXT, saved_name_form TEXT, saved_data TEXT, saved_by TEXT)";
    private static final String DATABASE_CREATE_COMPLETED_FORM = "CREATE TABLE grasp_completed_form (_id INTEGER PRIMARY KEY AUTOINCREMENT, completed_id TEXT, completed_nome_form TEXT, completed_data TEXT, completed_by TEXT)";
    private static final String DATABASE_CREATE_SUBMITTED_FORM = "CREATE TABLE grasp_submitted_form (_id INTEGER PRIMARY KEY AUTOINCREMENT, submitted_id TEXT, submitted_nome_form TEXT, completed_data TEXT, submitted_data TEXT, submitted_by TEXT)";

    //costruttore 
    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL("DROP TABLE IF EXISTS grasp_saved_form");
        database.execSQL("DROP TABLE IF EXISTS grasp_completed_form");
        database.execSQL("DROP TABLE IF EXISTS grasp_submitted_form");

        database.execSQL(DATABASE_CREATE_SAVED_FORM);
        database.execSQL(DATABASE_CREATE_COMPLETED_FORM);
        database.execSQL(DATABASE_CREATE_SUBMITTED_FORM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        database.execSQL("DROP TABLE IF EXISTS grasp_saved_form");
        database.execSQL("DROP TABLE IF EXISTS grasp_completed_form");
        database.execSQL("DROP TABLE IF EXISTS grasp_submitted_form");

        onCreate(database);
    }
} 