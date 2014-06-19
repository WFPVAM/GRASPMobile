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
 * Class that get from the DB the form and make it compilable
 *
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
	MyCallback callback;
	
	 public DownloadSmsTask(Context context, String item,String formname,String formid, String group, MyCallback callback) {
	    	this.context = context;
	    	this.item = item;
	    	this.formname = formname;
	    	this.formid = formid;
	    	this.group = group;
	    	this.callback = callback;
		}
	
	@Override
    protected void onPreExecute() {
    	progressDialog = ProgressDialog.show(context,"Saving Form...", "Wait...");
    }
	
	@Override
	protected Boolean doInBackground(Void... params) {
		Boolean value = false;
		try{
			File myfile = new File (Collect.FORMS_PATH +"/"+formname+".xml");
			myfile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myfile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut,"UTF-8");
			myOutWriter.append(item.toString());
			myOutWriter.close();
			fOut.close();
			
			/**
			 * gestione del mese
			 */
			//--------------------------------------------------------------------------------------
			Calendar rightNow = Calendar.getInstance();
			java.text.SimpleDateFormat month = new java.text.SimpleDateFormat("MM");
			//----------------------------------------------------------------------------------------
			
			/**
			 * data di importazione
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
			 * creo transformer factory
			 */
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer m = tf.newTransformer();
			m.setOutputProperty(OutputKeys.METHOD, "xml");
			m.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			/**
			 * creo stringwriter
			 */
			StringWriter swBuffer = new StringWriter();
			/**
			 * creo filewriter
			 */
			FileWriter fw = new FileWriter(myfile.getAbsolutePath());
			fw.write((swBuffer.toString()));
			/**
			 * creo stremresult
			 */
			StreamResult result = new StreamResult(fw);
			/**
			 * utilizzo il trasform pre creare il file
			 */
			m.transform(source, result);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		
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
		it.fabaris.wfp.provider.MessageProvider.DatabaseHelper dbh2 = new it.fabaris.wfp.provider.MessageProvider.DatabaseHelper("message.db");
	    String updatequery = "UPDATE message SET formImported='si' WHERE formName = '"+formname+"'";
	    dbh2.getReadableDatabase().execSQL(updatequery);
	    dbh2.close();
	    value=true;
		return value;
	}

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