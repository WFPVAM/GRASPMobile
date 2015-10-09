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

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import java.util.ArrayList;

import it.fabaris.wfp.activities.FormListCompletedActivity.FormListHandlerCompleted;
import it.fabaris.wfp.activities.FormListFinalizedActivity.FormListHandlerFinalized;
import it.fabaris.wfp.activities.FormListNewActivity.FormListHandlerNew;
import it.fabaris.wfp.activities.FormListSavedActivity.FormListHandlerSaved;
import it.fabaris.wfp.activities.FormListSubmittedActivity.FormListHandlerSubmitted;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import object.FormInnerListProxy;

/**
 * Class that defines the tab activity of the forms
 *
 */
public class FormListActivity extends TabActivity  implements 	FormListHandlerNew,
        FormListHandlerFinalized,
        FormListHandlerSaved,
        FormListHandlerCompleted,
        FormListHandlerSubmitted


{
    private int quanteNuove = 0;
    private int quanteSalvate = 0;
    private int quanteComplete = 0;
    private int quanteFinalizzate = 0;
    private int quanteInviate = 0;
    public static final int sendImages_ID = 1;
    public static final int sendforms_ID=2;

    private FormInnerListProxy nuova;
    private FormInnerListProxy salvata;
    private FormInnerListProxy completa;
    private FormInnerListProxy finalizzata;
    private FormInnerListProxy inviata;

    private ArrayList<FormInnerListProxy> listInviate;
    private ArrayList<FormInnerListProxy> listSalvate;
    private ArrayList<FormInnerListProxy> listFinalizzate;
    private ArrayList<FormInnerListProxy> listNuove;
    public static ArrayList<FormInnerListProxy> copyFinalized;
    public static ArrayList<FormInnerListProxy> copyCompleted;
    private ArrayList<FormInnerListProxy> listComplete;

    //private ArrayList<FormInnerListProxy> listSaved;//LL 14-05-2014 eliminata per dismissione db grasp
    //private ArrayList<FormInnerListProxy> listCompleted; //LL 14-05-2014 eliminata per dismissione del db grasp
    //private ArrayList<FormInnerListProxy> listSubmitted;//LL 14-05-2014 eliminata per dismissione del dg grasp

    private TextView textNuove;
    private TextView textSalvate;
    private TextView textComplete;
    private TextView textFinalizzate;
    private TextView textInviate;

    /**
     *  DATABASE
     */
    public Cursor cursor;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formlist);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        /**
         * set the context for the db
         * dbAdapter = new DbAdapterGrasp(this);
         */

        //listSaved = new ArrayList<FormInnerListProxy>(); //LL 14-05-2014 eliminata per dismissione del db grasp
        //listCompleted = new ArrayList<FormInnerListProxy>();//LL 14-05-2014 eliminata per dismissione del db grasp
        //listSubmitted = new ArrayList<FormInnerListProxy>();//LL 14-05-2014 eliminata per dismissione del db grasp

        listInviate = new ArrayList<FormInnerListProxy>();
        listSalvate = new ArrayList<FormInnerListProxy>();
        listComplete = new ArrayList<FormInnerListProxy>();
        listFinalizzate = new ArrayList<FormInnerListProxy>();
        listNuove = new ArrayList<FormInnerListProxy>();


        //getFormsDataSubmitted();//LL 14-05-2014 eliminata per dismissione del db grasp
        //getFormsDataCompleted(); //LL 14-05-2014 eliminata per dismissione del db grasp
        //getFormsDataSaved(); //LL 14-05-2014 eliminato per dismissione del db grasp


        final TabHost tabHost = getTabHost();

        /**
         * INTENT CREATION FOR THE ACTIVITY LAUNCH AND INITIALIZATION OF THE TABHOST
         */
        TabSpec newFormSpec = tabHost.newTabSpec("New");
        Intent newFormIntent = new Intent(this, FormListNewActivity.class);
        newFormSpec.setIndicator(getString(R.string.tab_new), getResources().getDrawable(R.layout.icontab_formnew))
                .setContent(newFormIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        newFormIntent.putParcelableArrayListExtra("new", listNuove);
        tabHost.addTab(newFormSpec);


        TabSpec savedFormSpec = tabHost.newTabSpec("Saved");
        Intent savedFormIntent = new Intent(this, FormListSavedActivity.class);
        savedFormSpec.setIndicator(getString(R.string.tab_saved), getResources().getDrawable(R.layout.icontab_formsaved))
                .setContent(savedFormIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        savedFormIntent.putParcelableArrayListExtra("saved", listSalvate);
        //savedFormIntent.putParcelableArrayListExtra("salvate", listSaved); LL 14-05-2014 eliminato per dismissione del db grasp
        tabHost.addTab(savedFormSpec);


        TabSpec completedFormSpec = tabHost.newTabSpec("Completed");
        Intent completedFormIntent = new Intent(this, FormListCompletedActivity.class);
        completedFormSpec.setIndicator(getString(R.string.tab_completed), getResources().getDrawable(R.layout.icontab_formcompleted))
                .setContent(completedFormIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        completedFormIntent.putParcelableArrayListExtra("completed", listComplete);
        //completedFormIntent.putParcelableArrayListExtra("complete", listCompleted);//LL 14-05-2014 eliminato per dismissione del db grasp
        tabHost.addTab(completedFormSpec);
        copyCompleted=listComplete;

        TabSpec finalizedFormSpec = tabHost.newTabSpec("Pending");     //INVIATI MA NON ANCORA RICEVUTI DAL SERVIZIO - PENDING -
        Intent finalizedFormIntent = new Intent(this, FormListFinalizedActivity.class);
        finalizedFormSpec.setIndicator(getString(R.string.tab_finalized), getResources().getDrawable(R.layout.icontab_formfinalized))
                .setContent(finalizedFormIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finalizedFormIntent.putParcelableArrayListExtra("finalized", listFinalizzate);
        tabHost.addTab(finalizedFormSpec);
        copyFinalized=listFinalizzate;


        TabSpec submittedFormSpec = tabHost.newTabSpec("Submitted");
        Intent submittedFormIntent = new Intent(this, FormListSubmittedActivity.class);
        submittedFormSpec.setIndicator(getString(R.string.tab_submitted), getResources().getDrawable(R.layout.icontab_formsubmitted))
                .setContent(submittedFormIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        submittedFormIntent.putParcelableArrayListExtra("submitted", listInviate);
        //submittedFormIntent.putParcelableArrayListExtra("inviate", listSubmitted);//LL 14-05-2014 eliminato per dismissione del db grasp
        tabHost.addTab(submittedFormSpec);


        //if(listSaved.size() > 0) LL 14-05-2014 modificato per dismissione del db grasp
        if(listSalvate.size() > 0) //LL 14-05-2014 modificato per dismissione del db grasp
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            /**
                             * Yes button clicked
                             */
                            tabHost.setCurrentTab(1);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            /**
                             * No button clicked
                             */
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.dialog_save)).setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }


    /**
     * selects from forms db all the forms in the submitted state and
     * initialize an ArrayList<FormInnerListProxy> object that contains all the submitted forms in order to pass it to
     * 'FormListSubmittedActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the submitted forms to display
     *
     */
    public void selectViewInviate(){
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,instanceFilePath,displayNameInstance,displaySubtext,date,formNameAndXmlFormid,enumeratorID,submissionDate  FROM forms WHERE status = 'submitted' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        try
        {
            listInviate.clear();
            if (c.moveToFirst()){
                do
                {
                    inviata = new FormInnerListProxy();
                    inviata.setPathForm(c.getString(0));
                    inviata.setFormName(c.getString(1));
                    inviata.setStrPathInstance(c.getString(2));
                    inviata.setFormNameInstance(c.getString(3));
                    inviata.setFormNameAutoGen(c.getString(4));
                    inviata.setDataInvio(c.getString(5));


                    inviata.setFormNameAndXmlFormid(c.getString(6)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    inviata.setFormEnumeratorId(c.getString(7)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    inviata.setSubmissionDate(c.getString(8));

                    listInviate.add(inviata);

                }while(c.moveToNext());
            }
            quanteInviate = listInviate.size();

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if ( c != null ) {
                c.close();
                dbh.close();
            }
        }
    }
	
	/*//LL 14-05-2014 eliminato per dismissione del db grasp
	public void getFormsDataSubmitted()
	{
		listSubmitted.clear();
        cursor = ApplicationExt.getDatabaseAdapter().open().fetchAllSubmitted();
        try
        {
	        while (cursor.moveToNext())  
	        { 
	        	FormInnerListProxy submitted = new FormInnerListProxy(); 
	        	submitted.setFormId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_ID_KEY))); 
	        	submitted.setFormName(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_NOME_FORM))); 
	        	submitted.setDataInvio(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_SUBMITTED_DATA)));   
	        	submitted.setDataDiCompletamento(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_COMPLETED_DATA))); 
	        	submitted.setFormEnumeratorId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_BY))); 
	              
	        	listSubmitted.add(submitted); 
	        } 
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	if ( cursor != null )
        	{
        		cursor.close(); 
        		ApplicationExt.getDatabaseAdapter().close(); 
        	}
        }	        
	}
	*/


    /**
     * selects from forms db all the forms in the Finalized state and
     * initialize an ArrayList<FormInnerListProxy> object that contains all the finalized forms in order to pass it to
     * 'FormListFinalizedActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the Finalized form to display
     *
     */
    public void selectViewFinalizzate(){
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,instanceFilePath,displayNameInstance,displaySubtext,date FROM forms WHERE status = 'finalized' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);;
        try {
            listFinalizzate.clear();
            if (c.moveToFirst()){
                do {
                    finalizzata = new FormInnerListProxy();

                    finalizzata.setPathForm(c.getString(0));
                    finalizzata.setFormName(c.getString(1));
                    finalizzata.setStrPathInstance(c.getString(2));
                    finalizzata.setFormNameInstance(c.getString(3));
                    finalizzata.setFormNameAutoGen(c.getString(4));

                    listFinalizzate.add(finalizzata);

                }while(c.moveToNext());
            }
            quanteFinalizzate = listFinalizzate.size();

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if ( c != null ) {
                c.close();
                dbh.close();
            }
        }

    }

    /**
     * selects from forms db all the forms in the state "completed" and
     * initialize an ArrayList<FormInnerListProxy> object that contains all the completed forms in order to pass it to
     * 'FormListCompletedActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the new form to display
     *
     */
    public void selectViewComplete(){
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,instanceFilePath,displayNameInstance,displaySubtext,completedDate,formNameAndXmlFormid,enumeratorID" +
                " FROM forms WHERE status = 'completed' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        try {
            listComplete.clear();
            if (c.moveToFirst()){
                do
                {
                    completa  = new FormInnerListProxy();
                    completa.setPathForm(c.getString(0));
                    completa.setFormName(c.getString(1));
                    completa.setStrPathInstance(c.getString(2));
                    completa.setFormNameInstance(c.getString(3));
                    completa.setFormNameAutoGen(c.getString(4));

                    completa.setDataDiCompletamento(c.getString(5));   //LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    completa.setFormNameAndXmlFormid(c.getString(6)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    completa.setFormEnumeratorId(c.getString(7)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db


                    listComplete.add(completa);

                }while(c.moveToNext());
            }
            quanteComplete = listComplete.size();

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if ( c != null ) {
                c.close();
                dbh.close();
            }
        }
    }
	
	/*LL 14-05-2014 eliminata per dismissione del db grasp
	public void getFormsDataCompleted()
	{
		listCompleted.clear();
		cursor = ApplicationExt.getDatabaseAdapter().open().fetchAllCompleted();
        try
        {
	        while (cursor.moveToNext())  
	        { 
	        	
	        	FormInnerListProxy completed = new FormInnerListProxy(); 
	        	String iddatabase = (cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_ID_KEY)));
	        	
	        	
	        	completed.setIdDataBase(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_ID_KEY)));//LL aggiunto 12-02-14 per gestire bene la visualizzazione delle submitted nella ListView submitted
	        	completed.setFormId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_ID_COMPLETED_KEY))); 
	        	completed.setFormName(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_NOME_FORM))); 
	        	completed.setLastSavedDateOn(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_DATA)));   
	        	completed.setFormEnumeratorId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_BY)));  
	              
	        	listCompleted.add(completed);
	        }
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	if ( cursor != null )
        	{
        		cursor.close();
        		ApplicationExt.getDatabaseAdapter().close();
        	}
        }
	}*/


    /**
     * selects from forms db all the forms in the Saved state and
     * initialize an ArrayList<FormInnerListProxy> object that contains all the saved forms in order to pass it to
     * 'FormListSaveddActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the saved form to display
     *
     */
    public void selectViewSalvate(){
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,instanceFilePath,displayNameInstance,displaySubtext,jrFormId,date,enumeratorID,formNameAndXmlFormid  FROM forms WHERE status = 'saved' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);;
        try {
            listSalvate.clear();
            if (c.moveToFirst()){
                do
                {
                    salvata = new FormInnerListProxy();
                    salvata.setPathForm(c.getString(0));
                    salvata.setFormName(c.getString(1));
                    salvata.setStrPathInstance(c.getString(2));
                    salvata.setFormNameInstance(c.getString(3));
                    salvata.setFormNameAutoGen(c.getString(4));
                    salvata.setFormId(c.getString(5));



                    salvata.setLastSavedDateOn(c.getString(6));   //LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    salvata.setFormEnumeratorId(c.getString(7)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    salvata.setFormNameAndXmlFormid(c.getString(8)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db


                    listSalvate.add(salvata);

                }while(c.moveToNext());
            }
            quanteSalvate = listSalvate.size();

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if ( c != null ) {
                c.close();
                dbh.close();
            }
        }

    }
	
	/*LL 14-05-2014 eliminato per dismissione del db grasp
	public void getFormsDataSaved(){
		listSaved.clear();
        cursor = ApplicationExt.getDatabaseAdapter().open().fetchAllSaved();
        
        try
        {
	        while (cursor.moveToNext())
	        { 
	        	
	        	FormInnerListProxy saved = new FormInnerListProxy(); 
	        	saved.setIdDataBase(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_KEY))); 
	        	//saved.setFormId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_KEY))); //LLtoltoquesto 10-03-14
	        	saved.setFormId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_ID_SAVED_KEY))); //LLmessoquesto 10-03-14
	        	saved.setFormName(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_NOME_FORM))); 
	        	saved.setLastSavedDateOn(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_DATA)));   
	        	saved.setFormEnumeratorId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SAVED_FORM_BY))); 
	        	
	        	listSaved.add(saved); 
	        }
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        finally
        {
        	if ( cursor != null )
        	{
        		cursor.close();
        		ApplicationExt.getDatabaseAdapter().close();
        	}
        } 
	}*/

    /**
     * selects from forms db all the forms in the new state and
     * initialize an ArrayList<FormInnerListProxy> object that contains all the new forms in order to pass it to
     * 'FormListNewActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the new form to display
     *
     */
    public void selectViewNuove()
    {
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,jrFormId,date FROM forms WHERE status = 'new' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        try
        {
            listNuove.clear();
            if (c.moveToFirst())
            {
                do
                {
                    nuova  = new FormInnerListProxy();

                    nuova.setPathForm(c.getString(0));
                    nuova.setFormName(c.getString(1));
                    nuova.setFormId(c.getString(2));
                    nuova.setDataDownload(c.getString(3));

                    listNuove.add(nuova);

                }
                while(c.moveToNext());
            }
            quanteNuove = listNuove.size();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            quanteNuove = 0;
        }
        finally
        {
            if ( c != null )
            {
                c.close();
                dbh.close();
            }
        }
    }

    /**
     * @return  an ArrayList<FormInnerListProxy> object that contains all the new forms in order to pass it to
     * 'FormListNewActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the new form to display
     */
    public ArrayList<FormInnerListProxy> getNewForm()
    {
        return listNuove;
    }


    /**
     * @return an ArrayList<FormInnerListProxy> object that contains all the completed forms in order to pass it to
     * 'FormListCompletedActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the completed form to display
     */
    public ArrayList<FormInnerListProxy> getCompletedForm()
    {
        return listComplete;
    }


    /**
     * @return an ArrayList<FormInnerListProxy> object that contains all the submitted forms in order to pass it to
     * 'FormListSubmittedActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the submitted form to display
     */
    public ArrayList<FormInnerListProxy> getSubmittedForm()
    {
        return listInviate;
    }

    /**
     * @return an ArrayList<FormInnerListProxy> object that contains all the saved forms in order to pass it to
     * 'FormListSavedActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the saved form to display
     */
    public ArrayList<FormInnerListProxy> getSavedForm()
    {
        return listSalvate;
    }

    /**
     * @return an ArrayList<FormInnerListProxy> object that contains all the finalized forms in order to pass it to
     * 'FormListFinalizedActivity' activity, which will use it as a data source for the adapter associated
     * with the list of the finalized form to display
     */
    public ArrayList<FormInnerListProxy> getFinalizedForm()
    {
        return listFinalizzate;
    }

    public void onResume()
    {
        super.onResume();

        selectViewNuove();
        selectViewSalvate();
        selectViewComplete();
        selectViewFinalizzate();
        selectViewInviate();


        textNuove = (TextView) findViewById(R.id.text1);
        textNuove.setText(Integer.toString(quanteNuove));

        textSalvate = (TextView) findViewById(R.id.text2);
        textSalvate.setText(Integer.toString(quanteSalvate));

        textComplete = (TextView) findViewById(R.id.text3);
        textComplete.setText(Integer.toString(quanteComplete));

        textFinalizzate = (TextView) findViewById(R.id.text4);
        textFinalizzate.setText(Integer.toString(quanteFinalizzate));

        textInviate = (TextView) findViewById(R.id.text5);
        textInviate.setText(Integer.toString(quanteInviate));


        if (listFinalizzate.size()> 0) {
            String nf = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(nf);
            int icon = R.drawable.icona_app_wfp;
            CharSequence tickerText = getString(R.string.not_img_ticker); // ticker-text
            long when = System.currentTimeMillis();
            Context context = getApplicationContext();
            CharSequence contentTitle =getString(R.string.not_img_title);
            CharSequence contentText = getString(R.string.not_img_text); ;
            Intent notificationIntent = new Intent(getBaseContext(), FormListFinalizedActivity.class);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new Notification(icon, tickerText, when);
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.defaults |= Notification.DEFAULT_SOUND;
         //   notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            mNotificationManager.notify(sendImages_ID, notification);


        }

        if(listComplete.size() >0){
            String nf = Context.NOTIFICATION_SERVICE;
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(nf);
            int icon = R.drawable.icona_app_wfp;
            CharSequence tickerText = getString(R.string.not_form_ticker); // ticker-text
            long when = System.currentTimeMillis();
            Context context = getApplicationContext();
            CharSequence contentTitle = getString(R.string.not_form_title);
            CharSequence contentText = getString(R.string.not_form_text); ;
            Intent notificationIntent = new Intent(getBaseContext(), FormListCompletedActivity.class);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new Notification(icon, tickerText, when);
            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.defaults |= Notification.DEFAULT_SOUND;
            mNotificationManager.notify(sendforms_ID, notification);
            mNotificationManager.cancel(MenuActivity.Pending_ID);
        }

    }
////////////////////////////////////////////////////////


//////////////////////////////////////////

    /**
     * called after a form has been completed and sets the number of the
     * completed forms in order to show it to the user.
     * @param a string array that contains all the completed forms
     */
    @Override
    public void catchCallBackCompleted(String[] complete)
    {
        quanteComplete = complete.length;
        super.onContentChanged();
    }


    /**
     * called after a form has been Finalize and sets the number of the finalized forms
     * in order to show it to the user.
     * @param a string array that contains all the finalized forms
     */
    public void catchCallBackFinalized(String[] finalized)
    {
        quanteFinalizzate = finalized.length;
        super.onContentChanged();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }





    /**
     * REFRESH THE DATA OF THE FORM TO COMPILE
     * THE TEXTVIEW OF THE COMPLETED FORMS LIST
     * @param nome_form
     * @param completed_data
     * @param completed_by
     */
	
	/*LL 14-05-2014 eliminata per dismissione del db grasp
	public void updateFormsDataToCompleted(String nome_form, String completed_data, String completed_by, String idDbDataBase)//LL 12-02-14 aggiunto parametro idDataBase per delete delle salvate
	{
		String completed_id = nome_form+completed_by;
		String mIdDbDataBase = idDbDataBase;//LL 12-02-14 aggiunto per fare la delite sulle salvate impostando come filtro idDataBase
        //ApplicationExt.getDatabaseAdapter().open().delete("SAVED", completed_id); //LL 12-02-14 eliminato perche' non usa il filtro giusto per cancellare le salvate
		
		ApplicationExt.getDatabaseAdapter().open().delete("SAVED", nome_form);//LL 12-02-14 aggiunto perche' con questo filtro si fa la delite sulla form salvata giusta
        ApplicationExt.getDatabaseAdapter().close();
		
		ApplicationExt.getDatabaseAdapter().open().insert("COMPLETED", completed_id, nome_form, completed_data, completed_by); 
        ApplicationExt.getDatabaseAdapter().close();
    }*/

    /**
     * AGGIORNA I DATI DELLA FORM PER COMPILARE LE TEXTVIEW DELLA LISTA SALVATE
     * @param nome_form
     * @param saved_data
     * @param saved_by
     */
	
	/*LL 14-05-2014 eliminato per dismissione db grasp
	public void updateFormsDataToSaved(String nome_form, String saved_data, String saved_by)
	{
		//CARICO IL DB CON I DATI RECUPERATI   
		String saved_id = nome_form+saved_by;
		ApplicationExt.getDatabaseAdapter().open().insert("SAVED", saved_id, nome_form, saved_data, saved_by); 
		ApplicationExt.getDatabaseAdapter().close(); 
	}*/



    /**
     * AGGIORNA I DATI DELLA FORM PER COMPILARE LE TEXTVIEW DELLA LISTA INVIATE
     * @param nome_form
     * @param submitted_data
     * @param submitted_by
     */
	/*//LL 14-05-2014 eliminato per dismissione del db grasp
	//IN QUESTA FUNZIONA VA AGGIUNTO IL PARAMETRO COMLETED ID DB GRASP CHE DEVE ESSERE USATO COME FILTRO SULLA DELITE
	public void updateFormsDataToSubmitted(String nome_form, String submitted_data, String submitted_by, String idFormDataBaseGras)
	{
		String submitted_id = nome_form+submitted_by;
		String filter = idFormDataBaseGras;
		//ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", submitted_id);//LL tolto per passare il giusto filtro per cancellare la form giusta
		
		
		ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", filter);//LL aggiunto per passare il giusto filtro per cancellare la form giusta
		ApplicationExt.getDatabaseAdapter().open().insert("SUBMITTED", submitted_id, idFormDataBaseGras, submitted_data, submitted_by); 
		ApplicationExt.getDatabaseAdapter().close(); 
	}*/





}
