/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 ******************************************************************************/
package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.listener.FormSavedListener;
import it.fabaris.wfp.logic.FormController;
import it.fabaris.wfp.provider.FormProviderAPI;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.provider.FormProviderAPI.FormsColumns;
import it.fabaris.wfp.utility.EncryptionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import utils.ApplicationExt;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for saving a form,
 * in every case it is needed 
 * during the compilation process.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */

public class SaveToDiskTask extends AsyncTask<Void, String, Integer> {
    private final static String t = "SaveToDiskTask";

    private FormSavedListener mSavedListener;
    private Boolean mSave;
    private Boolean mMarkCompleted;
    private Uri mUri;
    private String mInstanceName;

    public static final int SAVED = 500;
    public static final int SAVE_ERROR = 501;
    public static final int VALIDATE_ERROR = 502;
    public static final int VALIDATED = 503;
    public static final int SAVED_AND_EXIT = 504;

    public String substr;
    public String author;


    /**
     *
     * @param uri
     * @param saveAndExit
     * @param markCompleted
     * @param updatedName
     */
    public SaveToDiskTask(Uri uri, Boolean saveAndExit, Boolean markCompleted, String updatedName) {
        mUri = uri;
        mSave = saveAndExit;
        mMarkCompleted = markCompleted;
        mInstanceName = updatedName;
    }


    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or from XML. If given
     * an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected Integer doInBackground(Void... nothing) {

        /**
         *  validation failed, pass specific failure
         */
        int validateStatus = validateAnswers(mMarkCompleted);
        if (validateStatus != VALIDATED) {
            return validateStatus;
        }

        FormEntryActivity.mFormController.postProcessInstance();

        if (exportData(mMarkCompleted)) {
            return mSave ? SAVED_AND_EXIT : SAVED;
        }

        return SAVE_ERROR;

    }

    private void updateInstanceDatabase(boolean incomplete, boolean canEditAfterCompleted) {

        /**
         *  Update the instance database...
         */
        ContentValues values = new ContentValues();
        if (mInstanceName != null) {
            values.put(FormsColumns.DISPLAY_NAME, mInstanceName);
        }
        if (incomplete || !mMarkCompleted)
        {
            values.put(FormsColumns.STATUS, FormProviderAPI.STATUS_SAVED);
        }
        else
        {
            values.put(FormsColumns.STATUS, FormProviderAPI.STATUS_COMPLETED);
        }
        /**
         *  update this whether or not the status is complete...
         */
        values.put(FormsColumns.CAN_EDIT_WHEN_COMPLETE, Boolean.toString(canEditAfterCompleted));
        
        /*
         * MAGE DATE 
         * manage month
         */
        //--------------------------------------------------------------------------------------
        Calendar rightNow = Calendar.getInstance();
        java.text.SimpleDateFormat month = new java.text.SimpleDateFormat("MM");
        //----------------------------------------------------------------------------------------
		
		/*
		 * importing date
		 */
        GregorianCalendar gc = new GregorianCalendar();
        String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
        /**
         * add one because GregoriaCalendar months begin from 0
         */
        String year = Integer.toString(gc.get(Calendar.YEAR));

        String data = day + "/" + month.format(rightNow.getTime()) + "/" + year;
        String time = getCurrentTimeStamp();
        data = data+"  "+time;

        //------------------------------------------------------------------------------------------
        String formid = FormEntryActivity.formId;;

        String autoGeneratedName = FormEntryActivity.formName;
        //----------------
        String nome_form_saved = autoGeneratedName;
        //----------------
        String strgroup = calculateGroup(formid);
        int igroup = Integer.parseInt(strgroup);



        try{
            File fXmlFile = new File(FormEntryActivity.mInstancePath);
            Log.i("mInstancePathinSavetodt",FormEntryActivity.mInstancePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression inputcount = xpath.compile("//data/*");
            NodeList nodes = (NodeList) inputcount.evaluate(doc,XPathConstants.NODESET);
            for(int i = 0; i<=igroup;i++)
            {
                substr = (nodes.item(i).getTextContent());
                String nodename = (nodes.item(i).getNodeName());
                if(nodename.equalsIgnoreCase("enumerator_1"))
                {
                    author = substr;

                    Log.i("author",author);

                    //String saved_id = nome_form_saved+"_"+formid;
                    //ApplicationExt.getDatabaseAdapter().open().delete("SAVED", saved_id);
                    //ApplicationExt.getDatabaseAdapter().open().insert("SAVED", saved_id, nome_form_saved, data, author);
                }
                else if(!(nodename.equalsIgnoreCase("id")||nodename.equalsIgnoreCase("des_version_2")||nodename.equalsIgnoreCase("client_version_3")))
                {
                    int num = 0;
                    try
                    {
                        String conf = nodes.item(i).getNodeName();
                        conf = conf.split("\\_", 1000)[conf.split("\\_", 1000).length-1];
                        num = Integer.parseInt(conf);
                    }
                    catch(Exception e)
                    {
                        break;
                    }
                    if(num<igroup)
                    {
                        if(!(substr.equalsIgnoreCase("")))
                        {
                            autoGeneratedName = autoGeneratedName+"&"+substr;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        if(instanceIfExist(mInstanceName)){
            DatabaseHelper dbh = new DatabaseHelper("forms.db");
            String updatequery = "UPDATE forms SET instanceFilePath='"+FormEntryActivity.mInstancePath+"' WHERE displayNameInstance = '"+mInstanceName+"' AND status='saved'";
            String updatequery1 = "UPDATE forms SET displaySubtext='"+autoGeneratedName+"' WHERE displayNameInstance = '"+mInstanceName+"' AND status='saved'";
            dbh.getReadableDatabase().execSQL(updatequery);
            dbh.getReadableDatabase().execSQL(updatequery1);

            //-----------------------------------------------
		    
		    /*
		    String saved_id = nome_form_saved+"_"+author;
		    ApplicationExt.getDatabaseAdapter().open().delete("SAVED", saved_id);
		    ApplicationExt.getDatabaseAdapter().close();
		    ApplicationExt.getDatabaseAdapter().open().insert("COMPLETED", saved_id, nome_form_saved, data, author);
		    ApplicationExt.getDatabaseAdapter().close();
		    */
            //ApplicationExt.getDatabaseAdapter().open().insert("SAVED", nome_form_saved+"_"+formid, nome_form_saved, data, author);
            //-----------------------------------------------

            dbh.close();
        }
        else{
            //-----------------------------------------------------
            //ApplicationExt.getDatabaseAdapter().open().insert("SAVED", nome_form_saved+"&"+formid, nome_form_saved, data, author); LL 14-05-2014 commentate perche' il grasp db e' stato rimosso e le info sono state sposate sul db forms
            //ApplicationExt.getDatabaseAdapter().open().insert("SAVED", nome_form_saved+"&"+formid, mInstanceName, data, author);  LL 14-05-2014 commentate perche' il grasp db e' stato rimosso e le info sono state sposate sul db forms
            //ApplicationExt.getDatabaseAdapter().close();
            //-----------------------------------------------------

            DatabaseHelper dbh = new DatabaseHelper("forms.db");
            String query = "INSERT INTO forms" +
                    "(status," +
                    "displayName," +
                    "displayNameInstance," +
                    "description," +
                    "jrFormId," +
                    "formFilePath," +
                    "base64RsaPublicKey," +
                    "displaySubtext," +
                    "md5Hash," +
                    "date," +
                    "jrcacheFilePath," +
                    "formMediaPath," +
                    "modelVersion," +
                    "uiVersion," +
                    "submissionUri," +
                    "canEditWhenComplete," +

                    "enumeratorID," +          //LL 14-05-2014 added after the deleting of the grasp db
                    "formNameAndXmlFormid," +  //LL 14-05-2014 added after the deleting of the grasp db


                    "instanceFilePath," +
                    "language)" +
                    "VALUES" +
                    "('saved','"+FormEntryActivity.formName+"','"+mInstanceName+"','','"+formid+"','"+FormEntryActivity.mFormPath+"','','"+autoGeneratedName+"','','"+data+"','','','','','','','"+author+"','"+nome_form_saved+"&"+formid + "','"+FormEntryActivity.mInstancePath+"','IT')";
            dbh.getWritableDatabase().execSQL(query);
            dbh.close();
        }
    }

    private String calculateGroup(String jrFormId)
    {
        String formId[];
        String group = null;
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT description FROM forms WHERE status = 'new' and jrFormId = '"+jrFormId+"'";

        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        try
        {
            formId= new String[c.getCount()];
            if (c.moveToFirst())
            {
                do
                {
                    formId[c.getPosition()] = c.getString(0);
                    group = formId[c.getPosition()];
                }
                while(c.moveToNext());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if ( c != null )
            {
                c.close();
                dbh.close();
            }
        }
        return group;
    }

    public Boolean instanceIfExist(String instanceName)
    {
        String formId[];
        Boolean res = false;
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String query = "SELECT displayNameInstance FROM forms WHERE status = 'saved'";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        try
        {
            formId= new String[c.getCount()];
            if (c.moveToFirst())
            {
                do
                {
                    formId[c.getPosition()] = c.getString(0);
                    if(formId[c.getPosition()].equalsIgnoreCase(instanceName))
                    {
                        res = true;
                    }
                }
                while(c.moveToNext());
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if ( c != null )
            {
                c.close();
                dbh.close();
            }
        }
        return res;
    }
    /**
     * Write's the data to the sdcard, and updates the instances content provider.
     * In theory we don't have to write to disk, and this is where you'd add
     * other methods.
     * @param markCompleted
     * @return
     */
    private boolean exportData(boolean markCompleted)
    {
        ByteArrayPayload payload;
        try
        {
            // assume no binary data inside the model.
            FormInstance datamodel = FormEntryActivity.mFormController.getInstance();
            XFormSerializingVisitor serializer = new XFormSerializingVisitor();
            payload = (ByteArrayPayload) serializer.createSerializedPayload(datamodel);

            // write out xml
            exportXmlFile(payload, FormEntryActivity.mInstancePath);

        }
        catch (IOException e)
        {
            Log.e(t, "Error creating serialized payload");
            e.printStackTrace();
            return false;
        }

        // update the mUri. We have exported the reloadable instance, so update status...
        // Since we saved a reloadable instance, it is flagged as re-openable so that if any error 
        // occurs during the packaging of the data for the server fails (e.g., encryption),
        // we can still reopen the filled-out form and re-save it at a later time.
        updateInstanceDatabase(true, true);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        if ( markCompleted )
        {
            // now see if the packaging of the data for the server would make it
            // non-reopenable (e.g., encryption or send an SMS or other fraction of the form).
            boolean canEditAfterCompleted = FormEntryActivity.mFormController.isSubmissionEntireForm();
            boolean isEncrypted = false;

            // build a submission.xml to hold the data being submitted 
            // and (if appropriate) encrypt the files on the side

            // pay attention to the ref attribute of the submission profile...
            try
            {
                payload = FormEntryActivity.mFormController.getSubmissionXml();
            }
            catch (IOException e)
            {
                Log.e(t, "Error creating serialized payload");
                e.printStackTrace();
                return false;
            }
            File instanceXml = new File(FormEntryActivity.mInstancePath);
            File submissionXml = new File(instanceXml.getParentFile(), "submission.xml");
            // write out submission.xml -- the data to actually submit to aggregate
            exportXmlFile(payload, submissionXml.getAbsolutePath());

            // see if the form is encrypted and we can encrypt it...
////            EncryptedFormInformation formInfo = EncryptionUtils.getEncryptedFormInformation(mUri,FormEntryActivity.mFormController.getSubmissionMetadata());
//            if ( formInfo != null ) {
//                // if we are encrypting, the form cannot be reopened afterward
//                canEditAfterCompleted = false;
//                // and encrypt the submission (this is a one-way operation)...
//                if ( !EncryptionUtils.generateEncryptedSubmission(instanceXml, submissionXml, formInfo) ) {
//                    return false;
//                }
//                isEncrypted = true;
//            }

            // At this point, we have:
            // 1. the saved original instanceXml, 
            // 2. all the plaintext attachments
            // 2. the submission.xml that is the completed xml (whether encrypting or not)
            // 3. all the encrypted attachments if encrypting (isEncrypted = true).
            //
            // NEXT:
            // 1. Update the instance database (with status complete).
            // 2. Overwrite the instanceXml with the submission.xml 
            //    and remove the plaintext attachments if encrypting

            updateInstanceDatabase(false, canEditAfterCompleted);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            if (  !canEditAfterCompleted )
            {
                // AT THIS POINT, there is no going back.  We are committed
                // to returning "success" (true) whether or not we can
                // rename "submission.xml" to instanceXml and whether or
                // not we can delete the plaintext media files.
                //
                // Handle the fall-out for a failed "submission.xml" rename
                // in the InstanceUploader task.  Leftover plaintext media
                // files are handled during form deletion.

                // delete the restore Xml file.
                if ( !instanceXml.delete() ) {
                    Log.e(t, "Error deleting " + instanceXml.getAbsolutePath()
                            + " prior to renaming submission.xml");
                    return true;
                }

                // rename the submission.xml to be the instanceXml
                if ( !submissionXml.renameTo(instanceXml) ) {
                    Log.e(t, "Error renaming submission.xml to " + instanceXml.getAbsolutePath());
                    return true;
                }
            }
            else
            {
                // try to delete the submissionXml file, since it is
                // identical to the existing instanceXml file
                // (we don't need to delete and rename anything).
                if ( !submissionXml.delete() ) {
                    Log.w(t, "Error deleting " + submissionXml.getAbsolutePath()
                            + " (instance is re-openable)");
                }
            }

            // if encrypted, delete all plaintext files
            // (anything not named instanceXml or anything not ending in .enc)
            if ( isEncrypted ) {
                if ( !EncryptionUtils.deletePlaintextFiles(instanceXml) ) {
                    Log.e(t, "Error deleting plaintext files for " + instanceXml.getAbsolutePath());
                }
            }
        }
        return true;
    }
    /**
     * This method actually writes the xml to disk.
     * @param payload
     * @param path
     * @return
     */
    private boolean exportXmlFile(ByteArrayPayload payload, String path) {
        // create data stream
        InputStream is = payload.getPayloadStream();
        int len = (int) payload.getLength();

        // read from data stream
        byte[] data = new byte[len];
        try {
            int read = is.read(data, 0, len);
            if (read > 0) {
                // write xml file
                try {
                    // String filename = path + File.separator +
                    // path.substring(path.lastIndexOf(File.separator) + 1) + ".xml";
                    FileWriter fw = new FileWriter(path);
                    fw.write(new String(data, "UTF-8"));
                    fw.flush();
                    fw.close();
                    return true;

                } catch (IOException e) {
                    Log.e(t, "Error writing XML file");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(t, "Error reading from payload data stream");
            e.printStackTrace();
            return false;
        }

        return false;
    }
    @Override
    protected void onPostExecute(Integer result) {
        synchronized (this) {
            if (mSavedListener != null)
                mSavedListener.savingComplete(result);
        }
    }
    public void setFormSavedListener(FormSavedListener fsl) {
        synchronized (this) {
            mSavedListener = fsl;
        }
    }
    /**
     * Goes through the entire form to make sure all entered answers comply with their constraints.
     * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
     * allow saving to disk, though, until all answers conform to their constraints/requirements.
     *
     * @param markCompleted
     * @return validatedStatus
     */
    private int validateAnswers(Boolean markCompleted) {
        FormIndex i = FormEntryActivity.mFormController.getFormIndex();
        FormEntryActivity.mFormController.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        int event;
        while ((event =
                FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP)) != FormEntryController.EVENT_END_OF_FORM) {
            if (event != FormEntryController.EVENT_QUESTION) {
                continue;
            } else {
                int saveStatus =
                        FormEntryActivity.mFormController
                                .answerQuestion(FormEntryActivity.mFormController.getQuestionPrompt()
                                        .getAnswerValue());
                if (markCompleted && saveStatus != FormEntryController.ANSWER_OK) {

                    Log.e("- validateAnswer() -", "effettuata validazione delle risposte e salvataggio dello stato della form");
                    return saveStatus;
                }
            }
        }

        FormEntryActivity.mFormController.jumpToIndex(i);
        return VALIDATED;
    }

    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
