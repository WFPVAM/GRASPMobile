package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.listener.MyCallback;

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
import android.widget.Toast;

/**
 * Class that defines the task that check the connection with the server
 * and download the xform from the server
 * This class is called when the user click on the synchronize
 * button, in SmsListActivity.
 * In doInBackground() check if the server is on line,
 * if yes, in onPostExecute() call again the server using
 * HttpXmlSyncTask object, in order to complete the process
 */

public class HttpXmlCheckAndSyncTask extends AsyncTask<String, Void, String>{
    ProgressDialog pd;
    String http;
    String http1;//test server url
    String http2;//synchronization server url
    String phone;//client phone number
    String data;//a given string
    Context context;
    MyCallback callback;//called after the synchronization process

    public HttpXmlCheckAndSyncTask(Context context, String http, String phone, String data, MyCallback callback) {
        this.context = context;
        this.http = http;
        //RC modifica per adattarsi al reporting 21/03/2014
			/*
			 * set the server url format depending on the server
			 */
        if(http.contains(".aspx"))//if server is the web reporting
        {
            this.http1 = http+"?call=test";
            this.http2 = http+"?call=sync";
        }
        else//if the server is the desktop designer
        {
            this.http1 = http+"/test";
            this.http2 = http+"/sync";
        }
        this.phone = phone;
        this.data = data;//the string "test"
        this.callback = callback;
    }

    /**
     * show dialog before to do anything
     */
    @Override
    protected void onPreExecute() {
        pd = ProgressDialog.show(context, context.getString(R.string.checking_server), context.getString(R.string.wait));
    }

    /**
     * call the server in order to know if it is on line and
     * give the result to onPostExecute
     */
    @Override
    protected String doInBackground(String... params) {
        String result = "";
        if(http1.startsWith("http://")||(http1.startsWith("Http://")) && http1.length()>7){
            if (isOnline()){
                result = postCall(http1, phone, data);
                /**
                 IN DATA INVIO LA LISTA DI FORM CHE HO GIA' SINCRONIZZATO SUL TELEFONO
                 */
            }else{
                result = "Offline";
            }
        }else{
            result = "Invalid URL";
        }
        result = result.replace("\r\n", "");
        return result;
    }

    /**
     * Dismiss dialog.
     * if the server is on line then call HttpXmlSyncTask()
     * to finish the process, show a negative feedback at
     * the user otherwise
     */
    @Override
    protected void onPostExecute(String result) {
        if (pd.isShowing()&&result!=null) {
            pd.dismiss();
            if(result.trim().equalsIgnoreCase("OK")){
                Toast.makeText(context, R.string.server_on_line, Toast.LENGTH_LONG).show();
                PreferencesActivity.SERVER_ONLINE = "YES";
                data = data.replace("\n", "");
                HttpXmlSyncTask asyncTask = new HttpXmlSyncTask(context, http2, phone, data, callback);
                asyncTask.execute();
            }else if(result.trim().equalsIgnoreCase("error")){
                Toast.makeText(context, R.string.server_not_online, Toast.LENGTH_LONG).show();
                PreferencesActivity.SERVER_ONLINE = "NO";
            }else if(result.contains("number")){
                Toast.makeText(context, R.string.phone_not_in_server, Toast.LENGTH_LONG).show();
                PreferencesActivity.SERVER_ONLINE = "NO";
            }else{
                Toast.makeText(context, result.toString(), Toast.LENGTH_LONG).show();
                PreferencesActivity.SERVER_ONLINE = "NO";
            }
        }
    }

    /**
     * call the server in order to know if it is on line
     * @param http server url
     * @param phone client phone number
     * @param data the string "test"
     * @return a call response as a string
     */
    private String postCall(String http, String phone, String data) {
        /**
         * set parameter
         */
        String result = "";
        HttpPost httpPost = new HttpPost(http);
        HttpParams httpParameters = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
//        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
        nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
        nameValuePair.add(new BasicNameValuePair("data", data));
        /**
         *  Url Encoding the POST parameters
         */
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

    /**
     * check if the device has a data connection
     * @return
     */
    private boolean isOnline(){
        ConnectivityManager cm = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if(ni == null){
            return false;
        }
        return ni.isConnected();
    }
}