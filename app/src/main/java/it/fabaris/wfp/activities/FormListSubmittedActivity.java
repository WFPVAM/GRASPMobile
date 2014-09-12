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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import content.FormSubmittedAdapter;
import database.DbAdapterGrasp;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.provider.InstanceProviderAPI;
import object.FormInnerListProxy;
import utils.ApplicationExt;

/**
 * Class that defines the tab for the list of the submitted forms
 *
 */

public class FormListSubmittedActivity extends Activity {
    public interface FormListHandlerSubmitted {
        public ArrayList<FormInnerListProxy> getSubmittedForm();
    }

    public FormListHandlerSubmitted formListHandler;

    public int positionInviate;

    public ListView listview;
    private FormSubmittedAdapter adapter;

    private ArrayList<FormInnerListProxy> inviate;
    private ArrayList<FormInnerListProxy> submitted;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabsubmitted);

        inviate = new ArrayList<FormInnerListProxy>();
        inviate = getIntent().getExtras().getParcelableArrayList("submitted");

		/*LL 14-05-2014 eliminata per dismissione del db grasp
		submitted = new ArrayList<FormInnerListProxy>();
		submitted = getIntent().getExtras().getParcelableArrayList("inviate");
		*/

        listview = (ListView) findViewById(R.id.listViewSubmitted);
        listview.setCacheColorHint(00000000);
        listview.setClickable(true);

        final Builder builder = new AlertDialog.Builder(this);

        adapter = new FormSubmittedAdapter(this, inviate);
        listview.setAdapter(adapter);

        /**
         * When the user clicks on one of the items in the submitted forms list,
         * the form is shown in a preview mode in order to see the answers saved in it
         */
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Intent intent = new Intent(getApplicationContext(), FormEntryActivity.class);
                String keyIdentifer = "ciao";
                String keyIdentifer1 = "ciao1";
                String keyIdentifer2 = "ciao2";
                String keyIdentifer3 = "ciao3";
                String pkgName = getPackageName();

                //positionInviate = getRightCompletedParcelableObject(submitted.get(position).getFormName());// per
                // visualizzare
                // la
                // form
                // corretta //LL 14-05-2014 eliminato per dismissione db grasp

                intent.putExtra(pkgName + keyIdentifer, inviate.get(position).getPathForm());
                intent.putExtra(pkgName + keyIdentifer1, inviate.get(position).getFormName());
                intent.putExtra(pkgName + keyIdentifer2, inviate.get(position).getFormNameInstance());
                intent.putExtra(pkgName + keyIdentifer3, inviate.get(position).getFormNameAutoGen());

                intent.putExtra("submitted", true);

                intent.setAction(Intent.ACTION_VIEW);
                String extension = MimeTypeMap.getFileExtensionFromUrl(inviate.get(position).getPathForm()).toLowerCase();
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                intent.setDataAndType(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mimeType);
                startActivity(intent);
            }
        });

        /**
         * implemented to test the function of sending massive form.
         * The form chosen will be duplicated 20 times in the forms db as completed form
         */
        listview.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                boolean debug = false;
                if (debug) {
                    //in the db 20 new rows of completed form
                    it.fabaris.wfp.provider.FormProvider.DatabaseHelper dbh = new it.fabaris.wfp.provider.FormProvider.DatabaseHelper("forms.db");
                    for (int i = 1; i <= 20; i++) {// mette 50 nuove form nello
                        // stato di completate nel
                        // deb
                        // forms.db
                        String xmlpath = ""; // path dell'xml delle risposte
                        //int indexSubmitted = getIndexSubmitted(inviate.get(positionInviate)); //LL 14-05-2014 modificato per dismissione db grasp

                        //FormInnerListProxy form = inviate.get(positionInviate);// prendo
                        FormInnerListProxy form = inviate.get(position);  // la
                        // form
                        //take the xml string of the form
                        String xmlFormInstance = getXmlFormInstance(form);

                        //Delete the last part of the string that contains the FormIdentificator
                        xmlFormInstance = xmlFormInstance.substring(0, xmlFormInstance.indexOf("?formidentificator?"));

                        //change enumerator to string Random or sequential...
                        xmlFormInstance = xmlFormInstance.replace("</enumerator_1>", "_Clone" + Integer.toString(i) + "</enumerator_1>");

                        try {
                            //save the xml in the externalStorage e return back the path of the saved xml
                            xmlpath = saveInstance(form, i, xmlFormInstance);
                        }
                        catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        //create a new folder with a new xml file in the instances folder
                        //insert the form in the forms db
                        String completed_by = inviate.get(position).getFormEnumeratorId();
                        String formname = inviate.get(position).getFormName();
                        String displayNameInstance = inviate.get(position).getFormNameInstance();
                        String formid = inviate.get(position).getFormId();// empty
                        //xml's template path of the form
                        String pathxml = inviate.get(position).getPathForm();
                        String data = inviate.get(position).getDataInvio();
                        //path of the xml file of the answers
                        String myinstanceFilePath = xmlpath;

                        String query = "INSERT INTO forms" + "(status," + "displayName," + "displayNameInstance," + "description," + "jrFormId,"
                                + "formFilePath," + "base64RsaPublicKey," + "displaySubtext," + "md5Hash," + "date," + "jrcacheFilePath," + "formMediaPath,"
                                + "modelVersion," + "uiVersion," + "submissionUri," + "canEditWhenComplete," + "instanceFilePath," + "language)" + "VALUES"
                                + "('completed','" + formname + "','" + displayNameInstance + "','','" + formid + "','" + pathxml + "','','','','" + data
                                + "','','','','','','','" + myinstanceFilePath + "','IT')";
                        dbh.getWritableDatabase().execSQL(query);



                        // metti la form nella tabelle completed del DB grasp //LL 14-05-2014 eliminato per dismissione del db grasp
                        //ApplicationExt.getDatabaseAdapter().open().insert("COMPLETED", displayNameInstance, formname, data, completed_by); LL 14-05-2014 eliminato per dismissione del db grasp
                    }
                    dbh.close();

                }
                return true;
            }
        });

    }

    /**
     * not used
     */
    private ArrayList<FormInnerListProxy> querySubmittedForm() {
        formListHandler = new FormListActivity();
        ArrayList<FormInnerListProxy> inviati = formListHandler.getSubmittedForm();

        return inviati;
    }

    /**
     * set the adapter to see the submitted forms list
     */
    public void onResume() {
        super.onResume();
        getFormsDataSubmitted();
        runOnUiThread(new Runnable() {
            public void run() {
                FormSubmittedAdapter adapter = (FormSubmittedAdapter) listview.getAdapter();
                adapter.notifyDataSetChanged();
            }
        });
    }

	/*LL eliminato per dismissione db grasp
	// LL aggiunto 14-02-14
	// nell' oggetto parcellizzato di Fabaris identifica la posizione dell' item
	// che contiene i dati giusti per visualizzare la form corretta
	private int getRightCompletedParcelableObject(String idFormInFabaris) {// prende
																			// l'identificativo
																			// univoco
																			// della
																			// form

		int posizione = 0;
		// seleziona la posizione nella lista degli oggetti parcellizzati di
		// fabaris che contiene l'id della form collegato all'oggetto
		// parcellizzato cliccato sulla lista
		// delle complete
		for (int i = 0; i < inviate.size(); i++) {
			if (inviate.get(i).getFormNameInstance().equals(idFormInFabaris)) {
				return i;
			}
		}
		return posizione;// restituisce la posizione dell'oggetto parcellizato
							// fabaris cui fa riferimento la form selezionata
							// nella lista delle complete
	}*/

    /**
     * Once fetched all submitted forms from forms db, initializes
     * the global field  "inviate" that represents the complete list of submitted forms
     */
    public void getFormsDataSubmitted() {
        inviate.clear();
        int quanteInviate = 0;



        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT formFilePath, displayName, instanceFilePath, displayNameInstance, displaySubtext, date, formNameAndXmlFormid, enumeratorID,submissionDate FROM forms WHERE status = 'submitted' ORDER BY _id DESC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        try
        {
            if (c.moveToFirst()){
                do
                {
                    FormInnerListProxy inviata = new FormInnerListProxy();
                    inviata.setPathForm(c.getString(0));
                    inviata.setFormName(c.getString(1));
                    inviata.setStrPathInstance(c.getString(2));
                    inviata.setFormNameInstance(c.getString(3));
                    inviata.setFormNameAutoGen(c.getString(4));
                    inviata.setDataInvio(c.getString(5));


                    inviata.setFormNameAndXmlFormid(c.getString(6)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    inviata.setFormEnumeratorId(c.getString(7)); // LL 14-05-2014 aggiunte a seguito dell'eliminazione del db grasp e passaggio dei dati delle tabelle del db grasp in nuovi campi della tabella forms_table_name in forms.db
                    inviata.setSubmissionDate(c.getString(8));


                    inviate.add(inviata);

                }while(c.moveToNext());
            }
            quanteInviate = inviate.size();

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if ( c != null ) {
                c.close();
                dbh.close();
            }
        }

        Cursor cursor = ApplicationExt.getDatabaseAdapter().open().fetchAllSubmitted();
        try {
            while (cursor.moveToNext()) {
                /**
                 * SUBMITTED_FORM_ID_KEY, SUBMITTED_FORM_NOME_FORM,
                 * SUBMITTED_FORM_SUBMITTED_DATA, SUBMITTED_FORM_COMPLETED_DATA,
                 * SUBMITTED_FORM_BY
                 */
                FormInnerListProxy submitted = new FormInnerListProxy();
                submitted.setFormId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_ID_KEY)));
                submitted.setFormName(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_NOME_FORM)));
                submitted.setDataInvio(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_SUBMITTED_DATA)));
                submitted.setFormEnumeratorId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_BY)));
                this.inviate .add(submitted);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
                ApplicationExt.getDatabaseAdapter().close();
            }
        }
    }

    /**
     * not used it uses the grasp db and the grasp db has been dismissed
     */
    public void updateFormsDataToSubmitted(String nome_form, String submitted_data, String submitted_by) {
        /**
         * CARICO IL DB CON I DATI RECUPERATI
         */
        String submitted_id = nome_form + submitted_by;
        ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", nome_form);
        ApplicationExt.getDatabaseAdapter().open().insert("SUBMITTED", submitted_id, nome_form, submitted_data, submitted_by);
        ApplicationExt.getDatabaseAdapter().close();
    }

    /**
     * while distroing the activity set the adapter of the submitted forms to null
     */
    public void onDestroy() {
        listview.setAdapter(null);
        super.onDestroy();
    }

    /**
     * starting from the info taken from the parcelable object, creates the form to send to the server
     * @param form is a FormInnerListProxy, object an object that contains info about the current form
     * 			   such as the enumerator id, form name and so on
     * @return xml the xml with the answers of the form as a string
     */
    private String getXmlFormInstance(FormInnerListProxy form) {
        String xml = null;
        try {
            InputStream fileInput = new FileInputStream(form.getStrPathInstance()); // path[position]);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileInput);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.setOutputProperty(OutputKeys.METHOD, "xml");
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            xml = trasformItem(trans, doc, form);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(xml);
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
    public String trasformItem(Transformer trans, Document doc, FormInnerListProxy form) throws TransformerException {
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, result);
        String xmlString = sw.toString();
        String apos = "apos=\"'\"";
        xmlString = xmlString.replace(apos, "");
        /**
         * add unique code to data xml response
         */
        xmlString = xmlString + "?formidentificator?" + form.getFormNameAutoGen();
        /**
         * add autogenerated name to data xml response
         */
        xmlString = xmlString + "?formname?" + form.getFormNameInstance();
        /**
         * add date and time to data xml response
         */
        GregorianCalendar gc = new GregorianCalendar();
        String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
        String month = Integer.toString(gc.get(Calendar.MONTH));
        String year = Integer.toString(gc.get(Calendar.YEAR));
        String hour = Integer.toString(gc.get(Calendar.HOUR_OF_DAY));
        String date = day + "/" + month + "/" + year;
        return xmlString = xmlString + "?formhour?" + date + "_" + hour;
    }

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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * create and save xml file with the answers of the form
     * @param form the form as ForInnerListProxy object
     * @param folderWrittenCounter
     * @param xmltoWrite the xml with the answeres as a string
     * @return
     * @throws IOException
     */
    private String saveInstance(FormInnerListProxy form, int folderWrittenCounter, String xmltoWrite) throws IOException {
        // creo la cartella che conterra' l'xml delle risposte
        String instancesfolderpath = Environment.getExternalStorageDirectory() + "/GRASP/instances/";
        String folderandfilename = form.getFormName() + "_" + folderWrittenCounter;
        String fileInstancePath = instancesfolderpath + folderandfilename + "/" + folderandfilename + ".xml";

        File f = new File(instancesfolderpath, folderandfilename);
        boolean haswritten = f.mkdirs();

        // scrivo il file e lo metto nella cartella
        if (haswritten) {
            File xmlformfile = new File(f, folderandfilename + ".xml");// creo
            // il
            // file
            // xml
            // nella
            // cartella
            FileWriter writer = new FileWriter(xmlformfile);
            writer.append(xmltoWrite);
            writer.flush();
            writer.close();
        }
        return fileInstancePath;
    }

    /**
     * not in use
     */
    private int getIndexSubmitted(FormInnerListProxy forminviata) {
        int index = -1;

        String formNameInstanceinviate = forminviata.getFormNameInstance();
        for (int i = 0; i < submitted.size(); i++) {
            if (submitted.get(i).getFormName().toString().contains(formNameInstanceinviate)) {
                index = i;
            }
        }
        return index;
    }

}
