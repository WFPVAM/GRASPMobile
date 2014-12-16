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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.james.mime4j.codec.DecoderUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import content.FormSavedAdapter;
import content.FormSubmittedAdapter;
import database.DbAdapterGrasp;

import object.FormInnerListProxy;
import utils.ApplicationExt;

import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.provider.InstanceProviderAPI;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.task.SaveToDiskTask;
import it.fabaris.wfp.utility.FormCompletedDataDBUpdate;
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
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * Class that defines the tab for the list of the submitted forms
 *
 */

public class FormListSubmittedActivity extends Activity 
{
	public interface FormListHandlerSubmitted
	{
		public ArrayList<FormInnerListProxy> getSubmittedForm();
	}
	public FormListHandlerSubmitted formListHandler;
	
	public int positionInviate;
	
	public ListView listview;
	private FormSubmittedAdapter adapter;
	
	private ArrayList<FormInnerListProxy> inviate;
	private ArrayList<FormInnerListProxy> submitted;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabsubmitted);
		
		inviate = new ArrayList<FormInnerListProxy>();
		inviate = getIntent().getExtras().getParcelableArrayList("submitted");
		
		submitted = new ArrayList<FormInnerListProxy>();		
		submitted = getIntent().getExtras().getParcelableArrayList("inviate");
		
        listview = (ListView) findViewById(R.id.listViewSubmitted);
        listview.setCacheColorHint(00000000);
		listview.setClickable(true);
		
		final Builder builder = new AlertDialog.Builder(this);
		
		
		adapter = new FormSubmittedAdapter(this, inviate, submitted);
        listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override	
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
			{
			
				Intent intent = new Intent (getApplicationContext(), FormEntryActivity.class);
				String keyIdentifer  = "ciao";
				String keyIdentifer1  = "ciao1";
				String keyIdentifer2  = "ciao2";
				String keyIdentifer3  = "ciao3";
				String pkgName = getPackageName();
				
				
				positionInviate = getRightCompletedParcelableObject(submitted.get(position).getFormName());//per visualizzare la form corretta
				
				
				intent.putExtra(pkgName+keyIdentifer, inviate.get(positionInviate).getPathForm()); 
				intent.putExtra(pkgName+keyIdentifer1, inviate.get(positionInviate).getFormName()); 
				intent.putExtra(pkgName+keyIdentifer2, inviate.get(positionInviate).getFormNameInstance()); 
				intent.putExtra(pkgName+keyIdentifer3, inviate.get(positionInviate).getFormNameAutoGen()); 
				
				intent.putExtra("submitted", true);
				
				intent.setAction(Intent.ACTION_VIEW);
			    String extension = MimeTypeMap.getFileExtensionFromUrl(inviate.get(positionInviate).getPathForm()).toLowerCase();
				String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
				intent.setDataAndType(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mimeType);
			    startActivity(intent);
			}
		});
		
		
		
		
		
		
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {                 
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
				//mettere nel forms.db 50 compilazioni d
				it.fabaris.wfp.provider.FormProvider.DatabaseHelper dbh = new it.fabaris.wfp.provider.FormProvider.DatabaseHelper("forms.db");
				for(int i=1; i<=20; i++){//mette 50 nuove form nello stato di completate nel deb forms.db
				String xmlpath = ""; //path dell'xml delle risposte
				int indexSubmitted = getIndexSubmitted(inviate.get(positionInviate));
				
				FormInnerListProxy form = inviate.get(positionInviate);//prendo la form
				String xmlFormInstance = getXmlFormInstance(form);//prendo la stringa xml della form
				xmlFormInstance = xmlFormInstance.substring(0,xmlFormInstance.indexOf("?formidentificator?")); //Elimino l'ultima parte della stringa che contiene il FormIdentificator
				//Cambiare enumerator a stringa Random o sequenziale...
				xmlFormInstance = xmlFormInstance.replace("</enumerator_1>", "_Clone" + Integer.toString(i) + "</enumerator_1>");
				
				try {
					xmlpath = saveInstance(form, i, xmlFormInstance);//salvo l'xml nell'externalStorage e ritorno il path dell'xml salvato
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//creo una nuova cartella con un nuovo file xml nella cartella instances
				//inserisci la form nel db forms
				String formname = submitted.get(indexSubmitted).getFormName();
				String displayNameInstance = inviate.get(positionInviate).getFormNameInstance();
				String formid = submitted.get(indexSubmitted).getFormId();//vuoto
				String pathxml = inviate.get(positionInviate).getPathForm();//path dell'xml del template della form
				String data = inviate.get(positionInviate).getDataInvio();
				String myinstanceFilePath = xmlpath;//path dell'xml delle risposte della compilazione   
				
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
						"('completed','"+formname+"','"+ displayNameInstance +"','','"+formid+"','"+pathxml+"','','','','"+data+"','','','','','','','"+ myinstanceFilePath +"','IT')";
				dbh.getWritableDatabase().execSQL(query);
				
				String completed_by = submitted.get(indexSubmitted).getFormEnumeratorId();
				
				//metti la form nella tabelle completed del DB grasp
				ApplicationExt.getDatabaseAdapter().open().insert("COMPLETED", displayNameInstance, formname, data, completed_by);
				}
				dbh.close();
				return true;
			}
		});
	}
	
	
	
	private ArrayList<FormInnerListProxy> querySubmittedForm()
	{
		formListHandler = new FormListActivity();
		ArrayList<FormInnerListProxy> inviati = formListHandler.getSubmittedForm();
		
		return inviati;	
	}
	
	public void onResume()
    {
		super.onResume();
		getFormsDataSubmitted();
		runOnUiThread(new Runnable() 
        { 
            public void run()  
            { 
            	FormSubmittedAdapter adapter = (FormSubmittedAdapter) listview.getAdapter();
            	adapter.notifyDataSetChanged();
            }    
        }); 	
    }
	
		//LL aggiunto 14-02-14
		//nell' oggetto parcellizzato di Fabaris identifica la posizione dell' item che contiene i dati giusti per visualizzare la form corretta
		private int getRightCompletedParcelableObject(String idFormInFabaris){//prende l'identificativo univoco della form
			
			int posizione = 0;
			//seleziona la posizione nella lista degli oggetti parcellizzati di fabaris che contiene l'id della form collegato all'oggetto parcellizzato cliccato sulla lista 
			//delle complete
			for(int i = 0; i<inviate.size(); i++){
				if(inviate.get(i).getFormNameInstance().equals(idFormInFabaris)){
					return i;
				}
			}
			return posizione;//restituisce la posizione dell'oggetto parcellizato fabaris cui fa riferimento la form selezionata nella lista delle complete
		} 
	
	public void getFormsDataSubmitted()
	{
		submitted.clear();
        Cursor cursor = ApplicationExt.getDatabaseAdapter().open().fetchAllSubmitted();
        try
        {
	        while (cursor.moveToNext())  
	        { 
	        	/**
	        	 * SUBMITTED_FORM_ID_KEY, SUBMITTED_FORM_NOME_FORM, SUBMITTED_FORM_SUBMITTED_DATA, SUBMITTED_FORM_COMPLETED_DATA, SUBMITTED_FORM_BY
	        	 */
	        	FormInnerListProxy submitted = new FormInnerListProxy(); 
	        	submitted.setFormId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_ID_KEY))); 
	        	submitted.setFormName(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_NOME_FORM))); 
	        	submitted.setDataInvio(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_SUBMITTED_DATA)));   
	        	submitted.setFormEnumeratorId(cursor.getString(cursor.getColumnIndex(DbAdapterGrasp.SUBMITTED_FORM_BY))); 
	              
	        	this.submitted.add(submitted); 
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
	
	/**
	 * AGGIORNA I DATI DELLA FORM PER COMPILARE LE TEXTVIEW DELLA LISTA INVIATE
	 * @param nome_form
	 * @param submitted_data
	 * @param submitted_by
	 */
	public void updateFormsDataToSubmitted(String nome_form, String submitted_data, String submitted_by)
	{
		/**
		 * CARICO IL DB CON I DATI RECUPERATI    
		 */
		String submitted_id = nome_form+submitted_by;
		ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", nome_form);
		ApplicationExt.getDatabaseAdapter().open().insert("SUBMITTED", submitted_id, nome_form, submitted_data, submitted_by); 
		ApplicationExt.getDatabaseAdapter().close(); 
	}
	
	public void onDestroy()
    {
		listview.setAdapter(null);
        super.onDestroy();
    }
	
	
	//partendo dalla form i cui riferimenti sono stati presi dall'oggetto parcellizzato crea la form da inviare al server
		private String getXmlFormInstance(FormInnerListProxy form) {
			String xml = null;
			try {
				InputStream fileInput = new FileInputStream(
						form.getStrPathInstance()); // path[position]);
				Document doc = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(fileInput);
				Transformer trans = TransformerFactory.newInstance()
						.newTransformer();
				trans.setOutputProperty(OutputKeys.METHOD, "xml");
				trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

				xml = trasformItem(trans, doc, form);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(xml);
			return xml;
		}
		
		/**
		 * genero il file 
		 * @param trans
		 * @param doc
		 * @param form
		 * @return
		 * @throws TransformerException
		 */
		public String trasformItem(Transformer trans, Document doc,FormInnerListProxy form) throws TransformerException {
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			trans.transform(source, result);
			String xmlString = sw.toString();
			String apos = "apos=\"'\"";
			xmlString = xmlString.replace(apos, "");
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
		
		
		private String saveInstance(FormInnerListProxy form, int folderWrittenCounter, String xmltoWrite) throws IOException{
			//creo la cartella che conterra' l'xml delle risposte
			String instancesfolderpath = Environment.getExternalStorageDirectory()+"/GRASP/instances/";
			String folderandfilename = form.getFormName()+"_"+ folderWrittenCounter;
			String fileInstancePath = instancesfolderpath+folderandfilename+"/"+folderandfilename+".xml";
			
			File f = new File(instancesfolderpath, folderandfilename);
			boolean haswritten = f.mkdirs();
			
			
			
			//scrivo il file e lo metto nella cartella
			if(haswritten){
				File xmlformfile = new File(f, folderandfilename+".xml");//creo il file xml nella cartella 
				FileWriter writer = new FileWriter(xmlformfile);
		        writer.append(xmltoWrite);
		        writer.flush();
		        writer.close();
			}
			return fileInstancePath;
		}
		
		private int getIndexSubmitted(FormInnerListProxy forminviata){
			int index = -1;
			
				String formNameInstanceinviate = forminviata.getFormNameInstance();
				for(int i=0; i < submitted.size(); i++){
					if(submitted.get(i).getFormName().toString().contains(formNameInstanceinviate)){
						index = i;
					}
				}
			return index;
		}
		
		
	
}

