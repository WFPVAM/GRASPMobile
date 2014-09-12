package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.listener.MyCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Class that get from the DB the form and make it compilable. 
 * This class is called when the user wants to move a form from 
 * the synchronized forms list to the new forms list.
 */

public class DownloadSmsTask extends AsyncTask<Void, Void, Boolean> {
    ProgressDialog progressDialog;
    private Context context;
    private String item;
    private String formname;
    private String formid;
    private String group;
    private String pathxml;
    private String data;
    MyCallback callback;//create the list of synchronized once a form has been moved to the list of new

    /**
     * initialize all the data needed to make the form compilable
     * @param context
     * @param item
     * @param formname
     * @param formid
     * @param group
     * @param callback
     */
    public DownloadSmsTask(Context context, String item,String formname,String formid, String group, MyCallback callback) {
        this.context = context;
        this.item = item;//xml of the form
        this.formname = formname;//form name
        this.formid = formid;
        this.group = group;
        this.callback = callback;//create the list of synchronized once a form has been moved to the list of new
    }

    /**
     * show the progress dialog
     */
    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(context,"Saving Form...", "Wait...");
    }

    /**
     * Create an xml file called with the same name of the
     * form and put it in the "forms" folder in the app's file system.
     * Insert a new row in forms db and set the state of the form at new,
     * then update the state of the form at "yes" in the message db.
     * Now the form is read to be compiled.
     * And will appear in the new forms list.
     */
    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean value = false;

        //create the xml file called with the same name of the form
        //and put it in the "forms" folder in the app's file system
        try{
            File myfile = new File (Collect.FORMS_PATH +"/"+formname+".xml");
            myfile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myfile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut,"UTF-8");
            myOutWriter.append(item.toString());
            myOutWriter.close();
            fOut.close();

            //month
            //--------------------------------------------------------------------------------------
            Calendar rightNow = Calendar.getInstance();
            java.text.SimpleDateFormat month = new java.text.SimpleDateFormat("MM");
            //----------------------------------------------------------------------------------------

            /**
             * importing date
             */
            GregorianCalendar gc = new GregorianCalendar();
            String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
            String year = Integer.toString(gc.get(Calendar.YEAR));

            data = day + "/" + month.format(rightNow.getTime()) + "/" + year;

            pathxml = myfile.getPath();
            String version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0 ).versionName;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(item));
            Document doc = builder.parse(inStream);
            doc.getDocumentElement().normalize();
            DOMSource source = new DOMSource(doc);
            NodeList list = doc.getElementsByTagName("client_version_3");
            Node nodetext = doc.createTextNode(version);
            Node node = null;
            for (int i=0; i<list.getLength(); i++)
            {
                node = (Node)list.item(i);
                node.appendChild(nodetext);
            }
            /**
             * create transformer factory
             */
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer m = tf.newTransformer();
            m.setOutputProperty(OutputKeys.METHOD, "xml");
            m.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            /**
             * create stringwriter
             */
            StringWriter swBuffer = new StringWriter();
            /**
             * create filewriter
             */
            FileWriter fw = new FileWriter(myfile.getAbsolutePath());
            fw.write((swBuffer.toString()));
            /**
             * create stremresult
             */
            StreamResult result = new StreamResult(fw);
            /**
             * use the trasformer to create the file
             */
            m.transform(source, result);
        }
        catch(Exception e){
            e.printStackTrace();
        }
		
		/*
		 * update forms db 
		 */
        it.fabaris.wfp.provider.FormProvider.DatabaseHelper dbh = new it.fabaris.wfp.provider.FormProvider.DatabaseHelper("forms.db");
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
                "instanceFilePath," +
                "language)" +
                "VALUES" +
                "('new','"+formname+"','','"+group+"','"+formid+"','"+pathxml+"','','','','"+data+"','','','','','','','','IT')";
        dbh.getWritableDatabase().execSQL(query);
        dbh.close();
		/*
		 *update message db 
		 */
        it.fabaris.wfp.provider.MessageProvider.DatabaseHelper dbh2 = new it.fabaris.wfp.provider.MessageProvider.DatabaseHelper("message.db");
        String updatequery = "UPDATE message SET formImported='si' WHERE formName = '"+formname+"'";
        dbh2.getReadableDatabase().execSQL(updatequery);
        dbh2.close();
        value=true;
        return value;
    }

    /**
     * draw the list of synchronized forms, once that a
     * form has been moved in the list of the new forms
     */
    @Override
    protected void onPostExecute(Boolean value) {
        if (progressDialog.isShowing()&&value==true) {
            progressDialog.dismiss();
            if(callback!= null){
                callback.callbackCall();
            }
        }
    }
}