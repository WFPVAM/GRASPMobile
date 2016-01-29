package it.fabaris.wfp.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.activities.FormListSubmittedActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.listener.MyCallback;

/**
 * This class is called when the user wants to send a completed
 * form to the server.
 * Before to really send the form in doInBackground
 * check if the server is on line, using controlConnection method, if 
 * yes in onPostExecute send the form using an HttpCheckPostTask
 * object 
 */

public class HttpCheckAndSendPostTask extends AsyncTask<String, Void, String> {
    boolean formHasImages;
    ProgressDialog pd;
    String http;
    String http1;//test url
    String http2;//response url
    String http3;
    String phone;
    String data;//the form to send
    boolean isSendAllForms;
    String formId;
    Context context;
    MyCallback callback;
    Lock lock;

    private String result = "";

    public HttpCheckAndSendPostTask(Context context, String http, String phone,
                                    String data, MyCallback callback, boolean isSendAllForms, String formId, boolean formHasImages) {
        this.context = context;
        this.http = http;
        this.isSendAllForms = isSendAllForms;
        //RC modifica per adattarsi al reporting 21/03/2014
        //change the url format depending from the server
        if(http.contains(".aspx"))//if the server is the web reporting
        {
            this.http1 = http+"?call=test";
            this.http2 = http+"?call=response";
            this.http3= http+"?call=editedResponse";
        }
        else//if the server is the desktop designer
        {
            this.http1 = http+"/test";
            this.http2 = http+"/response";
            this.http3= http+"/call=editedResponse";
        }
        this.phone = phone;
        this.data = data;
        this.callback = callback;
        this.lock = new ReentrantLock();
        this.formId=formId;
        this.formHasImages= formHasImages;
        result = "";
    }

    /**
     * show the dialog
     */
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

    /**
     * check if the server is on line.
     */
    @Override
    protected String doInBackground(String... params)
    {

          if(data == null){
              return "empty";
          }
//        if(formId.contains("_img")){
//
//        }
        result = ControlConnection(http1, phone, data,formId);
        Log.i("result in doInBackground", result);
        return result;
    }

    /**
     * really send the form to the server
     * using HttpSendPostTask
     */
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
                if(!isSendAllForms)//if we are not sending all the form together show the result toast now
                    Toast.makeText(context, R.string.server_on_line, Toast.LENGTH_LONG).show();
                PreferencesActivity.SERVER_ONLINE = "YES";

                lock.lock();
                try
                {
                    synchronized (lock)
                    {
                        //send the form to the server
                        if (FormListSubmittedActivity.resendTask)
                        http2=http3;

                        HttpSendPostTask asyncTask = new HttpSendPostTask(context, http2, phone, data, callback, lock, isSendAllForms,formId,formHasImages);

                        Log.i("FUNZIONE HttpSendPostTask", "thread: "+ data);
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
                            // ok
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
            } //if something goes wrong
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
            else if (result.equalsIgnoreCase("empty"))
            {
                Toast.makeText(context, "file does not exist!",
                        Toast.LENGTH_SHORT).show();

            }
            else
            {
                Toast.makeText(context, result.toString(), Toast.LENGTH_SHORT).show();
                PreferencesActivity.SERVER_ONLINE = "NO";
            }
        }


    }

    //--------------------------------------------------------------------------------

    /**
     * not used
     * @param http
     * @param phone
     * @param data
     * @return
     */
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
                //return result = "ok-" + result;
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return result = "error";
        }
    }

    /**
     * if the device has a web connection and the url
     * has been set, then call the server
     * in order to know if it is on line
     * @param http1 server url
     * @param phone client phone number
     * @param data xml form
     * @return a feedback for the user
     */
    private String ControlConnection(String http1, String phone, String data,String formId)
    {
        String result = "";
        if (http1.startsWith("http://") || (http1.startsWith("Http://")) && http1.length() > 7)
        {
            if (isOnline())
                result = postControlConnection(http1, phone, data,formId);
            else
                result = "Offline";
        }
        else
            result = "Invalid URL";

        return result;
    }

    /**
     * @return true if the device has a web connection, false otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        }
        return ni.isConnected();
    }

    /**
     * call the server to know if it is online
     * @param http server url
     * @param phone client phone number
     * @param data xml form
     * @return
     */
    private String postControlConnection(String http, String phone, String data,String formId) {
        /**
         *  set parameter
         */
        String result = "";
        HttpPost httpPost = new HttpPost(http);
        HttpParams httpParameters = new BasicHttpParams();
        // HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        // HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
        nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
        nameValuePair.add(new BasicNameValuePair("data", data));
        nameValuePair.add(new BasicNameValuePair("formName", formId));
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