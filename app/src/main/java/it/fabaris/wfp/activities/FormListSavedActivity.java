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

import android.app.Activity;
import android.app.AlertDialog;
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
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import content.FormSavedAdapter;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.provider.InstanceProviderAPI;
import object.FormInnerListProxy;

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
		
		/*LL 14-05-2014 eliminato per dismissione del db grasp
		salvati = new ArrayList<FormInnerListProxy>();		
		salvati = getIntent().getExtras().getParcelableArrayList("saved");
		*/

        saved = new ArrayList<FormInnerListProxy>();
        //saved = getIntent().getExtras().getParcelableArrayList("salvate");LL 14-05-2014 modificato per dismissione del db grasp
        saved = getIntent().getExtras().getParcelableArrayList("saved");//LL 14-05-2014 modificato per dismissione del db grasp


        listview = (ListView)findViewById(R.id.listViewSaved);
        listview.setCacheColorHint(00000000);
        listview.setClickable(true);


        //adapter = new FormSavedAdapter(FormListSavedActivity.this, salvati, saved);
        adapter = new FormSavedAdapter(FormListSavedActivity.this, saved);//LL 14-05-2014 eliminato per dismissione del db grasp
        listview.setAdapter(adapter);

        final Builder builder = new AlertDialog.Builder(this);

        /**
         * When the user clicks on one of the items in the saved forms list.
         * FormEntryActivity is called and the user can continue compiling
         * the form.
         */
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

                //int positionSalvati = getRightCompletedParcelableObject(saved.get(position).getFormName());//LL per visualizzare la form corretta //LL 14-05-2014 eliminato per dismissione del db grasp

                String pkgName = getPackageName();
				String formID= saved.get(position).getFormId();
				/* LL 14-05-2014 modificati per dismissione del db grasp
				intent.putExtra(pkgName+keyIdentifer, salvati.get(positionSalvati).getPathForm()); 			//formPathSalvate[position]);
				intent.putExtra(pkgName+keyIdentifer1, salvati.get(positionSalvati).getFormName());			//formNameSalvate[position]);
				intent.putExtra(pkgName+keyIdentifer2, salvati.get(positionSalvati).getFormNameInstance());	//formNameInstanceSalvate[position]);
				intent.putExtra(pkgName+keyIdentifer3, salvati.get(positionSalvati).getFormId()); 				//formIdSalvate[position]);
				*/

                //LL 14-05-2014 modificati per dismissione del db grasp
                intent.putExtra(pkgName+keyIdentifer,  saved.get(position).getPathForm()); 			//formPathSalvate[position]);
                intent.putExtra(pkgName+keyIdentifer1, saved.get(position).getFormName());			//formNameSalvate[position]);
                intent.putExtra(pkgName+keyIdentifer2, saved.get(position).getFormNameInstance());	//formNameInstanceSalvate[position]);
                intent.putExtra(pkgName+keyIdentifer3, saved.get(position).getFormId()); 				//formIdSalvate[position]);


                intent.putExtra(pkgName+keyIdentifer4, saved.get(position).getIdDataBase()); 			//LLaggiunto 12 perche' e' necessario inviare a form entry idDataBase per poter fare la delite sul db delle salvate per iddbform nel caso in cui si voglia salvare la forma alla fine
                //dentro a formEntryActivity

                Log.i("enumeratorID:"+  saved.get(position).getFormEnumeratorId(),"FormNameInstance" + saved.get(position).getFormNameInstance() );

                String action = getIntent().getAction();
                FormEntryActivity.fromHyera = true;


                if (Intent.ACTION_PICK.equals(action))
                {
                    //setResult(RESULT_OK, new Intent().setData(Uri.parse(salvati.get(positionSalvati).getStrPathInstance()))); LL 14-05-2014 modificata per dismissione del db grasp
                    setResult(RESULT_OK, new Intent().setData(Uri.parse(saved.get(position).getStrPathInstance()))); //LL 14-05-2014 modificata per dismissione del db grasp
                }

                else
                {
                    intent.setAction(Intent.ACTION_EDIT);

                    SAVE = true;

                    String extension = MimeTypeMap.getFileExtensionFromUrl(saved.get(position).getStrPathInstance()).toLowerCase();
                    String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    intent.setDataAndType(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mimeType);
                    startActivity(intent);//chiama formEntry
                }
            }
        });

        /**
         * when a user do a long click on one of the items of the saved forms list.
         * The form is delete, but before that a dialog is displayed to make
         * sure about the real intentions of the user
         * on click "yes" button, in the db, the form's state passes from saved to cancelled.
         * The form will not be displayed as saved anymore, and the xml containing the answers is deleted.
         * on click "no" button the dialog is simply closed
         */
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
							/*LL 14-05-2014 eliminato per dismissione del db grasp
							//int positionSalvati = getRightCompletedParcelableObject(saved.get(position).getFormName()); LL eliminato per dismissione del db grasp
							ApplicationExt.getDatabaseAdapter().open().delete("SAVED", saved.get(position).getFormName());
							ApplicationExt.getDatabaseAdapter().close();
							*/
                                        dialog.dismiss();
                                        DatabaseHelper dbh = new DatabaseHelper("forms.db");
                                        String query = "UPDATE forms SET status='cancelled' WHERE displayNameInstance = '"
                                                + saved.get(position).getFormNameInstance()
                                                + "' AND status='saved'";

                                        dbh.getWritableDatabase().execSQL(query);
                                        dbh.close();

                                        File file = new File(saved.get(position).getStrPathInstance());
                                        boolean deleted = file.delete();

                                        /**
                                         * CANCELLAZIONE DELLA CARTELLA TEMPORANEA
                                         */
                                        String path = saved.get(position).getStrPathInstance().replace(".xml", "");
                                        String folder[] = path.split("/", path.length());
                                        File f = new File(Environment.getExternalStorageDirectory()+"/GRASP/instances/"+folder[folder.length-1]);

                                        deleteDirectory(f);

                                        if(deleted)
                                        Toast.makeText(FormListSavedActivity.this, getString(R.string.cancelform) + " " +saved.get(position).getFormName(), Toast.LENGTH_LONG).show();
                                        finish();

                                        //FormInnerListProxy filp1 = saved.remove(3);   //LL
                                        //FormInnerListProxy filp2 = salvati.remove(position); //LL
                                    }
                                })
                        .setNegativeButton(getString(R.string.negative_choise),	new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog,	int id)
                            {
                                dialog.dismiss();
                            }
                        }).show();
                return false;
            }
        });
    }

    /**
     * to delete a directory
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

    /**
     * not in use
     * @return
     */
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
	
	
	/*
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
	*/

    /**
     * Once fetched all saved forms from forms db, initializes
     * the global field  "saved" that represents the complete list of saved forms
     */
    public void getFormsDataSaved()
    {

        //LL 14-05-2014 aggiunta per dismissione del db grasp
        saved.clear();
        //dbAdapter.open();
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,instanceFilePath,displayNameInstance,displaySubtext,jrFormId,date,enumeratorID,formNameAndXmlFormid  FROM forms WHERE status = 'saved' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);;
        try
        {
            while (c.moveToNext())
            {
                /**
                 * SAVED_FORM_ID_KEY, SAVED_FORM_NOME_FORM, SAVED_FORM_DATA, SAVED_FORM_BY
                 */
                FormInnerListProxy saved = new FormInnerListProxy();
                saved = new FormInnerListProxy();
                saved.setPathForm(c.getString(0));
                saved.setFormName(c.getString(1));
                saved.setStrPathInstance(c.getString(2));
                saved.setFormNameInstance(c.getString(3));
                saved.setFormNameAutoGen(c.getString(4));
                saved.setFormId(c.getString(5));
                saved.setLastSavedDateOn(c.getString(6));   //LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                saved.setFormEnumeratorId(c.getString(7)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                saved.setFormNameAndXmlFormid(c.getString(8)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db

                this.saved.add(saved);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        /**
         * set the adapter to the saved list and display the correct list
         * of saved forms
         */
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                adapter.add(saved);
                adapter.notifyDataSetChanged();
            }

        });


        ////////////////////LL 15-04-2014 eliminata per dismissione del db grasp//////////////////////////////
        /*
		saved.clear();
		//dbAdapter.open();
		Cursor cursor = ApplicationExt.getDatabaseAdapter().open().fetchAllSaved();
        try
        {
	        while (cursor.moveToNext())  
	        { 
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
              
        });*/
    }

    /**
     * we set the adapter of the saved forms list to null
     */
    public void onDestroy()
    {
        listview.setAdapter(null);
        super.onDestroy();
    }

}