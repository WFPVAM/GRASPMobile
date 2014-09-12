package database;

import java.util.ArrayList; 
import java.util.List; 

import object.FormInnerListProxy;
   
  
import android.content.ContentValues; 
import android.content.Context; 
import android.database.Cursor; 
import android.database.SQLException; 
import android.database.sqlite.SQLiteDatabase; 
import android.util.Log; 
import android.view.LayoutInflater.Filter; 
  
/*********************************************************** 
 *  *  
 * QUESTA E' LA CLASSE INTERFACCIA CHE SI OCCUPA DI MANIPOLARE  
 * IL DB DEI CONTENUTI. ESSA CONTIENE TUTTI I METODI PER EFFETTUARE 
 * LE INTERROGAZIONI E GLI INSERIMENTI 
 * 
 */
  
public class DbAdapterGrasp  
{ 
    private Context context; 
    private SQLiteDatabase database; 
    private DatabaseHelper dbHelper; 
      
    //DATABASE_CREATE_SAVED_FORM = "CREATE TABLE grasp_saved_form (_id INTEGER PRIMARY KEY AUTOINCREMENT, nome_form TEXT, saved_data TEXT, saved_by TEXT)"; 
    //DATABASE_CREATE_COMPLETED_FORM = "CREATE TABLE grasp_completed_form (_id INTEGER PRIMARY KEY AUTOINCREMENT, nome_form TEXT, completed_data TEXT)"; 
    //DATABASE_CREATE_SUBMITTED_FORM = "CREATE TABLE grasp_submitted_form (_id INTEGER PRIMARY KEY AUTOINCREMENT, nome_form TEXT, completed_data TEXT, submitted_data TEXT)"; 
        
    //Campi del database 
    private static final String DATABASE_CREATE_SAVED_FORM = "grasp_saved_form";  
    public static final String SAVED_FORM_ID_KEY = "_id"; 
	public static final String SAVED_FORM_ID_SAVED_KEY = "saved_id"; 
    public static final String SAVED_FORM_NOME_FORM = "saved_name_form"; 
    public static final String SAVED_FORM_DATA = "saved_data"; 
    public static final String SAVED_FORM_BY = "saved_by"; 
    
    public static final String DATABASE_CREATE_COMPLETED_FORM = "grasp_completed_form"; 
    public static final String COMPLETED_FORM_ID_KEY = "_id"; 
    public static final String COMPLETED_FORM_ID_COMPLETED_KEY = "completed_id"; 
    public static final String COMPLETED_FORM_NOME_FORM = "completed_nome_form"; 
    public static final String COMPLETED_FORM_DATA = "completed_data"; 
    public static final String COMPLETED_FORM_BY = "completed_by"; 

    private static final String DATABASE_CREATE_SUBMITTED_FORM = "grasp_submitted_form";  
    public static final String SUBMITTED_FORM_ID_KEY = "_id"; 
    public static final String SUBMITTED_FORM_ID_SUBMITTED_KEY = "submitted_id"; 
    public static final String SUBMITTED_FORM_NOME_FORM = "submitted_nome_form"; 
    public static final String SUBMITTED_FORM_SUBMITTED_DATA = "submitted_data"; 
    public static final String SUBMITTED_FORM_COMPLETED_DATA = "completed_data"; 
    public static final String SUBMITTED_FORM_BY = "submitted_by"; 
          
    //ISTANZIO IL DB  
    public DbAdapterGrasp(Context context) 
    { 
        this.context = context;
        
        dbHelper = new DatabaseHelper(context); 
        database = dbHelper.getWritableDatabase(); 
        //return this; 
    } 
    public DbAdapterGrasp open() throws SQLException //il database su cui agiamo � leggibile/scrivibile 
    {   
        dbHelper = new DatabaseHelper(context); 
        database = dbHelper.getWritableDatabase(); 
        return this; 
    } 
    public void close() 
    { 
        dbHelper.close(); 
    } 
      

    //CANCELLAZIONE 
    public boolean delete(String formStatus, String filter)
    {
    	if(formStatus.equals("SAVED")){ 
    		
    		
    		
    		//////////////////////////////////-------------------------
    		Cursor cursor = fetchAllSaved();
    		while (cursor.moveToNext())  
	        { 
	        	/**
	        	 * SAVED_FORM_ID_KEY, SAVED_FORM_NOME_FORM, SAVED_FORM_DATA, SAVED_FORM_BY
	        	 */
	        	
    			String idDataBase = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_KEY)));
	        	String formID = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_SAVED_KEY)));
	        	String formname = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_NOME_FORM))); 
	        	String form_data = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_DATA)));   
	        	String formby = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_BY)));  
	        	//String riga = "formID:"+formID + ";  formname:" +formname+ ";  form_data:"+form_data+ ";  formby:"+formby;
	        	String riga = "idDataBase: "+ idDataBase + ",     formby:"+formby;
	        	Log.i("rigaSavedPrimadellaDelete:",riga);
	        }
    		////-----------------------------------------------------------------------
    		
    		//database.delete(DATABASE_CREATE_SAVED_FORM, SAVED_FORM_ID_KEY + "=" +"'"+ filter +"'", null) > 0; //LLaggiunto questo statement solo per fare i test
    		boolean risultatoquery = database.delete(DATABASE_CREATE_SAVED_FORM, SAVED_FORM_NOME_FORM + "=" +"'"+ filter +"'", null) > 0; 
    		//return database.delete(DATABASE_CREATE_SAVED_FORM, SAVED_FORM_ID_KEY + "=" +"'"+ filter +"'", null) > 0; //LLaggiunto cambiato il filtro per la delite questo e' quello giusto da rimettere questo statement dopo i test
    		
    		
			//////////////////////////////////-------------------------
			cursor = fetchAllSaved();
			while (cursor.moveToNext())  
			{ 
			/**
			* SAVED_FORM_ID_KEY, SAVED_FORM_NOME_FORM, SAVED_FORM_DATA, SAVED_FORM_BY
			*/
			
			String idDataBase = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_KEY)));
			String formID = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_SAVED_KEY)));
			String formname = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_NOME_FORM))); 
			String form_data = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_DATA)));   
			String formby = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_BY)));  
			//String riga = "formID:"+formID + ";  formname:" +formname+ ";  form_data:"+form_data+ ";  formby:"+formby;
			String riga = "idDataBase: "+ idDataBase + ",     formby:"+formby;
			Log.i("rigaSavedDopodellaDelete:",riga);
			}
			////-----------------------------------------------------------------------
    		
			database.close();
			return risultatoquery;
			
    		
        }
        else if(formStatus.equals("COMPLETED")) 
        { 
            boolean risultatoquery = database.delete(DATABASE_CREATE_COMPLETED_FORM, COMPLETED_FORM_NOME_FORM + "=" +"'"+ filter +"'", null) > 0; 
            database.close();
            return risultatoquery;
        }else{
        	database.close();
        	return false;
        }
		 
    }   
    
    //CANCELLAZIONE PER POSIZIONE
    public boolean delete(int position)  
    {
    	boolean resultquery = database.delete(DATABASE_CREATE_SAVED_FORM, SAVED_FORM_ID_KEY + "=" +"'"+ position +"'", null) > 0; 
    	database.close();
    	return  resultquery;
    }
      
    //INSERIMENTO FORM SALVATE
    public void insert(String filter, String id, String nome_form, String data, String by)  
    { 
        if(filter == "SAVED")
        { 
        	String saved_id = id;
        	String saved_nome_form = nome_form;
        	String saved_data = data;
        	String saved_by = by;
        	ContentValues initialValues = createFormItemsSavedValues(saved_id, saved_nome_form, saved_data, saved_by); 
        	database.insertOrThrow(DATABASE_CREATE_SAVED_FORM, null, initialValues);
        }
        else if(filter == "COMPLETED") 
        { 
        	String completed_id = id;
        	String completed_nome_form = nome_form;
        	String completed_data = data;
        	String completed_by = by;
        	ContentValues initialValues = createFormItemsCompletedValues(completed_id, completed_nome_form, completed_data, completed_by);
        	database.insertOrThrow(DATABASE_CREATE_COMPLETED_FORM, null, initialValues);
        }
        else if(filter == "SUBMITTED") 
        { 
        	String submitted_id = id;
          	String submitted_nome_form = nome_form;
            String completed_data = "";
            String submitted_data = data;
            String submitted_by = by;
            ContentValues initialValues = createFormItemsSubmittedValues(submitted_id, submitted_nome_form, completed_data, submitted_data, submitted_by);
        	database.insertOrThrow(DATABASE_CREATE_SUBMITTED_FORM, null, initialValues);
        }  
        return; 
    } 
    
    //----------------------------------------------------------------------------------------------------------------------
  
    private static ContentValues createFormItemsSavedValues(String saved_id, String saved_nome_form, String saved_data, String saved_by) 
    { 
        ContentValues values = new ContentValues();  
        values.put(SAVED_FORM_ID_SAVED_KEY, saved_id);
        values.put(SAVED_FORM_NOME_FORM, saved_nome_form); 
        values.put(SAVED_FORM_DATA, saved_data); 
        values.put(SAVED_FORM_BY, saved_by); 
        Log.e("riga inseritaSaved", (saved_id + " " +saved_nome_form + " " + saved_data + " " + saved_by).toString());  
        
        return values; 
    }   
    
    private static ContentValues createFormItemsCompletedValues(String completed_id, String completed_nome_form, String completed_data, String completed_by) 
    { 
        ContentValues values = new ContentValues();  
        values.put(COMPLETED_FORM_ID_COMPLETED_KEY, completed_id);
        values.put(COMPLETED_FORM_NOME_FORM, completed_nome_form); 
        values.put(COMPLETED_FORM_DATA, completed_data); 
        values.put(COMPLETED_FORM_BY, completed_by); 
        Log.e("riga inseritaCompleted", (completed_id + " " + completed_nome_form + " " + completed_data + " " + completed_by).toString());
        
        return values; 
    }
    
    private static ContentValues createFormItemsSubmittedValues(String submitted_id, String submitted_nome_form, String completed_data, String submitted_data, String submitted_by) 
    { 
    	ContentValues values = new ContentValues();
    	values.put(SUBMITTED_FORM_ID_SUBMITTED_KEY, submitted_id);
        values.put(SUBMITTED_FORM_NOME_FORM, submitted_nome_form); 
        values.put(SUBMITTED_FORM_SUBMITTED_DATA, submitted_data); 
        values.put(SUBMITTED_FORM_BY, submitted_by); 
        Log.e("riga inseritaSubmitted", (submitted_id + " " + submitted_nome_form + " " + submitted_data + " " + submitted_by).toString());  
        
        return values; 
    }
    
      
    //QUERY CONDIZIONATE
    public Cursor get(String filter, String choise)  
    { 
        if(filter == "SAVED") 
        { 
        	String query = "SELECT _id, saved_nome_form, saved_data, submitted_by FROM grasp_saved_form WHERE saved_id = ?"; 
            String[] selectionArgs = { choise }; 
            Cursor mCursor = database.rawQuery(query, selectionArgs);  
            return mCursor;
        } 
        else if(filter == "COMPLETED") 
        { 
        	String query = "SELECT _id, completed_nome_form, completed_data, completed_by FROM grasp_completed_form WHERE completed_id = ?"; 
            String[] selectionArgs = { choise }; 
            Cursor mCursor = database.rawQuery(query, selectionArgs);  
            return mCursor;
        }
        else if(filter == "SUBMITTED") 
        { 
        	String query = "SELECT _id, submitted_nome_form, submitted_data, completed_data, submitted_by FROM grasp_submitted_form WHERE submitted_id = ?"; 
            String[] selectionArgs = { choise }; 
            Cursor mCursor = database.rawQuery(query, selectionArgs);  
            return mCursor; 
        }
        return null; 
    } 
    
    //QUERY DI TUTTO 
    public Cursor fetchAllSaved()  
    { 
        return database.query(DATABASE_CREATE_SAVED_FORM, new String[] {SAVED_FORM_ID_KEY, SAVED_FORM_ID_SAVED_KEY, SAVED_FORM_NOME_FORM, SAVED_FORM_DATA, SAVED_FORM_BY}, null, null, null, null, null); 
    }
    
    public Cursor fetchAllCompleted()  
    { 
    	
    	Cursor cursor = database.query(DATABASE_CREATE_COMPLETED_FORM, new String[] {COMPLETED_FORM_ID_KEY, COMPLETED_FORM_ID_COMPLETED_KEY, COMPLETED_FORM_NOME_FORM, COMPLETED_FORM_DATA, COMPLETED_FORM_BY}, null, null, null, null, null);
    	
    	//return database.query(DATABASE_CREATE_COMPLETED_FORM, new String[] {COMPLETED_FORM_ID_KEY, COMPLETED_FORM_ID_COMPLETED_KEY, COMPLETED_FORM_NOME_FORM, COMPLETED_FORM_DATA, COMPLETED_FORM_BY}, null, null, null, null, null);
    	
    	return cursor;
    	
    }
    
    public Cursor fetchAllSubmitted()  
    { 
        return database.query(DATABASE_CREATE_SUBMITTED_FORM, new String[] {SUBMITTED_FORM_ID_KEY, SUBMITTED_FORM_ID_SUBMITTED_KEY, SUBMITTED_FORM_NOME_FORM, SUBMITTED_FORM_SUBMITTED_DATA, SUBMITTED_FORM_COMPLETED_DATA, SUBMITTED_FORM_BY}, null, null, null, null, SUBMITTED_FORM_ID_KEY+" DESC");
    }
    
    /*
    //LL 20-03-2014 DA TESTARE aggiunta per gestire le form di test 
    //CANCELLAZIONE FORM DI TEST
    public boolean deleteTestFormFromGRASPDb(String nomeForm){
    	//questo metodo e' stato implementato per poter cancellare dal questo database qualunque traccia di una form di test quando questa viene sincronizzata
    	//per la seconda volta
    	String deletequerysaved = "DELETE grasp_saved_form WHERE saved_name_form like '" + nomeForm +"_%'";
    	database.execSQL(deletequerysaved);
    	String deletequerycompleted = "DELETE grasp_completed_form WHERE completed_name_form like '" + nomeForm +"_%'";
    	database.execSQL(deletequerycompleted);
    	String deletequerysubmitted = "DELETE grasp_submitted_form WHERE submitted_name_form like '" + nomeForm +"_%'";
    	database.execSQL(deletequerysubmitted);
    	return true;
    }*/
} 