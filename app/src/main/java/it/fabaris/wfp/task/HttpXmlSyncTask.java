package it.fabaris.wfp.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.listener.MyCallback;

/**
 * Class that defines the task that downloads the xform from the server
 *
 *
 */

public class HttpXmlSyncTask extends AsyncTask<String, Void, String>{
    ProgressDialog pd;
    String http;//server url
    String phone;//client phone number
    String data;//a given string
    Context context;
    MyCallback callback;

    public HttpXmlSyncTask(Context context, String http, String phone, String data, MyCallback callback) {
        this.context = context;
        this.http = http;
        this.phone = phone;
        this.data = data;//the string "sync"
        this.callback = callback;
    }

    /**
     * show dialog
     */
    @Override
    protected void onPreExecute() {
        pd = ProgressDialog.show(context, context.getString(R.string.syncronyze), context.getString(R.string.wait));
    }

    /**
     * send to the server the list with the form already sent and return the call
     * result to onPostExecute(). The result can be a string with an error code
     * or the xml with the list of the forms not synchronized yet
     */
    @Override
    protected String doInBackground(String... params) {
        String result = "";
        if(http.startsWith("http://")&& http.length()>7){
            if(isOnline()){
                result = postCall(http, phone, data);
                Log.i("resultFromWeb", result);
                if(!result.equalsIgnoreCase("Generating a request response generated an error")) {
                    Document doc = stringToDoc(result);
                    xmlToDB(doc);
                }
            }else{
                result = "Offline";
            }
        }
        return result;
    }

    /**
     * draw the new synchronized forms list
     * and give a feedback to the user
     */
    @Override
    protected void onPostExecute(String result) {
        if (pd.isShowing()&& result!=null) {
            pd.dismiss();
            if(callback!= null){
                callback.callbackCall();
            }
            if(result.equalsIgnoreCase("Generating a request response generated an error")){
                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
            } else if(result.equalsIgnoreCase("Offline")){
                Toast.makeText(context, R.string.offline, Toast.LENGTH_LONG).show();
            }else if(result.trim().equalsIgnoreCase("Error")){
                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
            }else if(result.trim().equalsIgnoreCase("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><forms/>")){
                Toast.makeText(context, R.string.no_forms_to_download, Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, R.string.forms_downloaded, Toast.LENGTH_LONG).show();
            }

        }
    }

    private Document stringToDoc(String input) {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc = null;
        try{
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(new InputSource(new StringReader(input)));
        }catch(Exception e){
            e.printStackTrace();
        }
        return doc;
    }

    private void xmlToDB(Document doc) {
        Node root = doc.getFirstChild();
        for (int i = 0; i < root.getChildNodes().getLength(); i++) {
            try{
                Node form = root.getChildNodes().item(i);
                String xmlBody = form.getTextContent();
                Document xml = stringToDoc(form.getTextContent());

                System.out.println();

                String xmlName = XPathFactory.newInstance().newXPath().compile(
                        "/html/head/title").evaluate(xml);
                String xmlId = XPathFactory.newInstance().newXPath().compile(
                        "/html/head/model/instance/data/id").evaluate(xml);
                GregorianCalendar gc = new GregorianCalendar();
                String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
                String month = Integer.toString(gc.get(Calendar.MONTH));
                String year = Integer.toString(gc.get(Calendar.YEAR));
                String data = day+"-"+month+"-"+year;
                xmlBody = FormListCompletedActivity.encodeSms(xmlBody);
                it.fabaris.wfp.provider.MessageProvider.DatabaseHelper dbh =
                        new it.fabaris.wfp.provider.MessageProvider.DatabaseHelper("message.db");

                //LL 19-03-2014 commentato perche' sostituito dal codice sotto (modifica necessaria per gestire le form di test)
                String insertquery = "INSERT INTO message" +
                        "(formId," +
                        "formName," +
                        "formImported," +
                        "formEncodedText," +
                        "formText," +
                        "date)" +
                        "VALUES" +
                        "('"+xmlId+"','"+xmlName+"','no','"+xmlBody+"','','"+data+"')";
                dbh.getWritableDatabase().execSQL(insertquery);

///////////////////////////////////////////////////////////////////////////////////////////////////
//                String duplicateName;
//                FormProvider.DatabaseHelper dbh1 = new FormProvider.DatabaseHelper("forms.db");
//                String query = "SELECT instanceFilePath FROM forms WHERE displayName ='"+xmlName+"'";
//                Cursor c = dbh1.getReadableDatabase().rawQuery(query, null);;
//                 if (c.moveToFirst()){
//                     duplicateName = c.getString(c.getColumnIndex("instanceFilePath"));
//                 }
//                     c.close();
////////////////////////////////////////////////////////////////////////////////////////////////////

			/*
				//LL 19-03-2014 MODIFICA IMPLEMENTATA PER LE FORM DI TEST (e commentare l'insert sopra)
				String[] splittedFormName = xmlName.split("_");
				String endOfFormName = splittedFormName[splittedFormName.length-1];//prendo l'ultima occorrenza del nome
				
				if(endOfFormName.equalsIgnoreCase("test") || (endOfFormName.equalsIgnoreCase("test") && splittedFormName.length > 1)){//se il nome della form finisce per _test o la parola test e'l'unica parola che compone il nome della form
					//fai una select per nome form
					String selectquery = "SELECT formId FROM message WHERE formName = '" + xmlName + "'";
					Cursor c = dbh.getReadableDatabase().rawQuery(selectquery, null);
					if(c.getCount()!=0){//se nome form esiste
						if (c.moveToFirst()){
							//fai un update del record inserendo il nuovo contenuto (non posso fare una delete e poi una insert perche' cambierebbe l'ordine delle form nel DB e questo potrebbe  
							//far rompere la logica nell'uso  degli oggetti parcellizzati usati come sorgente dall' adapter nella visualizzazione delle info delle form nella lista delle form "nuove"
							String updatequery = "UPDATE message SET formId = '"+xmlId+"',formName ='" +xmlName+ "',formImported = 'no', formEncodedText = '"+ xmlBody +"', formText = '', date = '"+ data +"' WHERE formId = '" + c.getString(0) + "'";
							dbh.getWritableDatabase().execSQL(updatequery);
						}
						///////////////////QUI DEVO AGGIUNGERE L'ELIMINAZIONE DELLA FORM OVUNQUE!!!!!!!!!!!!!
						//////////////////////////////////////////////////////////////////////////////////
						//////////////////////////////////////////////////////////////////////////////////
						deleteTestForm(xmlName);//il metodo elimina ogni traccia della form di test!!!!!!!!
						
					}else{//se nome form NON esiste metti la form nel DB
						String insertquery = "INSERT INTO message" +
								"(formId," +
								"formName," +
								"formImported," +
								"formEncodedText," +
								"formText," +
								"date)" +
								"VALUES" +
								"('"+xmlId+"','"+xmlName+"','no','"+xmlBody+"','','"+data+"')";
						dbh.getWritableDatabase().execSQL(insertquery);
					}
				}else{//il nome della form non finisce per _test
				String insertquery = "INSERT INTO message" +
						"(formId," +
						"formName," +
						"formImported," +
						"formEncodedText," +
						"formText," +
						"date)" +
						"VALUES" +
						"('"+xmlId+"','"+xmlName+"','no','"+xmlBody+"','','"+data+"')";
				dbh.getWritableDatabase().execSQL(insertquery);
				}
				*/

                dbh.close();
            }catch(Exception e){
                e.printStackTrace();
                continue;
            }
        }
    }
	
	/*
	//LL 20-03-2014 questo metodo e' stato aggiunto exnovo per eliminare qualunque traccia delle form di test in fase di sincronizzazione
	private boolean deleteTestForm(String nomeForm){
	
		//cancello la form da forms.db non si puo' fare un update perche' le form vengono inserite in questo DB solo una volta che passano dalla lista delle
		//sincronizzate alla lista delle nuove e se facessi un update il processo classico avrebbe qualche problema
		DatabaseHelper dbh = new DatabaseHelper("forms.db");
		String deletequery = "DELETE forms WHERE displayName= '" + nomeForm + "'";
		dbh.getReadableDatabase().execSQL(deletequery);
		dbh.close();
		
		//cancello la form nelle tabelle del DB grasp
		ApplicationExt.getDatabaseAdapter().open().deleteTestFormFromGRASPDb(nomeForm);
		
		//cancello l'xml del template della form di test dalla cartella forms
		final File tampletFormFile = new File (Collect.FORMS_PATH +"/"+nomeForm+".xml");
		tampletFormFile.delete();
		
		//cancello gli xml che contengono le risposte delle varie compilazioni della form di test nella cartella instances
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(Collect.INSTANCES_PATH);
		for (File f : dir.listFiles()) {
    		if (f.isFile()){
        		String name = f.getName();
        		int i = name.lastIndexOf("_");
				String[] nameOfFormWithoutInstanceNumber =  {name.substring(0, i), name.substring(i)};
				if(nameOfFormWithoutInstanceNumber.equals(nomeForm)){
					f.delete();
				}
        	}
		}
		return true;
	}*/

    /**
     * call the server to retrive forms to
     * synchronized if there are
     * @param url server url
     * @param phone client phone number
     * @param data a list of forms already synchronized
     * @return the server response as a string
     */
    private String postCall(String url, String phone, String data) {
        /**
         * set parameter
         */
        String result = null;
        HttpPost httpPost = new HttpPost(url);
        HttpParams httpParameters = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
//        HttpConnectionParams.setSoTimeout(httpParameters, 3000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
        nameValuePair.add(new BasicNameValuePair("data", data));
        /**
         *  Url Encoding the POST parameters
         */
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        /**
         *  Making HTTP Request
         */
        try {
            HttpResponse response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
            Log.i("Http response:", result);
        } catch (Exception e) {
            e.printStackTrace();
            return result = "error";
        }
        return result;
    }

    /**
     * check if the device has a data connection
     * @return
     */
    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE );
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null){
            return false;
        }
        return ni.isConnected();
    }
}