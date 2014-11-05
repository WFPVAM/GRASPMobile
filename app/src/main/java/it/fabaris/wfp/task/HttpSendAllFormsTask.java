package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.FormListActivity;
import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.logic.PropertyManager;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.utility.FileUtils;
import it.fabaris.wfp.utility.FormCompletedDataDBUpdate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
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

import it.fabaris.wfp.utility.NewFileUtils;
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
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.xform.parse.XFormParser;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import utils.ApplicationExt;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * This class is called when the user wants to send
 * all the completed forms together, by clicking
 * on the "send all forms" button, shown
 * at the bottom of the completed forms tab.
 *
 * @author BrainsEngineering Laura
 */
public class HttpSendAllFormsTask extends AsyncTask<String, Void, String> {

    ProgressDialog pd;
    String http;//server url
    String phone;//client phone number
    static String data;//the form
    int numOfFormSent;//contains the nth number of submitted form
    Context context;
    MyCallback finishFormListCompleted;
    public String IMEI = "";
    private TelephonyManager mTelephonyManager;


    //parcelable object that contains useful info about the form
    ArrayList<FormInnerListProxy> completedformslistfirst; //fabaris parcelable object
    //ArrayList<FormInnerListProxy> completedformslistsecond; //oggetto parcellizzato di armando //LL 14-05-2014 eliminato

    static String date;
    private HashMap<String, String> formNotSent;


    //public HttpSendAllFormsTask(Context context, String http, String phone, ArrayList<FormInnerListProxy> completedformslistfirst, ArrayList<FormInnerListProxy> completedformslistsecond,//LL 14-05-2014

    /**
     * set the data needed to send the form
     *
     * @param context
     * @param http                    part of the server url
     * @param phone                   client phone number
     * @param completedformslistfirst
     * @param finishFormListCompleted
     */
    public HttpSendAllFormsTask(Context context, String http, String phone, ArrayList<FormInnerListProxy> completedformslistfirst,//LL 14-05-2014
                                MyCallback finishFormListCompleted) {
        this.context = context;

		/*
         * set the url format depending from the server
		 */
        if (http.contains(".aspx"))//if the server is web reporting
        {
            this.http = http + "?call=response";
        } else//if the server is desktop designer
        {
            this.http = http + "/response";
        }

        this.phone = phone;
        this.completedformslistfirst = completedformslistfirst;
        //this.completedformslistsecond = completedformslistsecond;//LL 14-05-2014 dimsissione db grasp
        this.finishFormListCompleted = finishFormListCompleted;


        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.IMEI = mTelephonyManager.getDeviceId();
        if (IMEI != null && (IMEI.contains("*") || IMEI.contains("000000000000000"))) {
            IMEI =
                    Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    /**
     * show dialog
     */
    @Override
    protected void onPreExecute() {
        Log.i("pd ", "Helloooooooooooooooooooooooooooooooooooooooooooo");

        this.pd = ProgressDialog.show(context,
                context.getString(R.string.checking_server),
                context.getString(R.string.wait));
    }

    /**
     * send all the form
     */
    @Override
    protected String doInBackground(String... params) {
        String result = "";
        if (!isOnline()) {//check if the device has data connection
            result = "offline";
        } else {//if the device is connected, call the server to send the forms
            numOfFormSent = 1;//set the number of forms to send, 1 to start
            for (FormInnerListProxy mydata : completedformslistfirst) {//loop on "the form to send" list
                data = decodeForm(mydata);//the form to send, encoded

//                Log.i("data  in httpSendPostTaskOnPostEx ", data);

                result = sendFormCall(http, phone, data);

//
//                Log.i("===================================", "");
//                Log.i("httpSendPostTaskOnPostEx", result);
//                Log.i("===================================", "");


                if (result.trim().toLowerCase().startsWith("ok") && !result.trim().toLowerCase().contains("HTTP 404")) //the server answered ok. The form has been
                //received correctly from the server
                {
//                    Log.i("RESULT", "Message return from server");
                    numOfFormSent = numOfFormSent++;
                    //--------------------------------------------------------------------------
                    XPathFactory factory = XPathFactory.newInstance();
                    XPath xPath = factory.newXPath();
                    XPathExpression xPathExpression;
                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    try {
                        DocumentBuilder builder = builderFactory.newDocumentBuilder();

                        String clearResult = result.substring(result.indexOf("-") + 1);
                        clearResult = clearResult.replaceAll("&", " ");

                        ByteArrayInputStream bin = new ByteArrayInputStream(clearResult.getBytes());
                        Document xmlDocument = builder.parse(bin);
                        bin.close();
                        xPathExpression = xPath.compile("/response/formResponseID");
                        String id = xPathExpression.evaluate(xmlDocument);

                        FormListCompletedActivity.setID(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /**
                     * REFRESH THE FORM STATE IN THE FORMS DB
                     */
                    updateFormToSubmitted(mydata);//update forms.db
                    //UpdateDBAfterSendAllForms(mydata);//update GRASP.DB //LL 14-05-2014 dismissione db grasp



						/*
                        if(numOfFormSent == completedformslistfirst.size()){//se sono state spedite tutte le form lancia il finish su formListCompletedActivity
							FormListCompletedActivity fmla = new FormListCompletedActivity();
							fmla.finishListCompletedActivity();
						}
						*/


                } else {//the form has not been received from the server or something has gone bad
                    if (formNotSent != null && mydata != null) {
                        formNotSent.put(mydata.getFormNameInstance(), result);//list of the forms not sent with the motivation of the failure
                    } else {
                        Log.i("the data is ", "fail to send message to server");
                        return "ko";
                    }
                    //form not sent name-> key = formNameInstance
                    //motivation of the failure-> value = answer from the server
                }
            }
        }
        if (formNotSent != null) {
            if (!formNotSent.isEmpty()) {
                return "ko";
            } else { //if there were some problems during the sending
                //and some or all the forms are not been sent to the server
                return "ok";
            }
        } else {
            return "ok";
        }
    }


    public Hashtable<String, String> readSubmittedImages(String filePath) {
        /**
         *  convert files into a byte array
         */
        byte[] fileBytes = FileUtils.getFileAsBytes(new File(filePath));
        Hashtable<String, String> images = new Hashtable<String, String>();


        /**
         *  get the root of the saved and template instances
         */
        TreeElement dataElements = XFormParser.restoreDataModel(fileBytes, null).getRoot();
        String imageName = "";
        for (int j = 0; j < dataElements.getNumChildren(); j++) {
            if (dataElements.getChildAt(j) != null && dataElements.getChildAt(j).getValue() != null && dataElements.getChildAt(j).getValue().getDisplayText().indexOf("jpg") > 0) {
                imageName = dataElements.getChildAt(j).getValue().getDisplayText();
                if(imageName.contains("/instances")){
                    imageName= imageName.substring(imageName.lastIndexOf("/") + 1);
                }

                File originalImage = new File(filePath.substring(0, filePath.lastIndexOf("/") + 1) + imageName);
                if (originalImage.exists()) {
                    try {
                        String plus = "\\+";
                        String syncImagesPath = Collect.IMAGES_PATH + "/" + phone.replaceAll(plus, "");
                        if (FileUtils.createFolder(syncImagesPath)) {
                            File newImage = new File(syncImagesPath + "/" + imageName);
                            NewFileUtils.copyFile(originalImage, newImage);
                            images.put(dataElements.getChildAt(j).getName(), phone.replaceAll(plus, "") + "\\" + imageName);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return images;
    }


    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        ////////dismiss dialog
        if (pd != null) {
            if (pd.isShowing()) {
                pd.dismiss();
            }
        }
        if (result == "ok") {//if all the forms are been sent
            Toast.makeText(context, "All forms have been sent succesfully", Toast.LENGTH_SHORT).show();
        } else {// not all the forms are been sent correctly
            Toast.makeText(context, "There has been some problems sending one or more forms", Toast.LENGTH_SHORT).show();
            //////metto il salvataggio di quelle che stanno qui nelle finalizzate?
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


        if (finishFormListCompleted != null) {

            finishFormListCompleted.finishFormListCompleted();
        }


    }

    /**
     * effectively send the form to the server
     *
     * @param url of the server
     * @param phone  client phone number
     * @param data   xml form
     * @return the call response
     */
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

        if (http.contains(".aspx")) {
            nameValuePair.add(new BasicNameValuePair("imei", IMEI));//only if we are sending the form to the server, we send the IMEI
        }

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

    /**
     * @return true if the device has data connection false otherwise
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni == null) {
            return false;
        }
        return ni.isConnected();
    }

    //partendo dalla form i cui riferimenti sono stati presi dall'oggetto parcellizzato crea la form da inviare al server

    /**
     * starting from parcelable objects create the form, then send it
     *
     * @param form xml form
     * @return call response
     */
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
        return xml;
    }


    /**
     * not used. grasp db has been dismissed
     *
     * @param form
     */
    public void UpdateDBAfterSendAllForms(FormInnerListProxy form) {
        String idFormNameInstance = form.getFormNameInstance();
        String nomeform = form.getFormName();
        String autore = getAuthor(form);
        updateFormsDataToSubmitted(nomeform + "&" + autore, date, autore, idFormNameInstance);
    }

    /**
     * update forms db and set the state of the form just sent to submitted
     *
     * @param form the form as parcelable object
     */
    public static void updateFormToSubmitted(FormInnerListProxy form) {


        Calendar rightNow = Calendar.getInstance();
        java.text.SimpleDateFormat month = new java.text.SimpleDateFormat(
                "MM");
        // ----------------------------------------------------------------------------------------
        /**
         *  sending date
         */
        GregorianCalendar gc = new GregorianCalendar();
        String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));

        String year = Integer.toString(gc.get(Calendar.YEAR));

        date = day + "/" + month.format(rightNow.getTime()) + "/" + year;

        String time = getCurrentTimeStamp();
        date = date + "  " + time;
        // -----------------------------------------------------

        String displayNameInstance = form.getFormNameInstance();
        DatabaseHelper dbh = new DatabaseHelper("forms.db");
        String updatequery = "UPDATE forms SET status='submitted', submissionDate = '" + date + "' WHERE displayNameInstance = '" + displayNameInstance + "'";

        Log.i("FUNZIONE updateFormToSubmitted per la form: ", displayNameInstance);

        dbh.getReadableDatabase().execSQL(updatequery);


        dbh.close();
    }


    /**
     * create the file
     *
     * @param trans
     * @param doc
     * @param form
     * @return
     * @throws TransformerException
     */
    public String trasformItem(Transformer trans, Document doc,
                               FormInnerListProxy form) throws TransformerException, IOException {
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);


        Hashtable<String, String> images = readSubmittedImages(form.getStrPathInstance());
        if (images != null && images.size() > 0) {
            Set<String> keys = images.keySet();
            for (String key : keys) {
                NodeList nodeList = doc.getElementsByTagName(key);
                Node node = nodeList.item(0);
                node.setTextContent(images.get(key));
                Log.i("node.getTextContent() ------------ ", node.getTextContent());
            }
        }


//        //todo add the image binary to the xml file
//        Hashtable<String, String> images = readSubmittedImages(form.getStrPathInstance());
//        if (images != null && images.size() > 0) {
//            Set<String> keys = images.keySet();
//            for (String key : keys) {
//                NodeList nodeList = doc.getElementsByTagName(key);
//                Node node = nodeList.item(0);
//                node.setTextContent(images.get(key));
//            }
//        }


//        DOMSource source = new DOMSource(doc);
//        trans.transform(source, result);
//        String xmlString = sw.toString();
//        String apos = "apos=\"'\"";
//        xmlString = xmlString.replace(apos, "");


        NodeList nodeList = doc.getElementsByTagName("data");
        Node node = nodeList.item(0);
        node.getAttributes().removeNamedItem("apos");

        String xmlString = getStringFromDoc(doc);

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

    public String getStringFromDoc(Document doc) throws IOException {
        com.sun.org.apache.xml.internal.serialize.OutputFormat format = new com.sun.org.apache.xml.internal.serialize.OutputFormat(doc);
        StringWriter stringOut = new StringWriter();
        com.sun.org.apache.xml.internal.serialize.XMLSerializer serial = new com.sun.org.apache.xml.internal.serialize.XMLSerializer(stringOut, format);
        serial.serialize(doc);
        return stringOut.toString();
    }

    /**
     * @param testo
     * @return
     */
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


    /**
     * take the enumeratorID from the form using the parcelable object
     *
     * @param form parcelable object that represent the current form
     * @return the enumeratorID as a string
     */
    private String getAuthor(FormInnerListProxy form) {
        String author = form.getFormEnumeratorId();//LL 14-05-2014


		/*LL 14-05-2014 dimissione db grasp
		String formNameInstancefirst = form.getFormNameInstance();//prendo dalla form corrente l'identificativo che mi serve per ritrovare la form nell'oggetto parcellizzato di armando per prendermi l'enumeratorID
		for(int i = 0; i<completedformslistsecond.size(); i++){//CICLO SULL'oggetto parcellizzato di Armando
				if(completedformslistsecond.get(i).getFormName().toString().contains(formNameInstancefirst)){
					author = completedformslistsecond.get(i).getFormEnumeratorId();//quando trovo l'id che fa match ci setto l'autore
				}
		}*/


        return author;
    }

    /**
     * not used because grasp db has been dismissed
     *
     * @param nome_form
     * @param submitted_data
     * @param submitted_by
     * @param idFormDataBaseGras
     */
    public void updateFormsDataToSubmitted(String nome_form, String submitted_data, String submitted_by, String idFormDataBaseGras) {
        /**
         * UPDATE DB WITH THE GRASP DB
         */

        String submitted_id = nome_form + submitted_by;
        String filter = idFormDataBaseGras;
        //ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", submitted_id);//LL tolto per passare il giusto filtro per cancellare la form giusta


        ApplicationExt.getDatabaseAdapter().open().delete("COMPLETED", filter);//LL aggiunto per passare il giusto filtro per cancellare la form giusta
        ApplicationExt.getDatabaseAdapter().open().insert("SUBMITTED", submitted_id, idFormDataBaseGras, submitted_data, submitted_by);
        ApplicationExt.getDatabaseAdapter().close();
    }


    /**
     * @return the timestamp as a string
     */
    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

}
