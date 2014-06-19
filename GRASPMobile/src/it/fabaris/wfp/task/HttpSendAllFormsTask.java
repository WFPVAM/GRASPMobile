package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.FormListActivity;
import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.utility.FormCompletedDataDBUpdate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import object.FormInnerListProxy;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import utils.ApplicationExt;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


public class HttpSendAllFormsTask extends AsyncTask<String, Void, String>{
	
	ProgressDialog pd;
	String http;
	String phone;
	static String data;//la form
	int numOfFormSent;//contiene l'ennesimo numero della form inviata
	Context context;
	MyCallback finishFormListCompleted;
	
	///e' stato necessario includerle entrambe perche' in un oggetto ci sono delle info e nell'altro ce ne sono altre
	ArrayList<FormInnerListProxy> completedformslistfirst; //oggetto parcellizzato di fabaris
	ArrayList<FormInnerListProxy> completedformslistsecond; //oggetto parcellizzato di armando 
	
	static String date;
	private HashMap <String, String> formNotSent;
	

	public HttpSendAllFormsTask(Context context, String http, String phone, ArrayList<FormInnerListProxy> completedformslistfirst, ArrayList<FormInnerListProxy> completedformslistsecond, 
								MyCallback finishFormListCompleted) {
		this.context = context;
		
		if(http.contains(".aspx"))
		{
			this.http = http+"?call=response";
		}
		else
		{
			this.http = http+"/response";
		}
		
		
		this.phone = phone;
		this.completedformslistfirst = completedformslistfirst;
		this.completedformslistsecond = completedformslistsecond;
		this.finishFormListCompleted = finishFormListCompleted;
	}
	
	@Override
	protected void onPreExecute() {
		//imposto e visualizzo la waiting wheel
		
		this.pd = ProgressDialog.show(context,
				context.getString(R.string.checking_server),
				context.getString(R.string.wait));
	}
	
	@Override
	protected String doInBackground(String... params) {
		String result = "";
		if (!isOnline()) {//controllo se il dispositivo ha connessione
			result = "offline";
		}else {//se il dispositivo ha connessione invio la chiamata al server per l'invio della form
			
			
			
			
			
			
			numOfFormSent = 1;//setto il numero di form che e' stato inviato ad 1 per iniziare
			for(FormInnerListProxy mydata:completedformslistfirst){//ciclo sulla lista delle form da inviare
				data = decodeForm(mydata);//la form da inviare codificata
				result = sendFormCall(http, phone,data);
				Log.i("httpSendPostTaskOnPostEx", result);
				 
					
					if (result.trim().toLowerCase().startsWith("ok")) //il server ha risposto ok la form e' stata inviata correttamente dal mobile e ricevuta correttamente dal server
					{
						Log.i("RESULT", "messaggio ricevuto dal server");
						numOfFormSent = numOfFormSent++;
						//--------------------------------------------------------------------------
						XPathFactory factory = XPathFactory.newInstance();
						XPath xPath = factory.newXPath();
						XPathExpression xPathExpression;
						DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
						try 
						{
							DocumentBuilder builder = builderFactory.newDocumentBuilder();
							ByteArrayInputStream bin = new ByteArrayInputStream(result
									.substring(result.indexOf("-") + 1).getBytes());
							Document xmlDocument = builder.parse(bin);
							bin.close();
							xPathExpression = xPath.compile("/response/formResponseID");
							String id = xPathExpression.evaluate(xmlDocument);
	
							FormListCompletedActivity.setID(id);
							
						//------------------------------------------------------------------------
						} 
						catch (XPathExpressionException e) 
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	
						/**
						 * AGGIORNA LO STATO DELLA FORM
						 */
						updateFormToSubmitted(mydata);//update forms.db
						UpdateDBAfterSendAllForms(mydata);//update GRASP.DB
						
						
						
						/*
						if(numOfFormSent == completedformslistfirst.size()){//se sono state spedite tutte le form lancia il finish su formListCompletedActivity
							FormListCompletedActivity fmla = new FormListCompletedActivity();
							fmla.finishListCompletedActivity();
						}
						*/
						
						
						
					}else{//la form non e' stata ricevuta dal server o cmq qualche cosa e' andata male
						formNotSent.put(mydata.getFormNameInstance(),result);//lista delle form non inviate con motivazione chiave = formNameInstance valore = risposta del server
					}
				}
			}
		if(formNotSent != null){
			if(!formNotSent.isEmpty()) {
				return "ko";
			}
			else { //se c'e' stato qualche problema nell'invio delle form e qualcuna o tutte non state inviate correttamente
				return "ok";
			}
		}else{
			return "ok";
		}
	}
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		////////invio terminato dismetto la dialog
		if(pd != null){
			if(pd.isShowing()){
				pd.dismiss();
			}
		}
		if(result == "ok"){//se tutte le form sono state inviate
			Toast.makeText(context, "All forms have been sent succesfully",	Toast.LENGTH_SHORT).show();
		}else{// non tutte le form sono state inviate correttamente
			Toast.makeText(context, "There has been some problems sending one or more forms",	Toast.LENGTH_SHORT).show();
			//////metto il salvataggio di quelle che stanno qui nelle finzalizzate?
			/*
			if (result.trim().toLowerCase().startsWith("ok"))
			{
			
			}else if (result.equalsIgnoreCase("Offline"))
			{
				FormListCompletedActivity.updateFormToFinalized();
				Toast.makeText(context, R.string.device_not_online,	Toast.LENGTH_SHORT).show();
			}
			else if (result.trim().equalsIgnoreCase("server error")) 
			{
				FormListCompletedActivity.updateFormToFinalized();
				Toast.makeText(context, R.string.check_connection, Toast.LENGTH_SHORT).show();
			}
			else if (result.trim().equalsIgnoreCase("formnotonserver"))
			{
				FormListCompletedActivity.updateFormToFinalized();
				Toast.makeText(context, R.string.form_not_available, Toast.LENGTH_SHORT).show();
			}
			else if (result.trim().equalsIgnoreCase("Error")) 
			{
				Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
			}
			*/
		}


		
		  



if (finishFormListCompleted != null)
{
	
	finishFormListCompleted.finishFormListCompleted();
}



	}
	
	private String sendFormCall(String url, String phone, String data) {
		/**
		 *  set parameter
		 */
		String result = null;
		HttpPost httpPost = new HttpPost(url);
		HttpParams httpParameters = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		// HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
		nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
		nameValuePair.add(new BasicNameValuePair("data", data));
		// Url Encoding the POST parameters
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result = "error";
		}
		/**
		 *  Making HTTP Request
		 */
		try {
			HttpResponse response = httpClient.execute(httpPost);
			result = EntityUtils.toString(response.getEntity());

			if (result.equalsIgnoreCase("\r\n")) {
				return result = "formnotonserver";
			} else {
				return result = "ok-" + result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return result = "error";
		}
	}
	
	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if (ni == null) {
			return false;
		}
		return ni.isConnected();
	}
	
	//partendo dalla form i cui riferimenti sono stati presi dall'oggetto parcellizzato crea la form da inviare al server
	private String decodeForm(FormInnerListProxy form) {
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

			String xmlString = trasformItem(trans, doc, form);
			xml = encodeSms(xmlString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(xml);
		return xml;
	}
	
	
	public void UpdateDBAfterSendAllForms(FormInnerListProxy form) {//aggiorno il GRASP.db: elimino dalle complete la form appena inviata a la inserisco nella tabella delle inviate
		String idFormNameInstance = form.getFormNameInstance();
		String nomeform = form.getFormName();
		String autore = getAuthor(form);
		updateFormsDataToSubmitted(nomeform+ "&" + autore, date, autore, idFormNameInstance); 
	}
	
	
	public static void updateFormToSubmitted(FormInnerListProxy form) {//aggiorno il forms.db settando lo stato della form appena inviata a 'submitted' 
		
		String displayNameInstance = form.getFormNameInstance();
		DatabaseHelper dbh = new DatabaseHelper("forms.db");
		String updatequery = "UPDATE forms SET status='submitted' WHERE displayNameInstance = '"+ displayNameInstance + "'";

		Log.i("FUNZIONE updateFormToSubmitted per la form: ",displayNameInstance);

		dbh.getReadableDatabase().execSQL(updatequery);

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

		date = day + "/" + month.format(rightNow.getTime()) + "/" + year;
		// -----------------------------------------------------

		dbh.close();
	}
	
	
	/**
	 * genero il file 
	 * @param trans
	 * @param doc
	 * @param form
	 * @return
	 * @throws TransformerException
	 */
	public String trasformItem(Transformer trans, Document doc,
			FormInnerListProxy form) throws TransformerException {
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
	
	private String getAuthor(FormInnerListProxy form){//partendo dalla form che sto inviando presa dall'oggetto parcellizzato creato da Fabaris, cercando per formNameInstance prendo dall'oggetto parcellizato di Armando il nome dell'enumeratorID
		String author = "";
		
		String formNameInstancefirst = form.getFormNameInstance();//prendo dalla form corrente l'identificativo che mi serve per ritrovare la form nell'oggetto parcellizzato di armando per prendermi l'enumeratorID
		for(int i = 0; i<completedformslistsecond.size(); i++){//CICLO SULL'oggetto parcellizzato di Armando
				if(completedformslistsecond.get(i).getFormName().toString().contains(formNameInstancefirst)){
					author = completedformslistsecond.get(i).getFormEnumeratorId();//quando trovo l'id che fa match ci setto l'autore
				}
		}
		return author;
	}
	
	//IN QUESTA FUNZIONA VA AGGIUNTO IL PARAMETRO COMLETED ID DB GRASP CHE DEVE ESSERE USATO COME FILTRO SULLA DELITE
		public void updateFormsDataToSubmitted(String nome_form, String submitted_data, String submitted_by, String idFormDataBaseGras)
		{
			/**
			 * CARICO IL DB CON I DATI RECUPERATI 
			 */  
			
			String submitted_id = nome_form+submitted_by;
			String filter = idFormDataBaseGras;
			//ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", submitted_id);//LL tolto per passare il giusto filtro per cancellare la form giusta
			
			
			ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", filter);//LL aggiunto per passare il giusto filtro per cancellare la form giusta
			ApplicationExt.getDatabaseAdapter().open().insert("SUBMITTED", submitted_id, idFormDataBaseGras, submitted_data, submitted_by); 
			ApplicationExt.getDatabaseAdapter().close(); 
		}

}
