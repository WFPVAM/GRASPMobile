package it.fabaris.wfp.task;

import it.fabaris.wfp.activities.FormEntryActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.javarosa.form.api.FormEntryPrompt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

/**
 * not used
 */
public class SetHiddenWidgetValueTask extends AsyncTask<String, String, String>{

    String formPath = "";//path dell'xml che contiene il tamplate della form
    FormEntryPrompt mPrompt;//oggetto che contiene le info sul widget che stiamo costruendo
    EditText mAnswer;//EditText da valorizzare (singelLine o Multiline)
    String widgetAnswer = "";//qui mettero' il testo da mettere nel widget
    FormEntryActivity fea;
    String widgetName = "";//nome dell'elemento da cercare

    public SetHiddenWidgetValueTask (String XmlTamplateFormPath, FormEntryPrompt actualFep, EditText actualWidget){
        this.formPath = XmlTamplateFormPath;
        this.mPrompt = actualFep;
        this.mAnswer = actualWidget;
        this.widgetName = mPrompt.mTreeElement.getName();//nome dell'elemento da cercare

    }
    @Override
    protected String doInBackground(String... arg0) {
        // TODO Auto-generated method stub

        //prendo l'xml del file
        File formXml = new File(formPath);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(formXml);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String FormTamplateXml = readTextFile(inputStream);//l'xml che contiene il tamplate della form


        Document TamplateFormXmlDom = getDomElement(FormTamplateXml);//creo il DOM dell'xml
        NodeList nl = TamplateFormXmlDom.getElementsByTagName(widgetName);//prendo la lista di tutti i nodi che hanno il nome dell'widget che stiamo costruendo


        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            //ciclo sul dom per trovare l'elemento che mi interessa
            if(getValue(e, widgetName) != ""){//se c'� un valore imposta il testo da mettere nel widget 
                widgetAnswer = getValue(e, widgetName);
            }
        }

        return widgetAnswer;
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != ""){
            mAnswer.setText(result);//setto il testo dell'EditText con il valore trovato nell'xml
        }
    }


    private String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {

        }
        return outputStream.toString();
    }

    public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);

        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        // return DOM
        return doc;
    }


    public String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }

    public final String getElementValue( Node elem ) {
        Node child;
        if( elem != null){
            if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

}
