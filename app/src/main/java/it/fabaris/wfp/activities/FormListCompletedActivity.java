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
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.parse.XFormParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import content.FormCompletedAdapter;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.provider.FormProvider;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.provider.InstanceProviderAPI;
import it.fabaris.wfp.task.HttpCheckAndSendPostTask;
import it.fabaris.wfp.task.HttpSendAllFormsTask;
import it.fabaris.wfp.utility.FileUtils;
import it.fabaris.wfp.utility.FormCompletedDataDBUpdate;
import it.fabaris.wfp.utility.NewFileUtils;
import object.FormInnerListProxy;
import utils.ApplicationExt;

/**
 * Class that defines the tab for the list of the completed forms
 */
public class FormListCompletedActivity extends Activity implements MyCallback {


    public interface FormListHandlerCompleted {
        public ArrayList<FormInnerListProxy> getCompletedForm();

        public void catchCallBackCompleted(String[] complete);
    }

    public FormListHandlerCompleted formListHandler;

    private Lock lock;
    private Condition cond;
    public static String portrait;
    public static String data;
    public String nomeform;
    public String autore;
    private FormCompletedAdapter adapter;
    private int positioncomplete = 0;
    private static String idFormNameInstance = null;
    //public static ArrayList<String> toBeDeleted = new ArrayList<>();
    public static String editingEnabled =null;
    public static HashMap<String,String> formsChangedOnServer = new HashMap<>();
    private static Notification notification;
    private NotificationManager nm;
    private ListView listview;

    private SharedPreferences settings;
    private String connectionType ;

    private String numClient;
    private String senderPhone;
    private String numModem;
    private String httpServer;
    private String encodeXml;
    private static boolean isSendAllForms = false;
    public boolean formHasImages =false;
    public boolean formHasVideos =true;
    public String VideoURI;
    ///////////////////////////////////////////////
    public static String formId;

    public static String idForm;

    public static ArrayList<String> istance;

    private ArrayList<FormInnerListProxy> complete;
    //private ArrayList<FormInnerListProxy> completed; LL 14-05-2014 eliminato per dismissione del db grasp

    private static ArrayList<FormCompletedDataDBUpdate> listDataToUpdateDB = new ArrayList<FormCompletedDataDBUpdate>();
    public static boolean formsForDeletion=false;
    public static boolean formUpdated=false;
    static String deletedFormName;
    ProgressDialog pd;
    String xmlID;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabcompleted);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String nf = Context.NOTIFICATION_SERVICE;
        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(nf);
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        portrait=settings.getString(PreferencesActivity.KEY_BUTTON_PORTRAIT,"");

        if(portrait.equalsIgnoreCase("enabled")){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        istance = new ArrayList<String>();
        final Builder builder = new AlertDialog.Builder(this);

		/*
         *  scrivo quante ne ho aggiunte sul tab
		 *  contaComplete - quanteComplete = aggiunte (textView)
		 *  text.setText(contaComplete -
		 *  quanteComplete);
		*/
        /**
         *  SEND All FORM
         */
        Button buttonSendAll = (Button) findViewById(R.id.sendAll);
        /**
         * always visible
         */
        buttonSendAll.setVisibility(View.VISIBLE);
        buttonSendAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                /**
                 *  MESSAGE SENDING IN THE LIST
                 */
                istance.clear();
                isSendAllForms = true;
                settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                //connectionType = settings.getString(PreferencesActivity.KEY_CONNECTION_TYPE, getString(R.string.default_connection_type));
                if (connectionType.equalsIgnoreCase("")) {
                    connectionType = "GPRS/UMTS preferred";
                }

                sendFormInList(complete);
                mNotificationManager.cancel(FormListActivity.sendforms_ID);
            }
        });

        lock = new ReentrantLock();
        cond = lock.newCondition();

        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        connectionType = settings.getString(PreferencesActivity.KEY_CONNECTION_TYPE, getString(R.string.default_connection_type));

        numClient = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE, getString(R.string.default_client_telephone));
        senderPhone = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE, getString(R.string.default_client_telephone));
        numModem = settings.getString(PreferencesActivity.KEY_SERVER_TELEPHONE, getString(R.string.default_server_telephone));
        httpServer = settings.getString(PreferencesActivity.KEY_SERVER_URL,getString(R.string.default_server_url));
        editingEnabled= settings.getString((PreferencesActivity.KEY_BUTTON_EDIT),"");

        Button sendAll = (Button) findViewById(R.id.sendAll);

//        if (!isNetworkConnected())
//            sendAll.setVisibility(View.GONE);

        complete = new ArrayList<FormInnerListProxy>();
       // complete = getIntent().getExtras().getParcelableArrayList("completed");
        complete = FormListActivity.copyCompleted;
		/*LL 14-05-2014 eliminato per dismissione del db grasp
        completed = new ArrayList<FormInnerListProxy>();
		completed = getIntent().getExtras().getParcelableArrayList("complete");
		*/

        if(complete.size() == 0){
           mNotificationManager.cancel(FormListActivity.sendforms_ID);
        }

        listview = (ListView) findViewById(R.id.listViewCompleted);
        listview.setCacheColorHint(00000000);
        listview.setClickable(true);
        //adapter = new FormCompletedAdapter(this, complete, completed); LL 14-05-2014 modificato per dismissione del db grasp
        adapter = new FormCompletedAdapter(this, complete); //LL 14-05-2014 modificato per dismissione del db grasp
        listview.setAdapter(adapter);
        registerForContextMenu(listview);


    ///////////////////////////////// For the Video //////////////////////////////////////

//        VideoURI = FormEntryActivity.getVideoPath();
//         if (VideoURI != null){
//             formHasVideos =true;
//         }



        /**
         * When the user click on an item of the list displayed, the relative form is shown, ready for the compilation
         */
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {

                    Context context = getApplicationContext();
                    Intent intent = new Intent (context, FormEntryActivity.class);
                    String keyIdentifer  = "ciao";
                    String keyIdentifer1  = "ciao1";
                    String keyIdentifer2  = "ciao2";
                    String keyIdentifer3  = "ciao3";
                    String keyIdentifer4  = "ciao4";


                    String pkgName = getPackageName();

                    //we can't get the formId for the completed forms, so this is a work around to get it
                   String str =complete.get(position).getFormNameAndXmlFormid();
                   String str1[]= str.split("&");

                    formId= str1[1];

                    //LL 14-05-2014 modificati per dismissione del db grasp
                    intent.putExtra(pkgName+keyIdentifer,  complete.get(position).getPathForm()); 			//formPathSalvate[position]);
                    intent.putExtra(pkgName+keyIdentifer1, complete.get(position).getFormName());			//formNameSalvate[position]);
                    intent.putExtra(pkgName+keyIdentifer2, complete.get(position).getFormNameInstance());	//formNameInstanceSalvate[position]);
                    intent.putExtra(pkgName+keyIdentifer3, complete.get(position).getFormId()); 				//formIdSalvate[position]);
                    intent.putExtra(pkgName+keyIdentifer4, complete.get(position).getIdDataBase()); 			//LLaggiunto 12 perche' e' necessario inviare a form entry idDataBase per poter fare la delite sul db delle salvate per iddbform nel caso in cui si voglia salvare la forma alla fine
                    //dentro a formEntryActivity

                    Log.i("enumeratorID:"+  complete.get(position).getFormEnumeratorId(),"FormNameInstance" + complete.get(position).getFormNameInstance() );

                //check if editing is enabled in settings
                if(editingEnabled.equalsIgnoreCase("Enabled")){

                    String action = getIntent().getAction();
                    FormEntryActivity.fromHyera = true;


                    if (Intent.ACTION_PICK.equals(action))
                    {
                        //setResult(RESULT_OK, new Intent().setData(Uri.parse(salvati.get(positionSalvati).getStrPathInstance()))); LL 14-05-2014 modificata per dismissione del db grasp
                        setResult(RESULT_OK, new Intent().setData(Uri.parse(complete.get(position).getStrPathInstance()))); //LL 14-05-2014 modificata per dismissione del db grasp
                    }

                    else
                    {
                        intent.setAction(Intent.ACTION_EDIT);
                        FormListSavedActivity.SAVE = true;
                        String extension = MimeTypeMap.getFileExtensionFromUrl(complete.get(position).getStrPathInstance()).toLowerCase();
                        String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        intent.setDataAndType(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mimeType);
                        startActivity(intent);//chiama formEntry
                    }
                }

                else
                {

                String action = getIntent().getAction();
                FormEntryActivity.fromHyera = true;
                intent.putExtra("submitted", true);
                intent.setAction(Intent.ACTION_EDIT);
                String extension = MimeTypeMap.getFileExtensionFromUrl(complete.get(position).getStrPathInstance()).toLowerCase(); //LL questa posizione non e' corretta//LL rimessa per dismissione db grasp
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                intent.setDataAndType(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mimeType);
                startActivity(intent);

                 }//end of else
            }
        });


        /**
         * called when the user long click on one of the form listed.
         * Its purpose is send a completed form, but before displays a Dialog in
         * order to be sure about the real intentions of the user
         */
//        listview.setOnItemLongClickListener(new OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
//                //idFormNameInstance = completed.get(position).getFormName();
//                builder.setMessage(getString(R.string.send_form))
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.positive_choise,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int id) {
//                                        dialog.dismiss();
//                                        //LL 14-05-2014 db grasp dismesso non ci sono piu' problemi di allineamento
//                                        //ArrayList<FormInnerListProxy> mycomplete = getCompleteParceableList();//LL aggiunti perche' qui non avevo visibilita' sulle variabili d'istanza
//                                        ArrayList<FormInnerListProxy> mycompleted = getCompletedParceableList();//LL aggiunti perche' qui non avevo visibilita' sulle variabili d'istanza
//
//                                        idFormNameInstance = mycompleted.get(position).getFormName();
//                                        ///////////////////////////////////////////////////////////////c
//                                         String str = mycompleted.get(position).getStrPathInstance();
////                                        String str1[] = str.replace("/storage/emulated/0/GRASP/instances/", "").split("/");
////                                        String formId = str1[0];
//                                        String str1[] = str.substring(str.lastIndexOf("instances")).split("/");
////                                        String formName = str1[1];
//                                        String formId= str1[1];
//
//                                        /////////////////////////////////////////////////////////////
//                                        //int positioncomplete = getPositionCompletedToSubmit(position); //LL 14-05-2014 db grasp dismesso non ci sono piu' problemi di disallineamento
//
//                                        // ****************************************************
//                                        istance.clear();
//
//                                        encodeXml = decodeForm(complete.get(position));//LL 14-05-2014 rimesso per dismissione db grasp
//                                        //encodeXml = decodeForm(mycomplete.get(positioncomplete));//LL sostituito position con positioncomplete per prendere la form giusta//LL 14-05-2014 dismissione db grasp
//                                        // ****************************************************
//                                        /**
//                                         *  CAMPI DI SALVATAGGIO INVIATO
//                                         */
//
//                                        //nomeform = mycomplete.get(positioncomplete).getFormName();
//                                        nomeform = mycompleted.get(position).getFormName();
//                                        autore = mycompleted.get(position).getFormEnumeratorId();//qui ci sta il valore dell' enumeratorID da passare quando si invia la form per poter salvare il valore nella tabella delle inviate
//
//                                        FormCompletedDataDBUpdate myFormCompletedDataDBUpdate = new FormCompletedDataDBUpdate(idFormNameInstance, nomeform, autore);
//                                        listDataToUpdateDB.add(myFormCompletedDataDBUpdate);//qui dentro metto i dati per fare l'update del DB grasp che verra' effettuato dalla chiamata di callback una volta che la form e' stata inviata al server
//
//
//										/*LL 20-03-2014 da rimettere per form di test
//										String[] splittedFormName = nomeform.split("_");
//										String endOfForm = splittedFormName[splittedFormName.length-1];//se l'ultima occorrenza e' test
//
//										if(!endOfForm.equalsIgnoreCase("test")){//se il nome della form NON finisce per _test non bisogna spedire la form
//										*/
//                                        if (!(readSubmittedImages(str).size() == 0)) {
//
//                                            formHasImages = true;
//                                        }
//
//
//                                        if (isNetworkConnected()) {
//                                            try {
//                                                // invio solo tramite sms
//                                                if (connectionType
//                                                        .equalsIgnoreCase("sms only")) {
//                                                    sendSMS(numModem,
//                                                            encodeXml,
//                                                            FormListCompletedActivity.this);
//                                                }
//                                                // invio solo tramite 2g/3g
//                                                else if (connectionType
//                                                        .equalsIgnoreCase("gprs/umts only")) {
//                                                    sendWithNetwork(
//                                                            FormListCompletedActivity.this,
//                                                            httpServer,
//                                                            numClient,
//                                                            encodeXml,
//                                                            FormListCompletedActivity.this, formId, formHasImages);
//                                                    adapter.notifyDataSetInvalidated();
//                                                    adapter.notifyDataSetChanged();
//                                                } else if (connectionType
//                                                        //.equalsIgnoreCase(getString(R.string.on_request))) {   LL 14-1-14
//                                                        .equalsIgnoreCase("gprs/umts preferred")) { //LL 14-1-14
//
//                                                    try {
//                                                        sendWithNetwork(
//                                                                FormListCompletedActivity.this,
//                                                                httpServer,
//                                                                numClient,
//                                                                encodeXml,
//                                                                FormListCompletedActivity.this, formId, formHasImages);
//                                                    } catch (InterruptedException e) {
//                                                        // TODO
//                                                        // Auto-generated
//                                                        // catch
//                                                        // block
//                                                        e.printStackTrace();
//                                                    }
//                                                    adapter.notifyDataSetInvalidated();
//                                                    adapter.notifyDataSetChanged();
//
//
//														/* vecchia logica ora                          LL
//														CharSequence[] items = {
//																"gprs/umts", "sms" };
//														new AlertDialog.Builder(
//																FormListCompletedActivity.this)
//																.setSingleChoiceItems(
//																		items, 0,
//																		null)
//																.setPositiveButton(
//																		R.string.positive_choise,
//																		new DialogInterface.OnClickListener() {
//																			public void onClick(
//																					DialogInterface dialog,
//																					int whichButton) {
//																				dialog.dismiss();
//																				int selectedPosition = ((AlertDialog) dialog)
//																						.getListView()
//																						.getCheckedItemPosition();
//																				if (selectedPosition == 0) {
//																					// -------------------------------------------------
//																					pd = ProgressDialog
//																							.show(FormListCompletedActivity.this,
//																									FormListCompletedActivity.this
//																											.getString(R.string.checking_server),
//																									FormListCompletedActivity.this
//																											.getString(R.string.wait));
//
//																					// -------------------------------------------------
//
//																					try {
//																						sendWithNetwork(
//																								FormListCompletedActivity.this,
//																								httpServer,
//																								numClient,
//																								encodeXml,
//																								FormListCompletedActivity.this);
//																					} catch (InterruptedException e) {
//																						// TODO
//																						// Auto-generated
//																						// catch
//																						// block
//																						e.printStackTrace();
//																					}
//																					adapter.notifyDataSetInvalidated();
//																					adapter.notifyDataSetChanged();
//
//
//																				} else if (selectedPosition == 1) {
//																					sendSMS(numModem,
//																							encodeXml,
//																							FormListCompletedActivity.this);
//
//																				}
//																			}
//																		}).show();*/ //LL
//                                                }
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        } else if (!isNetworkConnected()) {
//                                            try {
//                                                // invio solo tramite sms
//                                                if (connectionType
//                                                        //.equalsIgnoreCase("sms") LL 14-01-14
//                                                        .equalsIgnoreCase("sms only") //LL
//                                                    //|| connectionType.equalsIgnoreCase("gprs/umts") //LL 14-01-14
//                                                    //|| connectionType.equalsIgnoreCase("gprs/umts preferred")) { //LL
//                                                        ) { //LL
//
//                                                    sendSMS(numModem,
//                                                            encodeXml,
//                                                            FormListCompletedActivity.this);
//														/* LL
//														CharSequence[] items = { "sms" };
//														new AlertDialog.Builder(
//																FormListCompletedActivity.this)
//																.setSingleChoiceItems(
//																		items, 0,
//																		null)
//																.setPositiveButton(
//																		R.string.positive_choise,
//																		new DialogInterface.OnClickListener() {
//																			public void onClick(
//																					DialogInterface dialog,
//																					int whichButton) {
//																				dialog.dismiss();
//																				sendSMS(numModem,
//																						encodeXml,
//																						FormListCompletedActivity.this);
//
//
//																			}
//																		}).show();
//
//														 */    //LL
//                                                } else if (connectionType.equalsIgnoreCase("gprs/umts only")) { // gprs/umts only
//                                                    //it is not possible to send the form wait for the connection
//                                                  //  CharSequence[] items = {"There isn't a connection. It's not possible to send the form"};
//                                                    new AlertDialog.Builder(
//                                                            FormListCompletedActivity.this)
////                                                            .setSingleChoiceItems(
////                                                                    items, 0,
////                                                                    null)
//                                                            .setMessage(R.string.notConnected)
//                                                            .setPositiveButton(
//                                                                    R.string.positive_choise,
//                                                                    new DialogInterface.OnClickListener() {
//                                                                        public void onClick(
//                                                                                DialogInterface dialog,
//                                                                                int whichButton) {
//                                                                            dialog.dismiss();
//                                                                        }
//                                                                    }
//                                                            )
//                                                            .show();
//
//                                                } else if (connectionType.equalsIgnoreCase("gprs/umts preferred")) { //here there is no data connection
//                                                    //it is not possible to send the form by network do u want to send it by sms?
//                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormListCompletedActivity.this);
//                                                    alertDialogBuilder.setTitle("Connection not present!");
//                                                    alertDialogBuilder
//                                                            .setMessage("do you want to send the form by sms?")
//                                                            .setPositiveButton(
//                                                                    R.string.positive_choise,
//                                                                    new DialogInterface.OnClickListener() {
//                                                                        public void onClick(
//                                                                                DialogInterface dialog,
//                                                                                int whichButton) {
//                                                                            dialog.dismiss();
//                                                                            sendSMS(numModem,
//                                                                                    encodeXml,
//                                                                                    FormListCompletedActivity.this);
//                                                                        }
//                                                                    }
//                                                            )
//                                                            .setNegativeButton(
//                                                                    R.string.negative_choise,
//                                                                    new DialogInterface.OnClickListener() {
//                                                                        public void onClick(
//                                                                                DialogInterface dialog,
//                                                                                int whichButton) {
//                                                                            dialog.dismiss();
//                                                                        }
//                                                                    }
//                                                            )
//
//                                                            .show();
//                                                }
//                                            } catch (Exception e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        //}	LL per form di test
//                                    }
//                                }
//                        )
//                        .setNegativeButton(getString(R.string.negative_choise),
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog,
//                                                        int id) {
//                                        dialog.dismiss();
//                                    }
//                                }
//                        );
//                AlertDialog alert = builder.create();
//                alert.show();
//                //finish();
//                return false;
//            }
//        });
       // for(int i=0;i<formsChangedOnServer.size();i++) {
        ArrayList<String> updatedForms= new ArrayList<>();
        ArrayList<String> deletedForms= new ArrayList<>();
        if(formsForDeletion){
        if (formsChangedOnServer !=null){
        if(formsChangedOnServer.size()!= 0) {
            for (String key : formsChangedOnServer.keySet()) {
                if (formsChangedOnServer.get(key).equalsIgnoreCase("newPublishedVersion"))
                    updatedForms.add(key);
                else if (formsChangedOnServer.get(key).equalsIgnoreCase("NotExisted") || formsChangedOnServer.get(key).equalsIgnoreCase("NotFinalized") || formsChangedOnServer.get(key).equalsIgnoreCase("Deleted"))
                    deletedForms.add(key);
            }

            if (updatedForms.size() != 0) {
                for (int i = 0; i < updatedForms.size(); i++) {
                    String formName = updatedForms.get(i);
                    messageDialog(FormListCompletedActivity.this, formName, getString(R.string.updated), getString(R.string.updated_title));
                }
            }
            if (deletedForms.size() != 0) {
                for (int i = 0; i < deletedForms.size(); i++) {
                    String formName = deletedForms.get(i);
                    messageDialog(FormListCompletedActivity.this, formName, getString(R.string.deleted), getString(R.string.deletd_title));
                }
            }

        }
        }
        }
    }

    /**
     * send a form via sms
     *
     * @param numero destination number
     * @param testo  text to send
     */
    public void sendSmsNetworkOff(String numero, String testo) {
        ContentValues values = new ContentValues();
        values.put("address", numero);
        values.put("body", testo);
        getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
    }

    /**
     * refresh the state of the form from completed to submitted in the forms db
     */
    public static void updateFormToSubmitted() {
        for (int k = 0; k < istance.size(); k++) {


            Calendar rightNow = Calendar.getInstance();
            java.text.SimpleDateFormat month = new java.text.SimpleDateFormat(
                    "MM", Locale.ENGLISH);
            // ----------------------------------------------------------------------------------------
            /**
             *  data di importazione
             */
            GregorianCalendar gc = new GregorianCalendar();
            String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));

            String year = Integer.toString(gc.get(Calendar.YEAR));

            String time = getCurrentTimeStamp();//LL

            data = day + "/" + month.format(rightNow.getTime()) + "/" + year;

            data = data + "  " + time;//LL
            // -----------------------------------------------------

            DatabaseHelper dbh = new DatabaseHelper("forms.db");
            String updatequery = "UPDATE forms SET status='submitted', submissionDate = '" + data + "'  WHERE displayNameInstance = '"
                    + istance.get(k) + "'";

            Log.i("FUNZIONE updateFormToSubmitted per la form: ",
                    istance.get(k));

            dbh.getReadableDatabase().execSQL(updatequery);

            dbh.close();
        }
    }
    public static void updateFormToFinalized() {
        for (int k = 0; k < istance.size(); k++) {

            DatabaseHelper dbh = new DatabaseHelper("forms.db");
            String updatequery = "UPDATE forms SET status='finalized'  WHERE displayNameInstance = '"
                    + istance.get(k) + "'";

            Log.i("FUNZIONE updateFormToSubmitted per la form: ",
                    istance.get(k));

            dbh.getReadableDatabase().execSQL(updatequery);
             dbh.close();
        }
    }
	/*
	///passo alla dialog gli oggetti parcellizzati
	private ArrayList<FormInnerListProxy> getCompleteParceableList(){
		ArrayList<FormInnerListProxy> mycompleted = this.complete;
		return mycompleted;
	}*/

    /**
     * @return mycomplete is an ArrayList object that contains all the completed forms
     */
    private ArrayList<FormInnerListProxy> getCompletedParceableList() {
        //ArrayList<FormInnerListProxy> mycomplete = this.completed;  //modificate per dismissione...
        ArrayList<FormInnerListProxy> mycomplete = this.complete;     //...del db grasp
        return mycomplete;

    }
	
	/*LL 14-05-2014 eliminata per dismissione del db grasp ed automatica eliminazione del problema relativo al disallineamento degli oggetti parcellizati
	//LL aggiunto 12-02-14
	//nell' oggetto parcellizzato di Fabaris identifica la posizione dell' item che contiene i dati giusti per la form selezionata nella lista
	private int getRightCompletedParcelableObject(String idFormInFabaris){//prende l'identificativo univoco della form
		
		int posizione = 0;
		//seleziona la posizione nella lista degli oggetti parcellizzati di fabaris che contiene l'id della form collegato all'oggetto parcellizzato cliccato sulla lista 
		//delle complete
		for(int i = 0; i<complete.size(); i++){
			String prova = complete.get(i).getFormNameInstance();
			if(complete.get(i).getFormNameInstance().contains(idFormInFabaris)){
				posizione = i;
			}
		}
		return posizione;//restituisce la posizione dell'oggetto parcellizzato fabaris cui fa riferimento la form selezionata nella lista delle complete
	} */


    //LL 14-05-2014 eliminata per dismissione del db grasp ed automatica eliminazione del problema relativo al disallineamento degli oggetti parcellizati
    //LL aggiunto 14-02-2014
    //e' stato necessario aggiungere questa funzione per trovare la form giusta da inviare perche' gli oggetti parcellizzati non sono raggiungibili dalla dialog
	/*
	private int getPositionCompletedToSubmit(int position){//prende l'identificativo univoco della form
		int positioncomplete = getRightCompletedParcelableObject(completed.get(position).getFormName());
		return positioncomplete;//restituisce la posizione dell'oggetto parcellizzato fabaris cui fa riferimento la form selezionata nella lista delle complete
	}*/

    /**
     * refreshs in the forms db the form's state, from completed to finalized.
     * Called when a form is pending, means sent but waiting for to be accepted by the server.
     */
//    public static void updateFormToFinalized() {
//        DatabaseHelper dbh = new DatabaseHelper("forms.db");
//        String updatequery = "UPDATE forms SET status='finalized' WHERE displayNameInstance = '"
//                + istance + "' AND status='completed'";
//        dbh.getReadableDatabase().execSQL(updatequery);
//        dbh.close();
//    }


    public void sendSmsNetWorkOn(final String numero, final String testo,
                                 MyCallback callback) {
        String sended = "SENDED";
        String delivered = "DELIVERED";
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> arrayTesto = sms.divideMessage(testo);
        int messageCount = arrayTesto.size();
        ArrayList<PendingIntent> sendingIntents = new ArrayList<PendingIntent>(
                messageCount);
        ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(
                messageCount);
        for (int j = 0; j < messageCount; j++) {
            sendingIntents.add(PendingIntent.getBroadcast(this, 0, new Intent(
                    sended), 0));
        }
        for (int j = 0; j < messageCount; j++) {
            deliveryIntents.add(PendingIntent.getBroadcast(this, 0, new Intent(
                    delivered), 0));
        }

        /**
         * SMS SENT
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context cont, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast toast1 = Toast.makeText(getBaseContext(),
                                R.string.sending_sms, Toast.LENGTH_LONG);
                        toast1.show();
                        updateFormToSubmitted();
                        adapter.notifyDataSetChanged();

                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast toast2 = Toast.makeText(getBaseContext(),
                                R.string.generic_error, Toast.LENGTH_SHORT);
                        toast2.show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast toast3 = Toast.makeText(getBaseContext(),
                                R.string.error_no_service, Toast.LENGTH_SHORT);
                        toast3.show();
                        sendSmsNetworkOff(numero, testo);
                        updateFormToFinalized();
                        adapter.notifyDataSetChanged();

                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast toast4 = Toast.makeText(getBaseContext(),
                                R.string.error_no_pdu, Toast.LENGTH_SHORT);
                        toast4.show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast toast5 = Toast.makeText(getBaseContext(),
                                R.string.error_network, Toast.LENGTH_SHORT);
                        toast5.show();
                        break;
                }
            }
        }, new IntentFilter(sended));

        /**
         *  SMS RECIVED
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), R.string.sms_delivered,
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(),
                                R.string.error_sms_not_delivered,
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(delivered));
        sms.sendMultipartTextMessage(numero, null, arrayTesto, sendingIntents,
                null);
    }

    /**
     * encodes the sms
     *
     * @param testo to encode
     * @return the test encode as a string
     */
    public static String encodeSms(String testo) {
        String res = null;
        try {
            byte[] bytestesto = testo.getBytes();
            ByteArrayInputStream inStream = new ByteArrayInputStream(bytestesto);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            GZIPOutputStream zipOutput = new GZIPOutputStream(outStream);
            int i;
            byte[] buffer = new byte[1024];
            while ((i = inStream.read(buffer)) > 0) {
                zipOutput.write(buffer, 0, i);
            }
            zipOutput.finish();
            zipOutput.close();
            res = Base64.encodeToString(outStream.toByteArray(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * called after the form has been sent at the server, to show some feedbacks to the user
     */
    public void callbackCall() {
        FormListActivity f = new FormListActivity();
		/*LL 14-05-2014 eliminato per dismissione db grasp
		for(int i=0;i<listDataToUpdateDB.size();i++){
			FormCompletedDataDBUpdate FormDataForDBupdate = listDataToUpdateDB.get(i);
			//f.updateFormsDataToSubmitted(nomeform + "&" + autore, data, autore, idFormNameInstance); //LL aggiunto ultimo parametro per cancellare dal DBGRASP la completa che deve diventare inviata
			f.updateFormsDataToSubmitted(FormDataForDBupdate.nomeform+ "&" + FormDataForDBupdate.autore, data, FormDataForDBupdate.autore, FormDataForDBupdate.idFormNameInstance); //e' stato necessario mettere i parametri da inviare in un oggetto per poter gestire nello stesso modo l'invio di una singola form e l'invio di tutte le form insieme 
		}*/
        listDataToUpdateDB.clear();

        if (isSendAllForms) {
            if (PreferencesActivity.SERVER_ONLINE == "YES") {
                Toast.makeText(this, R.string.server_on_line, Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, R.string.forms_sent, Toast.LENGTH_LONG).show();//LL 17-04-2014 necessario per gestire la visualizzazione del toast nel caso di invio di piu' form insieme
            isSendAllForms = false;//LL 17-04-2014 necessario per gestire la visualizzazione del toast nel caso di invio di piu' form insieme
        }


        finish();
    }

    /**
     * not used
     *
     * @param fileList
     * @return
     */

    private final Dialog createListFile(final String[] fileList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confronta con:");
        builder.setSingleChoiceItems(fileList, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d("scelta tipo di connessione",
                                "E' stato premuto il pulsante: "
                                        + fileList[whichButton]
                        );

                    }
                }
        );
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                }
        );
        return builder.create();
    }

    /**
     * not used
     *
     * @return
     */
    private ArrayList<FormInnerListProxy> queryCompletedForm() {
        formListHandler = new FormListActivity();
        ArrayList<FormInnerListProxy> complete = formListHandler
                .getCompletedForm();

        return complete;
    }

    /**
     * utility method to verify the connection
     *
     * @return true if connected false if not
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            /**
             *  There are no active networks.
             */
            return false;
        } else
            return true;
    }

    /**
     * displays a dialog:
     * on click "yes" button is called a method for send the form by the network
     * on click "no" button is called a method for send the form by sms
     *
     * @param complete ArrayList<FormInnerListProxy> complete list of the completed forms
     */
    private void sendFormInList(final ArrayList<FormInnerListProxy> complete) {//LL 17-04-2014 aggiunto ultimo parametro per far vedere il toast dell'invio andato bene solo una volta

//        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                switch (which) {
//                    case DialogInterface.BUTTON_POSITIVE:

        if (isNetworkConnected()) {
        try {
            if (connectionType
                    .equalsIgnoreCase("sms only")) {
                for (int k = 0; k < complete.size(); k++) {
                    String xml = null;
                    xml = decodeForm(complete.get(k));
                    try {
                        sendSMS(numModem, xml, FormListCompletedActivity.this);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if (connectionType
                    .equalsIgnoreCase("gprs/umts only")) {

                try {
                    sendAllFormsWithNetwork(FormListCompletedActivity.this, httpServer, numClient, complete);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else if (connectionType
                    //.equalsIgnoreCase(getString(R.string.on_request))) {   LL 14-1-14
                    .equalsIgnoreCase("gprs/umts preferred")) { //LL 14-1-14

                try {
                    sendAllFormsWithNetwork(FormListCompletedActivity.this, httpServer, numClient, complete);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }

//
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



        else if (!isNetworkConnected()) {
            try {
                // invio solo tramite sms
                if (connectionType
                        //.equalsIgnoreCase("sms") LL 14-01-14
                        .equalsIgnoreCase("sms only") //LL
                    //|| connectionType.equalsIgnoreCase("gprs/umts") //LL 14-01-14
                    //|| connectionType.equalsIgnoreCase("gprs/umts preferred")) { //LL
                        ) { //LL

                    for (int k = 0; k < complete.size(); k++) {
                        String xml = null;
                        xml = decodeForm(complete.get(k));
                        try {
                            sendSMS(numModem, xml, FormListCompletedActivity.this);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } else if (connectionType.equalsIgnoreCase("gprs/umts only")) { // gprs/umts only
                    //it is not possible to send the form wait for the connection
                  //  CharSequence[] items = {"There isn't a connection it isn't possible send the form"};
                    new AlertDialog.Builder(
                            FormListCompletedActivity.this)
//                            .setSingleChoiceItems(
//                                    items, 0,
//                                    null)
                            .setMessage(R.string.notConnected)
                            .setPositiveButton(
                                    R.string.positive_choise,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    }
                            )
                            .show();

                } else if (connectionType.equalsIgnoreCase("gprs/umts preferred")) { //here there is no data connection
                    //it is not possible to send the form by network do u want to send it by sms?
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormListCompletedActivity.this);
                    alertDialogBuilder.setTitle("Connection not present!");
                    alertDialogBuilder
                            .setMessage("do you want to send the form by sms?")
                            .setPositiveButton(
                                    R.string.positive_choise,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                            for (int k = 0; k < complete.size(); k++) {
                                                String xml = null;
                                                xml = decodeForm(complete.get(k));
                                                try {
                                                    sendSMS(numModem, xml, FormListCompletedActivity.this);
                                                } catch (Exception e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }}
                                        }
                                    }
                            )
                            .setNegativeButton(
                                    R.string.negative_choise,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    }
                            )

                            .show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



                }


    /**
     * form decoded
     *
     * @param form the form
     * @return string the form decoded as a string
     */
    private String decodeForm(FormInnerListProxy form) {
        String xml = null;
        try {
            istance.add(form.getFormNameInstance()); // formNameInstanceComplete[position];

            InputStream fileInput = new FileInputStream(
                    form.getStrPathInstance()); // path[position]);
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(fileInput);
            Transformer trans = TransformerFactory.newInstance()
                    .newTransformer();
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            String xmlString = trasformItem(trans, doc, form);
            xml = encodeSms(xmlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * create the file
     *
     * @param trans
     * @param doc
     * @param form
     * @return
     * @throws TransformerException
     */
    public String trasformItem(Transformer trans, Document doc,
                               FormInnerListProxy form) throws TransformerException, IOException {
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
/**
 * added by mureed
 * add the image path to the xml file
 */

        Hashtable<String, String> images = readSubmittedImages(form.getStrPathInstance());
        if (images != null && images.size() > 0) {
            Set<String> keys = images.keySet();
            for (String key : keys) {
                NodeList nodeList = doc.getElementsByTagName(key);
                Node node = nodeList.item(0);
                node.setTextContent(images.get(key));
            }
        }
//added by mureed to solve out of momery exception
        NodeList nodeList = doc.getElementsByTagName("data");
        Node node = nodeList.item(0);
        node.getAttributes().removeNamedItem("apos");

        String xmlString = getStringFromDoc(doc);

        //removed by mureed
//        DOMSource source = new DOMSource(doc);
//        trans.transform(source, result);
//        String xmlString = sw.toString();


        // String apos = "apos=\"'\"";
        // xmlString = xmlString.replace(apos, "");
        /**
         *  add unique code to data xml response
         */
        xmlString = xmlString + "?formidentificator?"
                + form.getFormNameAutoGen();
        /**
         *  add autogenerated name to data xml response
         */
        xmlString = xmlString + "?formname?" + form.getFormNameInstance();
        /**
         *  add date and time to data xml response
         */
        GregorianCalendar gc = new GregorianCalendar();
        String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
        String month = Integer.toString(gc.get(Calendar.MONTH));
        String year = Integer.toString(gc.get(Calendar.YEAR));
        String hour = Integer.toString(gc.get(Calendar.HOUR_OF_DAY));
        String date = day + "/" + month + "/" + year;
        return xmlString = xmlString + "?formhour?" + date + "_" + hour;
    }

    public String getStringFromDoc(Document doc) throws IOException {
        com.sun.org.apache.xml.internal.serialize.OutputFormat format = new com.sun.org.apache.xml.internal.serialize.OutputFormat(doc);
        StringWriter stringOut = new StringWriter();
        com.sun.org.apache.xml.internal.serialize.XMLSerializer serial = new com.sun.org.apache.xml.internal.serialize.XMLSerializer(stringOut, format);
        serial.serialize(doc);
        return stringOut.toString();
    }

    private void sendSMS(String numModem, String encodeXml, MyCallback callback) {
        if (numModem.equalsIgnoreCase("") || numModem == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    R.string.number_error, Toast.LENGTH_SHORT);
            //toast.setGravity(Gravity.CENTER, 40, 40);
            toast.show();
        } else {
            sendSmsNetWorkOn(numModem, encodeXml, callback);
        }
    }


    public Hashtable<String, String> readSubmittedImages(String filePath) {
        /**
         *  convert files into a byte array
         */
        byte[] fileBytes = FileUtils.getFileAsBytes(new File(filePath));
        Hashtable<String, String> images = new Hashtable<String, String>();


        /**
         *  get the root of the saved and template instances
         */
        TreeElement dataElements = XFormParser.restoreDataModel(fileBytes, null).getRoot();
        String imageName = "";
        for (int j = 0; j < dataElements.getNumChildren(); j++) {
            if (dataElements.getChildAt(j) != null && dataElements.getChildAt(j).getValue() != null && dataElements.getChildAt(j).getValue().getDisplayText().indexOf("jpg")  > 0 || dataElements.getChildAt(j) != null && dataElements.getChildAt(j).getValue() != null && dataElements.getChildAt(j).getValue().getDisplayText().indexOf("mp4")  > 0)  {
                imageName = dataElements.getChildAt(j).getValue().getDisplayText();

                if(imageName.contains("/instances")){
                    imageName= imageName.substring(imageName.lastIndexOf("/") + 1);
                }

                File originalImage = new File(filePath.substring(0, filePath.lastIndexOf("/") + 1) + imageName);
                if (originalImage.exists()) {
                    try {
                        String plus="\\+";
                        String syncImagesPath = Collect.IMAGES_PATH + "/" + senderPhone.replaceAll(plus,"");
                        if (FileUtils.createFolder(syncImagesPath)) {
                            File newImage = new File(syncImagesPath + "/" + imageName);
                            NewFileUtils.copyFile(originalImage, newImage);
                            images.put(dataElements.getChildAt(j).getName(), senderPhone.replaceAll(plus,"") + "\\" + imageName);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return images;
    }


    /**
     * invio della form tramite network
     *
     * @param context
     * @param url
     * @param number
     * @param form
     * @param callback
     * @param formHasImages
     * @throws InterruptedException
     */
    private void sendWithNetwork(Context context, String url, String number,
                                 String form, MyCallback callback, String formId, boolean formHasImages) throws InterruptedException {

        if (httpServer.equalsIgnoreCase("") || httpServer == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getString(R.string.server_url_not_inserted),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 40, 40);
            toast.show();
        } else {

            HttpCheckAndSendPostTask asyncTask = new HttpCheckAndSendPostTask(context, url, number, form, callback, isSendAllForms,formId,formHasImages);
//            Log.i("url HttpCheckAndSendPostTask FormListCompletedActivity ", "thread: " + url);
//            Log.i("FUNZIONE HttpCheckAndSendPostTask FormListCompletedActivity ", "thread: " + form);
            asyncTask.execute();
            if (asyncTask.getStatus() != HttpCheckAndSendPostTask.Status.FINISHED) {

            }
        }
    }

    /**
     * sends all forms togheter
     *
     * @param context                 this
     * @param url                     the server address
     * @param number                  the client's phone number
     * @param listaformcompletedfirst the completed list of the completed form to sent
     * @throws InterruptedException
     */
    private void sendAllFormsWithNetwork(Context context, String url, String number, ArrayList<FormInnerListProxy> listaformcompletedfirst) throws InterruptedException {

        if (httpServer.equalsIgnoreCase("") || httpServer == null) {
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.server_url_not_inserted), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 40, 40);
            toast.show();
        } else {
            HttpSendAllFormsTask asyncTask = new HttpSendAllFormsTask(context, url, number, complete, FormListCompletedActivity.this);//chiamo l'async task con la lista di tutte le form da inviare
            //Log.i("FUNZIONE HttpSendAllFormsTask", "thread: " + form);
            //Log.i("FUNZIONE HttpSendAllFormsTask", "thread: ");
            asyncTask.execute();
        }
    }


    public static void setID(String id) {
        idForm = id;
    }

    /**
     * called when the activity has to be displayed.
     * we need to set the adapter to see the list of the completed forms.
     */
    public void onResume() {
        super.onResume();
        getFormsDataCompleted();
        runOnUiThread(new Runnable() {
            public void run() {
                FormCompletedAdapter adapter = (FormCompletedAdapter) listview
                        .getAdapter();
                adapter.notifyDataSetChanged();
                cleanDBAfterEditing(complete);
            }
        });
    }


    /**
     * Once fetched all completed forms from forms db, initializes
     * the global field  "complete" that represents the complete list of completed forms
     */
    public void getFormsDataCompleted() {
        complete.clear();
        // dbAdapter.open();


        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,instanceFilePath,displayNameInstance,displaySubtext,completedDate,formNameAndXmlFormid,enumeratorID" +
                " FROM forms WHERE status = 'completed' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        int quanteComplete = 0;
        try {
            complete.clear();
            if (c.moveToFirst()) {
                do {
                    FormInnerListProxy completa = new FormInnerListProxy();
                    completa = new FormInnerListProxy();
                    completa.setPathForm(c.getString(0));
                    completa.setFormName(c.getString(1));
                    completa.setStrPathInstance(c.getString(2));
                    completa.setFormNameInstance(c.getString(3));
                    completa.setFormNameAutoGen(c.getString(4));

                    completa.setDataDiCompletamento(c.getString(5));   //LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    completa.setFormNameAndXmlFormid(c.getString(6)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    completa.setFormEnumeratorId(c.getString(7)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db


                    complete.add(completa);

                } while (c.moveToNext());
            }
            quanteComplete = complete.size();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                dbh.close();
                ApplicationExt.getDatabaseAdapter().close();
            }
        }  

    }

    /**
     * when the activity is destroyed we set the adapter of the completed forms list as null
     */
    public void onDestroy() {
        listview.setAdapter(null);
        super.onDestroy();
    }

    /**
     * refresh the global fields with the values of the current form in order to send the correct datas to the server
     *
     * @param completeposition the index of the form we want to send in the list of completed forms
     */
    public void setGlobalFieldForDBUpdating(int completeposition) {

        //int positioncompleted = getPositionCompletedToSubmit(completeposition);LL 14-05-2014  eliminato per dismissione db grasp
        //FormInnerListProxy myCompleteDShortCut = completed.get(positioncompleted); LL 14-05-2014  eliminato per dismissione db grasp
        FormInnerListProxy myCompleteShortCut = complete.get(completeposition);


        nomeform = myCompleteShortCut.getFormName();
        //autore = myCompleteDShortCut.getFormEnumeratorId();//LL 14-05-2014 eliminato per dismissione db grasp
        autore = myCompleteShortCut.getFormEnumeratorId();
        idFormNameInstance = myCompleteShortCut.getFormNameInstance();

        FormCompletedDataDBUpdate myFormCompletedDataDBUpdate = new FormCompletedDataDBUpdate(idFormNameInstance, nomeform, autore);
        listDataToUpdateDB.add(myFormCompletedDataDBUpdate);


    }
    public void messageDialog(Activity a, final String formName, String message,String title){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(a);
        //final AlertDialog.Builder dialog = new AlertDialog.Builder(FormListCompletedActivity.this);
        dialog.setTitle(title);
        dialog.setMessage("'"+ formName+ "'" +" "+ message);

        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                formsForDeletion=false;
//                formUpdated=false;
                FormChangedOnServer(formName);
                formsChangedOnServer.remove(formName);
                   // finish();


            }

        });
        dialog.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                formsForDeletion=false;
          //      finish();
           //     dismissDialog();
            }
        });
        dialog.create().show();

//        dialog.setTitle(title);
//        dialog.setMessage(message);
//        dialog.setNeutralButton("OK", null);
//        dialog.create().show();

    }

    public  void FormChangedOnServer(String deletedFormName) {

      //  for(int i=0; i<f.size();i++){
         //   deletedFormName=toBeDeleted.get(i);

//            alertDialog.setButton(R.string.confirm,
//                        new DialogInterface.OnClickListener(){
//
//                            public void onClick(DialogInterface dialog, int id)
//                            {
//							/*LL 14-05-2014 eliminato per dismissione del db grasp
//							//int positionSalvati = getRightCompletedParcelableObject(saved.get(position).getFormName()); LL eliminato per dismissione del db grasp
//							ApplicationExt.getDatabaseAdapter().open().delete("SAVED", saved.get(position).getFormName());
//							ApplicationExt.getDatabaseAdapter().close();
//							*/
//                                dialog.dismiss();
            //TODO update this query to DELETE or else it will cause serious crash
            FormProvider.DatabaseHelper dbh = new FormProvider.DatabaseHelper("forms.db");
            String query1 = "DELETE FROM forms WHERE displayName = '"
                    + deletedFormName+"'" ;
            // + "' AND status='new',status='completed,status='saved'";

            dbh.getWritableDatabase().execSQL(query1);
            dbh.close();
//
//
//                            }
//                        })
            // .show();
        }
//}
    /**
     * updates the submission date in forms db after the form has been sent
     */
    public static void UpdateDBAfterSendAllForms() {
        FormListActivity f = new FormListActivity();
        DatabaseHelper dbh = new DatabaseHelper("forms.db");


        for (int i = 0; i < listDataToUpdateDB.size(); i++) {
            FormCompletedDataDBUpdate FormDataForDBupdate = listDataToUpdateDB.get(i);
            /**
             * DATE MANAGE
             *  month manage
             */
            // --------------------------------------------------------------------------------------
            Calendar rightNow = Calendar.getInstance();
            java.text.SimpleDateFormat month = new java.text.SimpleDateFormat(
                    "MM",Locale.ENGLISH);
            // ----------------------------------------------------------------------------------------

            /**
             *  data di importazione
             */
            GregorianCalendar gc = new GregorianCalendar();
            String day = Integer.toString(gc
                    .get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(gc
                    .get(Calendar.YEAR));

            String data = day + "/"
                    + month.format(rightNow.getTime())
                    + "/" + year;

            String time = getCurrentTimeStamp();
            data = data + "  " + time;


            String query = "UPDATE forms SET submissionDate='" + data + "' WHERE displayNameInstance = '"
                    + idFormNameInstance
                    + "' AND status='completed'";

            dbh.getWritableDatabase().execSQL(query);
            dbh.close();


            //f.updateFormsDataToSubmitted(nomeform + "&" + autore, data, autore, idFormNameInstance); //LL aggiunto ultimo parametro per cancellare dal DBGRASP la completa che deve diventare inviata

            //LL elminata per dismissione del db grasp
            //f.updateFormsDataToSubmitted(FormDataForDBupdate.nomeform+ "&" + FormDataForDBupdate.autore, data, FormDataForDBupdate.autore, FormDataForDBupdate.idFormNameInstance); //e' stato necessario mettere i parametri da inviare in un oggetto per poter gestire nello stesso modo l'invio di una singola form e l'invio di tutte le form insieme
        }
        listDataToUpdateDB.clear();


    }


    /**
     * We destroy the Activity
     */
    public void finishFormListCompleted() {
        finish();
    }

    /**
     * @return the current timestamp as a string
     */
    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss",Locale.ENGLISH);
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
    public void finishFormListFinalized() {
        // TODO Auto-generated method stub

    }



    //clean the database from the copies generated from the editing
    //the problem is that if we press back another copy will be saved under saved tab
    //and if we press save as completed 2 copies will be under completed tab


public void cleanDBAfterEditing(ArrayList<FormInnerListProxy> formList) {
    String filePath = null;
    for (int i = 0; i < formList.size(); i++) {
        //In case it went to saved
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String updatequery = "DELETE FROM forms WHERE instanceFilePath='" + formList.get(i).getStrPathInstance() + "' AND status='saved'";
        dbh.getReadableDatabase().execSQL(updatequery);
        //In case it went to completed
        String selectQuery = "SELECT formFilePath FROM forms WHERE instanceFilePath='" + formList.get(i).getStrPathInstance() + "' AND status='completed'";
        Cursor c = dbh.getReadableDatabase().rawQuery(selectQuery, null);
        try {

            if (c.moveToFirst()) {
                do {
            filePath= c.getString(0);
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
                dbh.close();
                ApplicationExt.getDatabaseAdapter().close();
            }
        }

        if (!filePath.contains("/forms")) {
            String updatequery1 = "DELETE FROM forms WHERE formFilePath='" + filePath + "' AND status='completed'";
            dbh.getReadableDatabase().execSQL(updatequery1);
        }
}}
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        final int position;
        if (v.getId()==R.id.listViewCompleted) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle( complete.get(info.position).getFormName());
            menu.setHeaderIcon(R.drawable.ic_menu_forms);
            String[] menuItems = getResources().getStringArray(R.array.context_menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        switch(item.getItemId()) {
            case 0:
             deleteForm(positioncomplete);
            //    complete.remove(positioncomplete);
                return true;
            case 1:

                sendSingleForm(positioncomplete);
                return true;
//            case 2:
//                // remove stuff here
//
//                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    private void deleteForm(final int position) {
        final Builder builder = new AlertDialog.Builder(this);
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
                                        + complete.get(position).getFormNameInstance()
                                        + "' AND status='completed'";

                                dbh.getWritableDatabase().execSQL(query);
                                dbh.close();
                ///if the folder is to be kept then comment lines from here ///////
                                File file = new File(complete.get(position).getStrPathInstance());
                                boolean deleted = file.delete();

                                /**
                                 * CANCELLAZIONE DELLA CARTELLA TEMPORANEA
                                 */
                                String path = complete.get(position).getStrPathInstance().replace(".xml", "");
                                String folder[] = path.split("/", path.length());
                                File f = new File(Environment.getExternalStorageDirectory()+"/GRASP/instances/"+folder[folder.length-1]);

                                deleteDirectory(f);
                                if(deleted)
/////////till here//////////////
                                    Toast.makeText(FormListCompletedActivity.this, getString(R.string.cancelform) + " " +complete.get(position).getFormName(), Toast.LENGTH_LONG).show();
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

    }

    private void sendSingleForm(final int position) {


        if (connectionType.equalsIgnoreCase("")) {
            connectionType = "GPRS/UMTS preferred";
        }

           ArrayList<FormInnerListProxy> mycompleted = getCompletedParceableList();//LL aggiunti perche' qui non avevo visibilita' sulle variabili d'istanza

          idFormNameInstance = mycompleted.get(position).getFormName();
                                        ///////////////////////////////////////////////////////////////c
        String str = mycompleted.get(position).getStrPathInstance();
//
        String str1[] = str.substring(str.lastIndexOf("instances")).split("/");
        xmlID = mycompleted.get(position).getFormNameAndXmlFormid().split("&")[1];
        String formId= str1[1];
        formId =xmlID +"_"+ formId;

        istance.clear();

        encodeXml = decodeForm(complete.get(position));//LL 14-05-2014 rimesso per dismissione db grasp

        nomeform = mycompleted.get(position).getFormName();
        autore = mycompleted.get(position).getFormEnumeratorId();//qui ci sta il valore dell' enumeratorID da passare quando si invia la form per poter salvare il valore nella tabella delle inviate
        FormCompletedDataDBUpdate myFormCompletedDataDBUpdate = new FormCompletedDataDBUpdate(idFormNameInstance, nomeform, autore);
        listDataToUpdateDB.add(myFormCompletedDataDBUpdate);//qui dentro metto i dati per fare l'update del DB grasp che verra' effettuato dalla chiamata di callback una volta che la form e' stata inviata al server

        if (!(readSubmittedImages(str).size() == 0)) {
            formHasImages = true;
        }

        if (isNetworkConnected()) {
            try {
                // invio solo tramite sms
                if (connectionType
                        .equalsIgnoreCase("sms only")) {
                    sendSMS(numModem,
                            encodeXml,
                            FormListCompletedActivity.this);
                }
                // invio solo tramite 2g/3g
                else if (connectionType
                        .equalsIgnoreCase("gprs/umts only")) {
                    sendWithNetwork(
                            FormListCompletedActivity.this,
                            httpServer,
                            numClient,
                            encodeXml,
                            FormListCompletedActivity.this, formId, formHasImages);
                    adapter.notifyDataSetInvalidated();
                    adapter.notifyDataSetChanged();
                } else if (connectionType
                        //.equalsIgnoreCase(getString(R.string.on_request))) {   LL 14-1-14
                        .equalsIgnoreCase("gprs/umts preferred")) { //LL 14-1-14
                    try {
                                                        sendWithNetwork(
                                                                FormListCompletedActivity.this,
                                                                httpServer,
                                                                numClient,
                                                                encodeXml,
                                                                FormListCompletedActivity.this, formId, formHasImages);
                                                    } catch (InterruptedException e) {
                                                        // TODO
                                                        // Auto-generated
                                                        // catch
                                                        // block
                                                        e.printStackTrace();
                                                    }
                                                    adapter.notifyDataSetInvalidated();
                                                    adapter.notifyDataSetChanged();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else if (!isNetworkConnected()) {
                                            try {
                                                // invio solo tramite sms
                                                if (connectionType
                                                        //.equalsIgnoreCase("sms") LL 14-01-14
                                                        .equalsIgnoreCase("sms only") //LL
                                                    //|| connectionType.equalsIgnoreCase("gprs/umts") //LL 14-01-14
                                                    //|| connectionType.equalsIgnoreCase("gprs/umts preferred")) { //LL
                                                        ) { //LL

                                                    sendSMS(numModem,
                                                            encodeXml,
                                                            FormListCompletedActivity.this);

                                                } else if (connectionType.equalsIgnoreCase("gprs/umts only")) { // gprs/umts only
                                                    //it is not possible to send the form wait for the connection
                                                  //  CharSequence[] items = {"There isn't a connection. It's not possible to send the form"};
                                                    new AlertDialog.Builder(
                                                            FormListCompletedActivity.this)
//                                                            .setSingleChoiceItems(
//                                                                    items, 0,
//                                                                    null)
                                                            .setMessage(R.string.notConnected)
                                                            .setPositiveButton(
                                                                    R.string.positive_choise,
                                                                    new DialogInterface.OnClickListener() {
                                                                        public void onClick(
                                                                                DialogInterface dialog,
                                                                                int whichButton) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    }
                                                            )
                                                            .show();

                                                } else if (connectionType.equalsIgnoreCase("gprs/umts preferred")) { //here there is no data connection
                                                    //it is not possible to send the form by network do u want to send it by sms?
                                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormListCompletedActivity.this);
                                                    alertDialogBuilder.setTitle("Connection not present!");
                                                    alertDialogBuilder
                                                            .setMessage("do you want to send the form by sms?")
                                                            .setPositiveButton(
                                                                    R.string.positive_choise,
                                                                    new DialogInterface.OnClickListener() {
                                                                        public void onClick(
                                                                                DialogInterface dialog,
                                                                                int whichButton) {
                                                                            dialog.dismiss();
                                                                            sendSMS(numModem,
                                                                                    encodeXml,
                                                                                    FormListCompletedActivity.this);
                                                                        }
                                                                    }
                                                            )
                                                            .setNegativeButton(
                                                                    R.string.negative_choise,
                                                                    new DialogInterface.OnClickListener() {
                                                                        public void onClick(
                                                                                DialogInterface dialog,
                                                                                int whichButton) {
                                                                            dialog.dismiss();
                                                                        }
                                                                    }
                                                            )

                                                            .show();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        //}	LL per form di test
                                    }







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


}
