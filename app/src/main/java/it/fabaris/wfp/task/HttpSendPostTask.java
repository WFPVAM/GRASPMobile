package it.fabaris.wfp.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
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
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.activities.FormListFinalizedActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.listener.MyCallback;

/**
 * Class that defines the task that send the xform to the server
 *
 */

public class HttpSendPostTask extends AsyncTask<String, Void, String> {
    ProgressDialog pd;
    String http; //server url
    String phone;//client phone number
    String data; //the form to sent
    boolean isSendAllForms;//LL 17-04-2014 to manage the toast's msg when we have to send more forms together
    Context context;
    MyCallback callback; //called after the form has been sent
    private Lock condCheck;
    private String IMEI = "";
    private TelephonyManager mTelephonyManager;
    private String formName;
    boolean isImage= false;
   boolean formHasImages;
    /**
     * set the data needed to send the form
     * @param context
     * @param http server url
     * @param phone client phone number
     * @param data xform
     * @param callback
     * @param cond if we are sending all the form together
     * @param isSendAllForms
     * @param formHasImages
     */
    public HttpSendPostTask(Context context, String http, String phone,
                            String data, MyCallback callback, Lock cond, boolean isSendAllForms, String formName, boolean formHasImages) {
		
		/*
		 * set the url format depending from the server
		 */
        if(http.contains(".aspx"))//if the server is web reporting
        {
            this.http = http+"?call=response";
        }
        else//if the server is desktop designer
        {
            this.http = http+"/response";
        }


        this.context = context;
        this.http = http;
        this.phone = phone;
        this.data = data; //la form da inviare
        this.callback = callback;
        this.condCheck = cond;
        this.isSendAllForms = isSendAllForms;
        this.formName=formName;
        this.formHasImages=formHasImages;

        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.IMEI = mTelephonyManager.getDeviceId();
        if (IMEI != null && (IMEI.contains("*") || IMEI.contains("000000000000000"))) {
            IMEI =
                    Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }



    }

    /**
     * show dialog before to do anything
     */
    @Override
    protected void onPreExecute() {

        pd = ProgressDialog.show(context,
                context.getString(R.string.checking_server),
                context.getString(R.string.wait));


    }

    /**
     * send the form
     */
    @Override
    protected String doInBackground(String... params) {
        String result = "";
        if (!isOnline()) {
            result = "offline";
        } else if (PreferencesActivity.SERVER_ONLINE == "NO") {
            result = "server error";
        } else {
            if(formName.contains("_image")||formName.contains("_video")){
                isImage=true;
            }
            result = postCall(http, phone, data,formName);
        }
        return result;
    }

    /**
     * dismiss dialog and give a feedback to the user
     */
    @Override
    protected void onPostExecute(String result)
    {
        Log.i("httpSendPostTaskOnPostEx", result);
        if(pd.isShowing() && result != null)
        {
            pd.dismiss();

            if (result.equalsIgnoreCase("Offline"))
            {
              //  FormListCompletedActivity.updateFormToFinalized();
                Toast.makeText(context, R.string.device_not_online,	Toast.LENGTH_SHORT).show();
                if (callback != null)
                {
                    callback.callbackCall();
                }
            }
            else if (result.trim().equalsIgnoreCase("server error"))
            {
               // FormListCompletedActivity.updateFormToFinalized();
                Toast.makeText(context, R.string.check_connection, Toast.LENGTH_SHORT).show();
                if (callback != null)
                {
                    callback.callbackCall();
                }
            }
            else if (result.trim().equalsIgnoreCase("formnotonserver"))
            {
            //    FormListCompletedActivity.updateFormToFinalized();
                Toast.makeText(context, R.string.form_not_available, Toast.LENGTH_SHORT).show();
            }
            else if (result.trim().equalsIgnoreCase("Error"))
            {
                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
            }
            else if (result.trim().toLowerCase().startsWith("ok"))
            {
                Log.i("RESULT", "messaggio ricevuto dal server");

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
                 * UPDATE THE FORM STATE IN FORMS DB
                 */
                if(!isImage) {
                   if(!formHasImages)
                   {
                       FormListCompletedActivity.updateFormToSubmitted();
                   }
                    else {
                       FormListCompletedActivity.updateFormToFinalized();
                   }
                }
                else{
                    FormListFinalizedActivity.updateFormToSubmitted();
                }
                if(!isSendAllForms)//se non si stanno inviando piu' form insieme
                {
                    if(!isImage) {
                        Toast.makeText(context, R.string.forms_sent, Toast.LENGTH_LONG).show();
                        Toast.makeText(context, R.string.gotToSubmitImg, Toast.LENGTH_LONG).show();
                    }  else
                        Toast.makeText(context, R.string.AllImgsSent, Toast.LENGTH_LONG).show();
                }
                if (callback != null)
                {
                    callback.callbackCall();
                    synchronized (condCheck)
                    {
                        condCheck.notify();
                    }
                  callback.finishFormListFinalized();
                }


            }
        }
    }


    /**
     * send the xform to the server using a
     * DefaultHttpClient object
     * @param url server url
     * @param phone client phone number
     * @param data xform
     * @return the call response as a string
     */
    private String postCall(String url, String phone, String data,String formName) {
        /**
         *  set parameter
         */
        Log.i("URL ------------ ", url);

        String result = null;
        HttpPost httpPost = new HttpPost(url);
        HttpParams httpParameters = new BasicHttpParams();
        // HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        // HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
        nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
        nameValuePair.add(new BasicNameValuePair("data", data));
        nameValuePair.add(new BasicNameValuePair("formName", formName));

        if(http.contains(".aspx")){
            nameValuePair.add(new BasicNameValuePair("imei", IMEI));//solo se stiamo spedendo la form al server web inviamo anche l'IMEI
        }


        // Url Encoding the POST parameters
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return result = "error";
        }
        /**
         *  Doing HTTP Request
         */
        try {
            HttpResponse response = httpClient.execute(httpPost);
            result = EntityUtils.toString(response.getEntity());

            if (result.equalsIgnoreCase("\r\n")) {
                return result = "formnotonserver";
            } else {
               // return result = "ok-" + result;
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return result = "error";
        }
    }

    /**
     * @return true if the advice has a data connection, false otherwise
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
}