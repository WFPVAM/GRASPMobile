package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.utility.ConstantUtility;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Class that defines the task that check the connection with the server and
 * send the xform
 * 
 */

public class HttpCheckAndSendPostTask extends AsyncTask<String, Void, String> {
	ProgressDialog pd;
	String http;
	String http1;
	String http2;
	String phone;
	String data;//la form da inviare
	boolean isSendAllForms;
	
	Context context;
	MyCallback callback;
	Lock lock;
	
	private String result = "";

	public HttpCheckAndSendPostTask(Context context, String http, String phone,
			String data, MyCallback callback, boolean isSendAllForms) {
		this.context = context;
		this.http = http;
		this.isSendAllForms = isSendAllForms;
		//RC modifica per adattarsi al reporting 21/03/2014
		if(http.contains(".aspx"))
		{
			this.http1 = http+"?call=test";
			this.http2 = http+"?call=response";
		}
		else
		{
			this.http1 = http+"/test";
			this.http2 = http+"/response";
		}
		this.phone = phone;
		this.data = data;
		this.callback = callback;
		this.lock = new ReentrantLock();
		result = "";
	}

	@Override
	protected void onPreExecute() 
	{
		//pd = ProgressDialog.show(context, "Sending data to server...", "Please wait...");
		
		//----------------------------------------------------------
		
		pd = ProgressDialog.show(context,
				context.getString(R.string.checking_server),
				context.getString(R.string.wait));
			
		
		//----------------------------------------------------------
	}

	@Override
	protected String doInBackground(String... params)
	{
		
		result = ControlConnection(http1, phone, data);
		Log.i("result in doInBackground", result);
		return result;
	}

	@Override
	protected void onPostExecute(String result) 
	{
		if (pd.isShowing() && result != null) 
			if(pd!=null){
				pd.dismiss();
			}
		{
		if (result.trim().equalsIgnoreCase("OK")) 
		{
			int k = 1;
			if(!isSendAllForms)//se NON si stanno inviando tutte le form insieme visualizza il toast
				Toast.makeText(context, R.string.server_on_line, Toast.LENGTH_LONG).show();
			PreferencesActivity.SERVER_ONLINE = "YES";
				
			lock.lock();
			try
			{
				synchronized (lock)
				{
					
					HttpSendPostTask asyncTask = new HttpSendPostTask(context, http2, phone, data, callback, lock, isSendAllForms);
					//Log.i("FUNZIONE HttpSendPostTask", "thread: "+ data);
					Log.i("FUNZIONE HttpSendPostTask", "thread: ");
					asyncTask.execute();
					
					lock.wait(2000);
					if (asyncTask.getStatus() == AsyncTask.Status.PENDING) 
					{
						FormListCompletedActivity.updateFormToFinalized();
						Toast.makeText(context, R.string.check_connection,
								Toast.LENGTH_SHORT).show();
						Log.i("pending","i");
					}
					else if (asyncTask.getStatus() == AsyncTask.Status.FINISHED) 
					{
						// tutto ok
						Log.i("tuttok","i");
					} 
					else if (asyncTask.getStatus() == AsyncTask.Status.RUNNING) 
					{
						// condCheck.await(10, TimeUnit.SECONDS);
						Log.i("condCheck.await(10, TimeUnit.SECONDS)","i");
					}
				}
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			lock.unlock();		
		} 
		else if (result.trim().equalsIgnoreCase("error"))
		{
			Toast.makeText(context, R.string.server_not_online,
					Toast.LENGTH_SHORT).show();
			PreferencesActivity.SERVER_ONLINE = "NO";
		} 
		else if (result.contains("number"))
		{
			Toast.makeText(context, R.string.phone_not_in_server,
					Toast.LENGTH_SHORT).show();
			PreferencesActivity.SERVER_ONLINE = "NO";
		}
		else
		{
			Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show();
			PreferencesActivity.SERVER_ONLINE = "NO";
		}
		}
		
	}

	//--------------------------------------------------------------------------------

	private String postCall(String http, String phone, String data) {
		/**
		 *  set parameter
		 */
		String result = null;
		HttpPost httpPost = new HttpPost(http);
		HttpParams httpParameters = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		// HttpConnectionParams.setSoTimeout(httpParameters, 10000);
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

	private String ControlConnection(String http1, String phone, String data)
	{
		String result = "";
		if (http1.startsWith("http://") || (http1.startsWith("Http://")) && http1.length() > 7) 
		{
			if (isOnline()) 
				result = postControlConnection(http1, phone, data);
			else
				result = "Offline";	
		}
		else
			result = "Invalid URL";	
		
		return result;
	}	

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			return false;
		}
		return ni.isConnected();
	}	
	
	private String postControlConnection(String http, String phone, String data) {
		/**
		 *  set parameter
		 */
		String result = "";
		HttpPost httpPost = new HttpPost(http);
		HttpParams httpParameters = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		// HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
		nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
		nameValuePair.add(new BasicNameValuePair("data", data));
		/**
		 *  Url Encoding the POST parameters
		 */
		try 
		{
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
			HttpResponse response = httpClient.execute(httpPost);
			result = EntityUtils.toString(response.getEntity());
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			return result = "error";
		}
		return result;
	}
}