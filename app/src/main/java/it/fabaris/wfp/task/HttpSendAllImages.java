package it.fabaris.wfp.task;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
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
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.parse.XFormParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import it.fabaris.wfp.activities.FormListCompletedActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.provider.FormProvider;
import it.fabaris.wfp.utility.FileUtils;
import it.fabaris.wfp.utility.NewFileUtils;
import object.FormInnerListProxy;

/**
 * Created by Claudia Alawi
 */
public class HttpSendAllImages extends AsyncTask<String, Void, String> {
    ProgressDialog pd;
    String http;//server url
    String phone;//client phone number
    static String data;//the form
    int numOfFormSent;//contains the nth number of submitted form
    Context context;
    MyCallback finishImageList;
    public String IMEI = "";
    private TelephonyManager mTelephonyManager;
    ////////////////////////////////////////
    String ImageResult="";
    //parcelable object that contains useful info about the image
    ArrayList<FormInnerListProxy> imageslistfirst; //fabaris parcelable object
    static String date;
    private HashMap<String, String> imageNotSent;

    /**
     * set the data needed to send the form
     *
     * @param context
     * @param http                    part of the server url
     * @param phone                   client phone number
     * @param completedformslistfirst
     * @param finishImageList
     */
    public HttpSendAllImages(Context context, String http, String phone, ArrayList<FormInnerListProxy> completedformslistfirst,//LL 14-05-2014
                                MyCallback finishImageList) {
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
        this.imageslistfirst = completedformslistfirst;
        this.finishImageList = finishImageList;


        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.IMEI = mTelephonyManager.getDeviceId();
        if (IMEI != null && (IMEI.contains("*") || IMEI.contains("000000000000000"))) {
            IMEI =
                    Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

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
        } else {//if the device is connected, call the server to send the images
            numOfFormSent = 1;//set the number of forms to send, 1 to start
            for (FormInnerListProxy mydata : imageslistfirst) {//loop on "the form to send" list
                String str =mydata.getStrPathInstance();
//                String str1[] = str.replace("/storage/emulated/0/GRASP/instances/","").split("/");
//                String formName = str1[0];
                String str1[] = str.substring(str.lastIndexOf("instances")).split("/");
                String formName = str1[1];

                byte[] fileBytes = FileUtils.getFileAsBytes(new File(str));
                TreeElement dataElements = XFormParser.restoreDataModel(fileBytes, null).getRoot();
                String imageName = "";

            //    int numberOfImages = readSubmittedImages(mydata.getStrPathInstance()).size();

//
                        int imageNameCount =0;

                        for (int j = 0; j < dataElements.getNumChildren(); j++) {
                    if (dataElements.getChildAt(j) != null && dataElements.getChildAt(j).getValue() != null && dataElements.getChildAt(j).getValue().getDisplayText().indexOf("jpg") > 0) {
                        imageName = dataElements.getChildAt(j).getValue().getDisplayText();
                        imageNameCount ++;
                        if (imageName.contains("/instances")) {
                            imageName = imageName.substring(imageName.lastIndexOf("/") + 1);
                        }

                        String imagePath= str.substring(0, str.lastIndexOf("/") + 1) + imageName;
//                        Bitmap originalImage = BitmapFactory.decodeFile(imagePath);
//                        originalImage = scaleDown(originalImage, true); //resize
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        originalImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
////                      byte[] imageBytes = baos.toByteArray();
//                        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//
//                        String formNameImage= formName + "_image"+imageName;

                        byte[] compresedImageBytes = FileUtils.compressImage(imagePath,1600,1200,80);
                        String encodedImage = Base64.encodeToString(compresedImageBytes, Base64.DEFAULT);
                        String formNameImage= formName + "_" + imageName + "_image";
                        result=sendImage(http,phone,encodedImage,formNameImage);

//                        result=sendImage(http,phone,encodedImage,formNameImage);
                    }
        }
//


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


                         updateFormToSubmitted(mydata);//update forms.db


                    } else {//the form has not been received from the server or something has gone bad
                        if (imageNotSent != null && mydata != null) {
                            imageNotSent.put(mydata.getFormNameInstance(), result);//list of the forms not sent with the motivation of the failure
                        } else {
                            Log.i("the data is ", "fail to send message to server");
                            return "ko";
                        }
                        //form not sent name-> key = formNameInstance
                        //motivation of the failure-> value = answer from the server
                    }

            }
        }
        if (imageNotSent != null) {
            if (!imageNotSent.isEmpty()) {
                return "ko";
            }
//            else if(formResult=="empty"){
//                return "ko";
//            }
            else { //if there were some problems during the sending
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

            if (result == "ok") {//if all the forms are been sent
                Toast.makeText(context,R.string.AllImgsSent , Toast.LENGTH_SHORT).show();

            } else {// not all the forms are been sent correctly
                Toast.makeText(context, R.string.problemSendingImg, Toast.LENGTH_SHORT).show();
            }}
        else {// not all the forms are been sent correctly
            Toast.makeText(context, R.string.imgDNE, Toast.LENGTH_SHORT).show();

        }


        if (finishImageList != null) {

            finishImageList.finishFormListFinalized();
        }


    }

    /**
     * effectively send the form to the server
     *
     * @param url of the server
     * @param phone  client phone number
     * @param encodedImage   xml form
     * @return the call response
     */
    private String sendImage(String url, String phone, String encodedImage, String formName) {

        String result = null;
        HttpPost httpPost = new HttpPost(url);
        HttpParams httpParameters = new BasicHttpParams();
        // HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        // HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(3);
        nameValuePair.add(new BasicNameValuePair("phoneNumber", phone));
        nameValuePair.add(new BasicNameValuePair("Image", encodedImage));
        nameValuePair.add(new BasicNameValuePair("formName", formName));

        if (http.contains(".aspx")) {
            nameValuePair.add(new BasicNameValuePair("imei", IMEI));//only if we are sending the form to the server, we send the IMEI
        }

        // Url Encoding the POST parameters
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, HTTP.UTF_8));
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
                // return result = "ok-" + result;
                return result;
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
         *
         *
         */
        GregorianCalendar gc = new GregorianCalendar();
        String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));

        String year = Integer.toString(gc.get(Calendar.YEAR));

        date = day + "/" + month.format(rightNow.getTime()) + "/" + year;

        String time = getCurrentTimeStamp();
        date = date + "  " + time;
        // -----------------------------------------------------

        String displayNameInstance = form.getFormNameInstance();
        FormProvider.DatabaseHelper dbh = new FormProvider.DatabaseHelper("forms.db");
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
     * @throws javax.xml.transform.TransformerException
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

    public static Bitmap scaleDown(Bitmap realImage, boolean filter) {

        int width = (int) Math.round(0.8* realImage.getWidth());
        int height = (int) Math.round(0.8 * realImage.getHeight());


        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,height, filter);
        return newBitmap;
    }

}
