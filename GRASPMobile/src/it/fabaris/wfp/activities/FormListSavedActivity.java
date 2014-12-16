/*******************************************************************************
 * Copyright (c) 2012 Fabaris SRL.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Fabaris SRL - initial API and implementation
 ******************************************************************************/
package it.fabaris.wfp.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import content.FormSavedAdapter;
import database.DbAdapterGrasp;

import object.FormInnerListProxy;
import utils.ApplicationExt;
import utils.FormComparator;


import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.provider.FormProviderAPI;
import it.fabaris.wfp.provider.InstanceProviderAPI;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Class that defines the tab for the list of the saved forms
 *
 */
public class FormListSavedActivity extends Activity 
{
	public interface FormListHandlerSaved
	{
		public ArrayList<FormInnerListProxy> getSavedForm();
	}
	public FormListHandlerSaved formListHandler;
	
	private ArrayList<FormInnerListProxy> salvati;
	private ArrayList<FormInnerListProxy> saved;
	
	public int posizione;
	
	private FormSavedAdapter adapter;
	private ListView listview;
	
	
	public static boolean SAVE = false;
	

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabsaved);
		
		
		salvati = new ArrayList<FormInnerListProxy>();		
		salvati = getIntent().getExtras().getParcelableArrayList("saved");
		
		saved = new ArrayList<FormInnerListProxy>();		
		saved = getIntent().getExtras().getParcelableArrayList("salvate");
		
		
		
        listview = (ListView)findViewById(R.id.listViewSaved);
        listview.setCacheColorHint(00000000);
		listview.setClickable(true);


		adapter = new FormSavedAdapter(FormListSavedActivity.this, salvati, saved);
		listview.setAdapter(adapter);
		
		final Builder builder = new AlertDialog.Builder(this);
		
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override	
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
			{
				Context context = getApplicationContext();
				Intent intent = new Intent (context, FormEntryActivity.class);
				String keyIdentifer  = "ciao";
				String keyIdentifer1  = "ciao1";
				String keyIdentifer2  = "ciao2";
				String keyIdentifer3  = "ciao3";
				String keyIdentifer4  = "ciao4";
				
				int positionSalvati = getRightCompletedParcelableObject(saved.get(position).getFormName());//LL per visualizzare la form corretta
				
				String pkgName = getPackageName();
				intent.putExtra(pkgName+keyIdentifer, salvati.get(positionSalvati).getPathForm()); 			//formPathSalvate[position]);
				intent.putExtra(pkgName+keyIdentifer1, salvati.get(positionSalvati).getFormName());			//formNameSalvate[position]);
				intent.putExtra(pkgName+keyIdentifer2, salvati.get(positionSalvati).getFormNameInstance());	//formNameInstanceSalvate[position]);
				intent.putExtra(pkgName+keyIdentifer3, salvati.get(positionSalvati).getFormId()); 				//formIdSalvate[position]);
				intent.putExtra(pkgName+keyIdentifer4, saved.get(position).getIdDataBase()); 			//LLaggiunto 12 perche' e' necessario inviare a form entry idDataBase per poter fare la delite sul db delle salvate per iddbform nel caso in cui si voglia salvare la forma alla fine
																										//dentro a formEntryActivity
				
				Log.i("enumeratorID:"+  saved.get(position).getFormEnumeratorId(),"FormNameInstance" + salvati.get(positionSalvati).getFormNameInstance() );
				
				String action = getIntent().getAction();
				FormEntryActivity.fromHyera = true;
				
				
			    if (Intent.ACTION_PICK.equals(action)) 
			    {
			        setResult(RESULT_OK, new Intent().setData(Uri.parse(salvati.get(positionSalvati).getStrPathInstance())));
			    } 
			   
			    else
			    {	  
				    intent.setAction(Intent.ACTION_EDIT);
				    
				    SAVE = true;
				    
				    String extension = MimeTypeMap.getFileExtensionFromUrl(salvati.get(positionSalvati).getStrPathInstance()).toLowerCase();
					String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
					intent.setDataAndType(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mimeType);
				    startActivity(intent);//chiama formEntry
			    }
			}
		});
		
		listview.setOnItemLongClickListener(new OnItemLongClickListener() 
		{
			public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id)
			{
				posizione = position;
				Log.v("long clicked", "position"+" "+posizione);
				
				builder.setMessage(getString(R.string.delete_form))
					.setCancelable(false)
					.setPositiveButton(R.string.confirm, 
							new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int id) 
						{
							
							int positionSalvati = getRightCompletedParcelableObject(saved.get(position).getFormName());
							ApplicationExt.getDatabaseAdapter().open().delete("SAVED", saved.get(position).getFormName());
							ApplicationExt.getDatabaseAdapter().close();
							
							DatabaseHelper dbh = new DatabaseHelper("forms.db");
							String query = "UPDATE forms SET status='cancelled' WHERE displayNameInstance = '"
									+ salvati.get(positionSalvati).getFormNameInstance()
									+ "' AND status='saved'";
							
							dbh.getWritableDatabase().execSQL(query);
							dbh.close();
							
							File file = new File(salvati.get(positionSalvati).getStrPathInstance());
							boolean deleted = file.delete();
							
							/**
							 * CANCELLAZIONE DELLA CARTELLA TEMPORANEA
							 */
							String path = salvati.get(positionSalvati).getStrPathInstance().replace(".xml", "");
							String folder[] = path.split("/", path.length());
							File f = new File(Environment.getExternalStorageDirectory()+"/GRASP/instances/"+folder[folder.length-1]);
													
							deleteDirectory(f);
							
							if(deleted)
								Toast.makeText(FormListSavedActivity.this, getString(R.string.cancelform) + " " +salvati.get(positionSalvati).getFormName(), Toast.LENGTH_LONG).show();
							finish();
							
							//FormInnerListProxy filp1 = saved.remove(3);   //LL
							//FormInnerListProxy filp2 = salvati.remove(position); //LL   
						}
				})
				.setNegativeButton(getString(R.string.negative_choise),	new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog,	int id)
					{
						dialog.cancel();
					}
				}).show();;
				return false;
			}				
		});
	}
	
	/**
	 * metodo per la cancellazione della directory
	 * @param path
	 * @return
	 */
	public static boolean deleteDirectory(File path)
	{
		if(path.exists())  {
			File[] files = path.listFiles();
			if (files == null)	{
				return true;
			}
			else if (files != null) {
				for(int i=0; i<files.length; i++) {
					if(files[i].isDirectory()) {
						deleteDirectory(files[i]);
					}
					else {
						files[i].delete();
					}
				}
			}
		}
	    return(path.delete());
    }
	
	private ArrayList<FormInnerListProxy> querySavedForm()
	{
		formListHandler = new FormListActivity();
		ArrayList<FormInnerListProxy> nuovi = formListHandler.getSavedForm();
		
		return nuovi;
	}

	public void onResume()
    {
		super.onResume();
		getFormsDataSaved();
    }
	
	private int getRightCompletedParcelableObject(String idFormInFabaris){//prende l'identificativo univoco della form
		
		int posizione = 0;
		//seleziona la posizione nella lista degli oggetti parcellizzati di fabaris che contiene l'id della form collegato all'oggetto parcellizzato cliccato sulla lista 
		//delle complete
		for(int i = 0; i<salvati.size(); i++){
			String prova = salvati.get(i).getFormNameInstance();
			if(salvati.get(i).getFormNameInstance().contains(idFormInFabaris)){
				posizione = i;
			}
		}
		return posizione;//restituisce la posizione dell'oggetto parcellizato fabaris cui fa riferimento la form selezionata nella lista delle complete
	} 
	
	/**
	 * metodo per effettuare la query delle form salvate
	 */
	public void getFormsDataSaved()
	{
		saved.clear();
		//dbAdapter.open();
		Cursor cursor = ApplicationExt.getDatabaseAdapter().open().fetchAllSaved();
        try
        {
	        while (cursor.moveToNext())  
	        { 
	        	/**
	        	 * SAVED_FORM_ID_KEY, SAVED_FORM_NOME_FORM, SAVED_FORM_DATA, SAVED_FORM_BY
	        	 */
	        	FormInnerListProxy saved = new FormInnerListProxy(); 
	        	saved.setIdDataBase(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_KEY)));
	        	saved.setFormId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_SAVED_KEY))); 
	        	saved.setFormName(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_NOME_FORM))); 
	        	saved.setLastSavedDateOn(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_DATA))); 
	        	saved.setFormEnumeratorId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_BY)));  
	        	
	        	this.saved.add(saved); 
	        }
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        ApplicationExt.getDatabaseAdapter().close();//LL added to close the db after the fetch
        Collections.sort(saved, new FormComparator());
        
        runOnUiThread(new Runnable() 
        { 
            public void run()  
            { 
            	adapter.add(saved);
            	adapter.notifyDataSetChanged();
            } 
              
        });
	}
	
	public void onDestroy()
    {
		listview.setAdapter(null);
        super.onDestroy();
    }

}