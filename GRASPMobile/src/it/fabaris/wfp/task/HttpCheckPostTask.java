package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Class that defines the task that check the connection with the server
 *
 */

public class HttpCheckPostTask extends AsyncTask<String, Void, String>{
	ProgressDialog pd;
	String http;
	String phone;
	String data;
	Context context;
	
	public HttpCheckPostTask(Context context, String http, String phone, String data) {
			this.context = context;
			this.http = http;
	        this.phone = phone;
	        this.data = data;
	}
	
	@Override
	protected void onPreExecute() {
		pd = ProgressDialog.show(context, context.getString(R.string.checking_server), context.getString(R.string.wait));
	}
	
	@Override
	protected String doInBackground(String... params) {
		String result = "";
		if(http.startsWith("http://")||(http.startsWith("Http://")) && http.length()>7){
			if (isOnline()){
				result = postCall(http, phone, data);
				Log.i("campoDataNellaRichiesta",data);
				Log.i("risultato dalla richiesta", result);
			}else{
				result = "Offline";
			}
		}else{
			result = "Invalid URL";
		}
		result = result.replace("\r\n", "");
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (pd.isShowing()&&result!=null) {
			pd.dismiss();
			if(result.trim().equalsIgnoreCase("ok")){
				Toast.makeText(context, R.string.server_on_line, Toast.LENGTH_LONG).show();
				PreferencesActivity.SERVER_ONLINE = "YES";
			}else if(result.contains("number")){
				Toast.makeText(context, R.string.phone_not_in_server, Toast.LENGTH_LONG).show();
				PreferencesActivity.SERVER_ONLINE = "NO";
			}else{
				Toast.makeText(context, R.string.server_not_online, Toast.LENGTH_LONG).show();
				PreferencesActivity.SERVER_ONLINE = "NO";
			}
	    }
	}	

	private String postCall(String http, String phone, String data) {
		/**
		 * set parameter
		 */
		String result = null;
		HttpPost httpPost = new HttpPost(http);
        HttpParams httpParameters = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
//        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
        nameValuePair.add(new BasicNameValuePair("data", data));
        // Url Encoding the POST parameters
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            HttpResponse response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());
        }catch (Exception e) {
            e.printStackTrace();
			return result = "error";
		}
		return result;
	}
	
	private boolean isOnline(){
        ConnectivityManager cm = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null){
        	return false;
        }
        return ni.isConnected();
	}
}