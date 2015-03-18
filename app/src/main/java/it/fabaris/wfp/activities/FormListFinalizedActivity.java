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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.parse.XFormParser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import content.FormPendingAdapter;
import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.provider.FormProvider;
import it.fabaris.wfp.task.HttpCheckAndSendPostTask;
import it.fabaris.wfp.task.HttpSendAllImages;
import it.fabaris.wfp.utility.FileUtils;
import object.FormInnerListProxy;
import utils.ApplicationExt;

/**
 * Class that defines the tab for the list of the finalized forms
 *
 */
public class FormListFinalizedActivity extends Activity implements MyCallback
{
    public interface FormListHandlerFinalized
    {
        public ArrayList<FormInnerListProxy> getFinalizedForm();
        public void catchCallBackFinalized(String[] finalized);
    }
    public FormListHandlerFinalized formListHandler;


    private FormPendingAdapter adapter;

    private ArrayList<FormInnerListProxy> finalizzate;
    private ListView listview;

    private SharedPreferences settings;
    private String numClient;
    private String numModem;
    private String encodedImage;
    private String httpServer;
    private String senderPhone;
    public  String nomeform;
    public  String autore;
    boolean formHasImages=true;
   // public String idFormNameInstance;
    public static ArrayList<String> istance;



    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabpending);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        finalizzate = new ArrayList<FormInnerListProxy>();
        finalizzate = getIntent().getExtras().getParcelableArrayList("finalized");
        istance = new ArrayList<String>();

        listview = (ListView)findViewById(R.id.listViewPending);
        listview.setCacheColorHint(00000000);
        listview.setClickable(true);

        adapter = new FormPendingAdapter(this, finalizzate);
        listview.setAdapter(adapter);

         /****   Settings    ****/
        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        numClient = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE, getString(R.string.default_client_telephone));
        senderPhone = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE, getString(R.string.default_client_telephone));
        numModem = settings.getString(PreferencesActivity.KEY_SERVER_TELEPHONE, getString(R.string.default_server_telephone));
        httpServer = settings.getString(PreferencesActivity.KEY_SERVER_URL,getString(R.string.default_server_url));

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Button  sendImages =(Button) findViewById(R.id.send_img);
        sendImages.setClickable(true);

        sendImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkConnected()) {

                    Toast.makeText(getApplicationContext(), "Connection not prestent!", Toast.LENGTH_LONG).show();
                }
                else{
                sendImagesInList(finalizzate);
                adapter.notifyDataSetInvalidated();
                adapter.notifyDataSetChanged();
            }}

        });

        listview.setOnItemClickListener(new OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {



            }

        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                //idFormNameInstance = completed.get(position).getFormName();
                builder.setMessage(getString(R.string.send_images))
                        .setCancelable(false)
                        .setPositiveButton(R.string.positive_choise,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.dismiss();
                                        //LL 14-05-2014 db grasp dismesso non ci sono piu' problemi di allineamento
                                        //ArrayList<FormInnerListProxy> mycomplete = getCompleteParceableList();//LL aggiunti perche' qui non avevo visibilita' sulle variabili d'istanza
                                        ArrayList<FormInnerListProxy> myFinalized = getFinalizedParceableList();//LL aggiunti perche' qui non avevo visibilita' sulle variabili d'istanza

                                        // idFormNameInstance = myFinalized.get(position).getFormName();
                                        String str = myFinalized.get(position).getStrPathInstance();
                                        byte[] fileBytes = FileUtils.getFileAsBytes(new File(str));
                                        TreeElement dataElements = XFormParser.restoreDataModel(fileBytes, null).getRoot();
                                        String imageName = "";
                                        String encodedImage = "";
                                        int imageNameCount = 0;

                                        for (int j = 0; j < dataElements.getNumChildren(); j++) {
                                            if (dataElements.getChildAt(j) != null && dataElements.getChildAt(j).getValue() != null && dataElements.getChildAt(j).getValue().getDisplayText().indexOf("jpg") > 0) {
                                                imageName = dataElements.getChildAt(j).getValue().getDisplayText();
                                                imageNameCount++;
                                                if (imageName.contains("/instances")) {
                                                    imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
                                                }

                                                String imagePath = str.substring(0, str.lastIndexOf("/") + 1) + imageName;
//                                                Bitmap originalImage = BitmapFactory.decodeFile(imagePath);
//                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                                                originalImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
////                                                byte[] imageBytes = baos.toByteArray();
////                                                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//                                                byte[] compresedImageBytes = FileUtils.compressImage(imagePath,0,0,80);
//                                                 encodedImage = Base64.encodeToString(compresedImageBytes, Base64.DEFAULT);
                                                byte[] compresedImageBytes = FileUtils.compressImage(imagePath,1600,1200,80);
                                                 encodedImage = Base64.encodeToString(compresedImageBytes, Base64.DEFAULT);



                                                ///////////////////////////////////////////////////////////////c

//                                                String str1[] = str.replace("/storage/emulated/0/GRASP/instances/", "").split("/");
//                                                String formId = str1[0] + "_img"+ "_" + imageName ;
                                                  String str1[] = str.substring(str.lastIndexOf("instances")).split("/");
                                                  String formId = str1[1] + "_" + imageName + "_image" ;

                                                 istance.clear();

                                                if (isNetworkConnected()) {
                                                    try {

                                                        istance.add(myFinalized.get(position).getFormNameInstance());
                                                        sendWithNetwork(
                                                                FormListFinalizedActivity.this,
                                                                httpServer,
                                                                numClient,
                                                                encodedImage,
                                                                FormListFinalizedActivity.this, formId);
                                                    } catch (InterruptedException e) {
                                                        // TODO
                                                        // Auto-generated
                                                        // catch
                                                        // block
                                                        e.printStackTrace();
                                                    }
                                                    adapter.notifyDataSetInvalidated();
                                                    adapter.notifyDataSetChanged();


                                                } else if (!isNetworkConnected()) {
//                                                    try {
//
//                                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormListFinalizedActivity.this);
//
//                                                        alertDialogBuilder.setMessage("Connection not present!");
//                                                        alertDialogBuilder.show();
//
//                                                    } catch (Exception e) {
//                                                        e.printStackTrace();
//                                                    }
                                                    Toast.makeText(getApplicationContext(), "Connection not prestent!", Toast.LENGTH_LONG).show();
                                                }
                                                //}	LL per form di test
                                            }
                                            //check if it has videos
                                            /*********************************************/

                                             if (dataElements.getChildAt(j) != null && dataElements.getChildAt(j).getValue() != null && dataElements.getChildAt(j).getValue().getDisplayText().indexOf("mp4") > 0) {


                                             }
                                          /***************************************************/
                                        }
                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.negative_choise),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.dismiss();
                                    }
                                }
                        );
                AlertDialog alert = builder.create();
                alert.show();
                //finish();
                return false;
            }

        });




//****************************************************************************************************************
        adapter.notifyDataSetInvalidated();
        adapter.notifyDataSetChanged();

    }

//    private String getFormImages(String formName) {
//        byte[] fileBytes = FileUtils.getFileAsBytes(new File(formName));
//        TreeElement dataElements = XFormParser.restoreDataModel(fileBytes, null).getRoot();
//        String imageName = "";
//        String encodedImage="";
//     //   int imageNameCount =0;
//
//        for (int j = 0; j < dataElements.getNumChildren(); j++) {
//            if (dataElements.getChildAt(j) != null && dataElements.getChildAt(j).getValue() != null && dataElements.getChildAt(j).getValue().getDisplayText().indexOf("jpg") > 0) {
//                imageName = dataElements.getChildAt(j).getValue().getDisplayText();
//             //   imageNameCount++;
//                if (imageName.contains("/instances")) {
//                    imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
//                }
//
//                String imagePath = formName.substring(0, formName.lastIndexOf("/") + 1) + imageName;
//                Bitmap originalImage = BitmapFactory.decodeFile(imagePath);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                originalImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] imageBytes = baos.toByteArray();
//                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//
//                //  String formNameImage= formName + "_image"+imageNameCount;
//            }}
//            return encodedImage;
//
//        }

    private void sendImagesInList(ArrayList<FormInnerListProxy> finalizzate) {

        if (httpServer.equalsIgnoreCase("") || httpServer == null) {
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.server_url_not_inserted), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 40, 40);
            toast.show();
        } else {
           HttpSendAllImages asyncTask = new HttpSendAllImages(FormListFinalizedActivity.this ,httpServer, numClient, finalizzate, FormListFinalizedActivity.this);//chiamo l'async task con la lista di tutte le form da inviare

            asyncTask.execute();
        }

    }


    /**
     *
     * @return
     */
    private ArrayList<FormInnerListProxy> queryFinalizedForm()
    {
        formListHandler = new FormListActivity();
        ArrayList<FormInnerListProxy> finalizzate = formListHandler.getFinalizedForm();

        return finalizzate;
    }

    public void onResume()
    {
        super.onResume();
       getFormsDataFinalized();
        runOnUiThread(new Runnable() {
            public void run() {
                FormPendingAdapter adapter = (FormPendingAdapter) listview
                        .getAdapter();
                adapter.notifyDataSetChanged();

            }
        });
    }
        @Override
    public void callbackCall()
    {
		/*
		formNameAutoGenFinalizzata = formListHandler.getFinalizedForm().get(5);
		formListHandler.catchCallBackFinalized(formNameAutoGenFinalizzate);
		listview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		finish();
		*/
    }

    public void onDestroy()
    {
        listview.setAdapter(null);
        super.onDestroy();
    }
    @Override
    public void finishFormListCompleted() {
        // TODO Auto-generated method stub

    }
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

    private void sendWithNetwork(Context context, String url, String number,
                                 String form, MyCallback callback,String formId) throws InterruptedException {

        if (httpServer.equalsIgnoreCase("") || httpServer == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getString(R.string.server_url_not_inserted),
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 40, 40);
            toast.show();
        } else {

            HttpCheckAndSendPostTask asyncTask = new HttpCheckAndSendPostTask(context, url, number, form, callback, false,formId, formHasImages);
            asyncTask.execute();
            if (asyncTask.getStatus() != HttpCheckAndSendPostTask.Status.FINISHED) {

            }
        }
    }

    private ArrayList<FormInnerListProxy> getFinalizedParceableList() {
        ArrayList<FormInnerListProxy> myFinalized = this.finalizzate;
        return myFinalized;

    }
    public static void updateFormToSubmitted() {
    for (int k = 0; k < istance.size(); k++){
//

            Calendar rightNow = Calendar.getInstance();
            java.text.SimpleDateFormat month = new java.text.SimpleDateFormat(
                    "MM");
            // ----------------------------------------------------------------------------------------
            /**
             *  data di importazione
             */
            GregorianCalendar gc = new GregorianCalendar();
            String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));

            String year = Integer.toString(gc.get(Calendar.YEAR));

            String time = getCurrentTimeStamp();//LL

           String  data = day + "/" + month.format(rightNow.getTime()) + "/" + year;

            data = data + "  " + time;//LL
            // -----------------------------------------------------

            FormProvider.DatabaseHelper dbh = new FormProvider.DatabaseHelper("forms.db");
            String updatequery = "UPDATE forms SET status='submitted', submissionDate = '" + data + "'  WHERE displayNameInstance = '"
                    + istance.get(k) + "'";

            Log.i("FUNZIONE updateFormToSubmitted per la form: ",
                    istance.get(k));

            dbh.getReadableDatabase().execSQL(updatequery);

            dbh.close();

        }

    }
    public void finishFormListFinalized() {
        finish();

    }
    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
    public void getFormsDataFinalized() {
        finalizzate.clear();
        // dbAdapter.open();


        FormProvider.DatabaseHelper dbh = new FormProvider.DatabaseHelper("forms.db");
        String query = "SELECT formFilePath,displayName,instanceFilePath,displayNameInstance,displaySubtext,completedDate,formNameAndXmlFormid,enumeratorID" +
                " FROM forms WHERE status = 'finalized' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        int quanteComplete = 0;
        try {
            finalizzate.clear();
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


                    finalizzate.add(completa);

                } while (c.moveToNext());
            }
            quanteComplete = finalizzate.size();

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

}
