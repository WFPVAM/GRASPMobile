/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.fabaris.wfp.activities;

import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.provider.MessageProvider.DatabaseHelper;
import it.fabaris.wfp.task.DownloadSmsTask;
import it.fabaris.wfp.task.HttpXmlCheckAndSyncTask;
import it.fabaris.wfp.utility.XmlParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class is responsible for displaying SMS arrived containing forms
 * 
 *
 */
public class SmsListActivity extends ListActivity implements MyCallback{
	public ArrayList<String> listvalue;
	public ArrayList<String> listvalueinflater;
	public ArrayAdapter<String> adapterinflater;
	public Cursor cur;
	public String strFormName;
	public String strFormText;
	public String[] arrFormName;
	public String[] arrFormText;
	
	@Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_list);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.new_form));
        
        /**
         * creazione lista
         */
        final ListView listview = drawList();
        
		/**
		 * button syncronyze server
		 */
		Button buttonRefresh= (Button)findViewById(R.id.btnrefresh);
		buttonRefresh.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				ArrayList<String> formList = getFormList();
				String xmlToSend = createXmlToSend(formList);
				SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				String httpServer = settings.getString(PreferencesActivity.KEY_SERVER_URL, getString(R.string.default_server_url));
				String numClient = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE, getString(R.string.default_client_telephone));
				String http = httpServer;
				String phone = numClient;
				String data = xmlToSend;
				HttpXmlCheckAndSyncTask asyncTask = new HttpXmlCheckAndSyncTask(SmsListActivity.this, http, phone, data, SmsListActivity.this);
				asyncTask.execute();
			}
		});
		
		/**
		 * click to import form
		 */
		listview.setOnItemClickListener(new OnItemClickListener(){
			@Override	
			public void onItemClick(AdapterView<?> parent, final View v, int position, long id) {
			getView(position, v, parent);
			final XmlParser px = new XmlParser();
			strFormName = (arrFormName[position].toString()); 	
			strFormText = (arrFormText[position]).toString();
			try {
				byte[] decodedString = Base64.decode(strFormText, 0);
				ByteArrayInputStream inStream = new ByteArrayInputStream(decodedString);
				GZIPInputStream zipInput = new GZIPInputStream(inStream);
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				int i;
					byte[] buffer = new byte[1024];
					while ((i = zipInput.read(buffer)) > 0) {
						outStream.write(buffer, 0, i);
			        }
				zipInput.close();
				inStream.close();
				final String res = outStream.toString("UTF-8");
	        	final File myfile = new File (Collect.FORMS_PATH +"/forms.xml");
				myfile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(myfile);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut,"UTF-8");
				myOutWriter.append(res.toString());
				myOutWriter.close();
				fOut.close();
				String formid= px.getID(myfile);
				int igroup = px.getNumGroup(myfile);
				String strgroup = Integer.toString(igroup);
				DownloadSmsTask asyncTask = new DownloadSmsTask(SmsListActivity.this, res, strFormName, formid, strgroup, SmsListActivity.this);
				asyncTask.execute();
				Toast.makeText(getApplicationContext(), R.string.imported, Toast.LENGTH_LONG).show();
				myfile.delete();
			}catch (Exception e) {				
				e.printStackTrace();
			}
			}
		});
	}
	
	public ListView drawList() {
		cur = selectView();
        listvalue = new ArrayList<String>();
		listvalueinflater = new ArrayList<String>();
        for (int i = 0; i <= cur.getCount() - 1; i++) {
        		listvalue.add(arrFormText[i]);
        		listvalueinflater.add("Import form : "+arrFormName[i]);
    	}
		adapterinflater = new ArrayAdapter<String>(this,R.layout.sms_list_row,R.id.label, listvalueinflater);
		setListAdapter(adapterinflater);
        final ListView listview = getListView();
        listview.setCacheColorHint(00000000);
		listview.setClickable(true);
        listview.refreshDrawableState();
		return listview;
	}
	
	protected String createXmlToSend(ArrayList<String> formName) {
		String strXmlList = "";
		int numForm = formName.size();
		try{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("forms");
			doc.appendChild(root);
			
			for(int i=0;i<numForm;i++){
				/*
				//LL 19-03-2014 MODIFICA IMPLEMENTATA PER LE FORM DI TEST (e commentare tutto il codice dentro al for)
				//se il nome della form finisce per _test allora nn bisogna mettere la form nella lista di quelle gia' presenti cosi' il 
				//designer te la rispedira' di nuovo
				Element form = doc.createElement("form");
				String name = formName.get(i);
				String nameForm = fetchFormNameFromFormId(formName.get(i));
				String[] splittedFormName = nameForm.split("_");
				String endOfFormName = splittedFormName[splittedFormName.length-1];//prendo l'ultima occorrenza del nome
				
				if(!endOfFormName.equalsIgnoreCase("test") || (endOfFormName.equalsIgnoreCase("test") && splittedFormName.length == 1)){//se il nome della form NON finisce per _test o o la parola test e'l'unica parola che compone il nome della form
					//metti la form nella lista di quelle che gia' ho
					form.setTextContent(name);
					root.appendChild(form);
					
				}
				*/
				//LL 19-03-2014 va commentato perche' sostituito dal codice sopra (modifica necessaria per gestire le form di test)
				Element form = doc.createElement("form");
				String name = formName.get(i);
				form.setTextContent(name);
				root.appendChild(form);
				
		
			}
			
			/**
			 *  create Transformer object
			 */
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			strXmlList = writer.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.i("formGiaSincronizzate",strXmlList);
		return strXmlList;
	}
	
	public ArrayList<String> getFormList(){
		ArrayList<String> formList = new ArrayList<String>();
		it.fabaris.wfp.provider.MessageProvider.DatabaseHelper dbh = new it.fabaris.wfp.provider.MessageProvider.DatabaseHelper("message.db");
        String query = "SELECT formId FROM message ORDER BY formId ASC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        String[] arrFormName = null;
        try {
        	arrFormName = new String[c.getCount()];
        	if (c.moveToFirst()){
        		do {
    			arrFormName[c.getPosition()] = c.getString(0);
	        	}while(c.moveToNext());
	        }
        }catch (Exception e){
        	e.printStackTrace();
        } 
        finally {
        	if ( c != null ) {
        		c.close();
        		dbh.close();
        	}
        }
        for(int i = 0;i<arrFormName.length;i++){
        	formList.add(arrFormName[i]);
        }
		return formList;
		
	}
	
	public Cursor selectView(){
		it.fabaris.wfp.provider.MessageProvider.DatabaseHelper dbh = new DatabaseHelper("message.db");
        String query = "SELECT formName,formEncodedText,date FROM message WHERE formImported = 'no' ORDER BY formId ASC";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        try {
        	arrFormName = new String[c.getCount()];
        	arrFormText = new String[c.getCount()];
        	
   	
        	if (c.moveToFirst())
        	{
        		do 
        		{
        			arrFormName[c.getPosition()] =c.getString(0);
        			arrFormText[c.getPosition()] =c.getString(1);
	        	}
        		while(c.moveToNext());
	        }
        	
        }catch (Exception e){
        	e.printStackTrace();
        } 
        finally {
        	if ( c != null ) {
        		c.close();
        		dbh.close();
        	}
        }
		return c;
	}
	
	
	
	//LL 20-03-2014 aggiunta per poter gestire le form di test (ho necessita' di conoscere il nome della form per sapere se metterla o meno nella lista di quelle gia' sincronizzate)
	public String fetchFormNameFromFormId(String formId){
		//il metodo seleziona in base al nome l'id della form di test se questa gia' esiste
		//se la form esiste l'id verra' usato per cancellare la form
		String formName = "";
		it.fabaris.wfp.provider.MessageProvider.DatabaseHelper dbh = new it.fabaris.wfp.provider.MessageProvider.DatabaseHelper("message.db");
        String query = "SELECT formName FROM message WHERE formId = '"+ formId +"'";
        Cursor c = dbh.getReadableDatabase().rawQuery(query, null);
        if(c.getCount() != 0){//se e' presente una form con questo id
        	if (c.moveToFirst()){
        		formName = c.getString(0);
        	}
        }
        return formName;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
	  View view;
	  Context context = getApplicationContext();
	  LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  view = mInflater.inflate(R.layout.sms_list_row, parent, false);
	  String itemText = listvalue.get(position);
	  String itemName = (String) getListAdapter().getItem(position);
	  TextView smsText = (TextView) view.findViewById(R.id.labelinflater);
	  TextView smsName = (TextView) view.findViewById(R.id.label);
	  smsText.setText(itemText.toString());
	  smsName.setText(itemName.toString());
	  return parent;
	}

	@Override
	public void callbackCall() {
		drawList();
	}

	@Override
	public void finishFormListCompleted() {
		// TODO Auto-generated method stub
		
	}
	
}
