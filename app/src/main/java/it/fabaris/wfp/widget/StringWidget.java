/*******************************************************************************
 * Copyright (c) 2012 Fabaris SRL.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Fabaris SRL - initial API and implementation
 ******************************************************************************/
package it.fabaris.wfp.widget;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.utility.ConstantUtility;
import it.fabaris.wfp.view.ODKView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 
 * Class that implements the string answers
 * 
 */

public class StringWidget extends QuestionAndStringAswerWidget 
{
	int answerint = 0;
	

	public StringWidget(final Context context, final FormEntryPrompt prompt) {
		super(context, prompt);
	
		
		//**********************************
		//**********************************

		
		// 11/10/2013  ------------------------------------------
		/**
		 * after a question has been answered then assign the right color 
		 * to the question and save the answer in the disk
		 */
		mAnswer.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				
				
	//				if(before==count)return;
					try{
						HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
						Set<FormIndex> indexKeys = answers.keySet();
							
							final FormIndex index = StringWidget.this.getPrompt().getIndex();
							int saveStatus = 0;
							if(!mReadOnly)
								saveStatus = ((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
							
							
							switch (saveStatus) {
							case 0:
								assignStandardColors();
								if(mReadOnly)
								{
									break;
								}
								break;
							case 1:	
								if((mAnswer.getText().toString()).equals("")){
									assignMandatoryColors();
								}else {
									assignStandardColors();
								}
								//costanti violate
								break;
							case 2:
								assignErrorColors();
								break;
								
							default:
								//mAnswer.clearFocus();
								((FormEntryActivity) context).refreshCurrentView(index);
								break;
							}
					}catch(Exception e){
						e.printStackTrace();
						return;
					}
			}
		});
		
		
		
		
		/**
		 * when the widget loses the focus refresh the current view
		 */
		mAnswer.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!(hasFocus || ((FormEntryActivity) context).verifica)){
					/*((FormEntryActivity) context).refreshCurrentView(index,);
					 mAnswer.setFocusable(true);*///commentato per bug su roster e spostato sotto dopo il controllo del passaggio sull'onFling	
					 
					//((FormEntryActivity) context).refreshCurrentView(index);	//<---------------------------------------12/11/2013
					//mAnswer.setFocusable(true);	//<---------------------------------------12/11/2013								
				 	
					 
					//has been added a check before to refresh, because the refresh in this case causes some bugs on rosters
					 String flagOnCalculated = ConstantUtility.getFlagCalculated();
					 if(flagOnCalculated.equals("no")){
						 	((FormEntryActivity) context).refreshCurrentView(index);	
						 	//mAnswer.setFocusable(true);									//<---------------------------------------12/11/2013
						 	ConstantUtility.setFlagCalculated("no");
						 	
					 	}else{
					 		ConstantUtility.setFlagCalculated("no");
					 	}
			 }
				 ((FormEntryActivity) context).verifica = false;	
				 //------------------------------ 13/11/2013
			}
		});
		//-------------------------------------------------------
		
		
		
		
		
		
	}
	/**
	 * show the answer given and change the color
	 * of the question Widget
	 */
	public void syncAnswerShown() {
		/*  ***************   CORRETTA  11/11/2013   *****************
		if(mPrompt != null)
		{
			if(checking == false)
				checking = true;
			checking = false;
			String s = mPrompt.getAnswerText();
			if (s != null) {
				mAnswer.setText(s);
			}
			syncColors();
		}
		*/
		String version = "";
		
		PackageInfo pInfo;
		try {
			pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		String str = null;
		if(mPrompt != null)
			str = mPrompt.getIndex().toString();
		
		
		if(FormEntryActivity.ROSTER)
		{
			if(mPrompt != null)
			{
				if(mPrompt.isReadOnly())
				{
					if(str.contains("_0"))
					{
						FormEntryActivity.readOnlyInRoster.put(mPrompt.getIndex().toString(), mPrompt.getAnswerValue());
						
						//mAnswer.setText(mPrompt.getAnswerText());
					}	
					else if(mPrompt.getAnswerText() == null)
					{
                        String ind= mPrompt.getIndex().toString();
                        if(ind.contains("_"))
                            ind= ind.split("_")[1];

                        if(ind.contains(","))
                            ind= ind.split(",")[0];

                        String ind2 = mPrompt.getIndex().toString().replace("_"+ind, "_0");
						
						int num = ind2.length();
						try {
							IAnswerData value = FormEntryActivity.readOnlyInRoster.get(ind2);
							mAnswer.setText(value.getDisplayText().toString());
							
							//*****save
							HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
							Set<FormIndex> indexKeys = answers.keySet();
							((FormEntryActivity) context).saveAnswer(answers.get(mPrompt.getIndex()), mPrompt.getIndex(), true);
							
						
							//answers.put(mPrompt.getIndex(), value); commentato da armando
							
							
							
							//mPrompt = null; //LL 03-03-2014 commentato perche' manda in errore il caricamento di una nuova compilazione di roster in caso di 
												//presenza di regola di visibilita' del tipo valoreCampoX != unCertoValore
						}
						catch(Exception e) 
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		
		if(mPrompt != null)
		{
			if(checking == false)
				checking = true;
			
			checking = false;
			String s = mPrompt.getAnswerText();
			if (s != null) {
				if(mPrompt.getFormElement().getLabelInnerText() != null)
				{
					if (mPrompt.getFormElement().getLabelInnerText().equals("Client version")){
						//set client version
						mAnswer.setText(version);
					}else{
						//set designer version
						mAnswer.setText(s);
					}
				}
				else{
					mAnswer.setText(s);
				}
			}else{
				if(mReadOnly){//if is a read only is a singleline or a multiline
					String formPath = Collect.FORMS_PATH + "/" + FormEntryActivity.formName+ ".xml";//path dell'xml che contiene il tampalte della form
					try {
						setHiddenStringWigetText(formPath);//LL 12-05-2014 metodo aggiunto per recuperare il testo di un single o di un multi line buttato perche' 
															//sottoposto alla regola di visibilita' di un widget che non e' stato valorizzato prima di uscire dalla form.
					}
					catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			syncColors();
		}
	}


	/**
	 * reset the text value of the answer Widget
	 */
	public void clearAnswer() {
		mAnswer.getText().replace(0, mAnswer.getText().length(), "", 0, 0);

	}

	/**
	 * get the given answer from the widget
	 */
	@Override
	public IAnswerData getAnswer() {
		String s = mAnswer.getText().toString();
		if (s == null || s.equals("")) {
			//nConstraint=false;
			return null;
		} else {
			return new StringData(s);
		}
	}
	
	/**
	 * set the answer as blank when remove a QuestionWidget
	 * or a QuestionAndStringAnswerWidget
	 */
	public IAnswerData setAnswer(IAnswerData a)
	{
		a.setValue("");
		return a;
	}
	
	

	@Override
	public void setFocus(Context context) {
		if (!mReadOnly) {
			mAnswer.requestFocus();
		} else {
			View next = focusSearch(FOCUS_FORWARD);
			if (next==null){
				next = focusSearch(FOCUS_DOWN);
			}
			if (next==null){
				next = focusSearch(FOCUS_RIGHT);
			}
			if (next !=null){
				next.requestFocus();
			} else {
				Log.e("StringWidget", "I MUST take focus. But I shouldn't!");
			}

		}
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.isAltPressed() == true) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mAnswer.setOnLongClickListener(l);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mAnswer.cancelLongPress();
	}
	
	
	/** * 
	 *The method gets the form's template from the xml file. 
	 * From the template we take the text value of the singleline and 
	 * of the multiline that are been wrongly deleted
	 * when, starting from the first compilation of the form,
	 * the widget from whose depends the visibility of the
	 * multiline or singleine has not been
	 * valorized and we exit the form without saving it
	 * @param xmlTamplatePath the path of the xml form template
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private void setHiddenStringWigetText(String xmlTamplatePath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		//get the xml file
				File formXml = new File(xmlTamplatePath);
				InputStream inputStream = null;
		        try {
		        	inputStream = new FileInputStream(formXml);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} 
				String XmlFormTamplate = readTextFile(inputStream);//the xml with the form's template
				String widgetName = mPrompt.mTreeElement.getName();//name of the element to look for
				String stringWidgetText = "";//the string to use in order
											//to set the text of the single or multi line
				
				
				InputSource source = new InputSource(new StringReader(XmlFormTamplate));

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document document = db.parse(source);
				NodeList mynodes = document.getElementsByTagName(widgetName);
				Element e = (Element) mynodes.item(0);
				stringWidgetText = e.getTextContent();
				mAnswer.setText(stringWidgetText);//set the text of the answer Widget with the text
												  //founded in the xml
	}
	
	/**
	 * 
	 * @param inputStream
	 * @return the string with the xml template
	 */
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
}