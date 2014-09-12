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
/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package it.fabaris.wfp.widget;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.utility.ConstantUtility;
import it.fabaris.wfp.view.ODKView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.DateTime;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * A widget that restricts values to floating point numbers.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Fabaris Srl: Leonardo Luciani www.fabaris.it
 */
public class DecimalWidget extends StringWidget    //QuestionAndStringAswerWidget
{//StringWidget { //QuestionWidget {
	
	//String s = null;
	
	/*
	FormIndex index;
	boolean nConstraint = false;
	int answerint = 0;
	*/
	int answerint = 0;
	public boolean checking = false;
	
	/**
	 * set the layout of the answer Widget called "mAnswer" after
	 * an answer is given.
	 * set the layout of the question widget called "mQuestionText"
	 * after an answer is given.
	 * save the answer to the disk.
	 * @param context
	 * @param prompt prompt is aFormEntryPrompt object has the FormIndex, which is where the answer
	 *			 	 gets stored 
	 */
	public DecimalWidget(final Context context, final FormEntryPrompt prompt)
	{
		super(context, prompt);
		
		mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswer.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        mAnswer.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // needed to make long readonly text scroll
        mAnswer.setHorizontallyScrolling(false);
        mAnswer.setSingleLine(false);

        // only allows numbers and no periods
        mAnswer.setKeyListener(new DigitsKeyListener(true, true));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        mAnswer.setFilters(fa);
        
        syncAnswerShown();
		

        /**
         * after the user answered, we save the answer
         * and change the color to the widget that 
         * represents the question
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
						
						final FormIndex index = DecimalWidget.this.getPrompt().getIndex();
						
						int saveStatus = ((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
						switch (saveStatus) {
						//to assign the right color to the widget that represent the question
						case FormEntryController.ANSWER_OK:
							assignStandardColors();
							if(mReadOnly)
							{
								break;
							}
							break;
						case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
							if((mAnswer.getText().toString()).equals("")){
								assignMandatoryColors();
							}else {
								assignStandardColors();
								break;
							}
							//costanti violate
						case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
							/*
							if(mReadOnly)
							{
								changeColor();
								//break;
							}
							*/
							QuestionAndStringAswerWidget.err = true;
							assignErrorColors();
							break;
							
						default:
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
		 * when the answer Widget loses the focus refresh the current view
		 */
		mAnswer.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				 if(!(hasFocus || ((FormEntryActivity) context).verifica)){
					 
					 
					 //////////////LL has been added a check before to refresh, because the refresh causes some bugs on rosters LL 23-01-14
					 String flagOnCalculated = ConstantUtility.getFlagCalculated();
					 //((FormEntryActivity) context).refreshCurrentView(index);	
					 //mAnswer.setFocusable(true);			//<---------------------------------------12/11/2013
					
					if(flagOnCalculated.equals("no")){//if "no", refreshCurrentView has not been called no where else and you can do 
						((FormEntryActivity) context).refreshCurrentView(index);	
						 	//mAnswer.setFocusable(true);	//<---------------------------------------12/11/2013
					}else{
				 		ConstantUtility.setFlagCalculated("no");
				 	}
				}
				 ((FormEntryActivity) context).verifica = false;
			}
		});
		
		
		
		
		
		

		/*
//        mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mAnswer.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
        mAnswer.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // needed to make long readonly text scroll
        mAnswer.setHorizontallyScrolling(false);
        mAnswer.setSingleLine(false);

        // only allows numbers and no periods
        mAnswer.setKeyListener(new DigitsKeyListener(true, false));

        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        mAnswer.setFilters(fa);
        
        syncAnswerShown();
		*/	
		//---------
    }

	/*
	public void syncAnswerShown() {
		Integer i = null;
        if (mPrompt.getAnswerValue() != null)
        {
        	//variazione del 17/09/2013 per catturare calcoli con Double
        	try	
        	{
        		i = (Integer) mPrompt.getAnswerValue().getValue();
        	}
        	catch (Exception e) 
        	{      		
        		i = Integer.valueOf((int) Math.floor((Double) mPrompt.getAnswerValue().getValue()));
        	}
        	
        	
        	if (i != null) 
        	{
        		//scrivi il valore nel campo
            	mAnswer.setText(i.toString());
        	}
        	assignStandardColors();        	
        	
        	
        	 //VERSIONE CORRETTA E FUNZIONANTE MA INSUFFCIENTE PER MANCATI CONTROLLI
            i = (Integer) mPrompt.getAnswerValue().getValue();

        	if (i != null) {
            	mAnswer.setText(i.toString());
        	}
        }
	}
	*/
	
	/**
	 * set the text of the answer Widget
	 * and set the right color to the question Widget
	 */
	public void syncAnswerShown() {
		Double d = null; Integer i = null; 
        if (mPrompt.getAnswerValue() != null)
        {
        	try 
        	{
        		d = (Double) mPrompt.getAnswerValue().getValue();
               		
        		//formatto la  stringa ragionando sul risultato 
        		//String value = Double.toString(d);
        		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        		((DecimalFormat)nf).applyLocalizedPattern("##0.###");
        		String result = nf.format(d);//new DecimalFormat("##0.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(d);
    
        		mAnswer.setText(result);
        	}catch(Exception e)
        	{
        		i = (Integer) mPrompt.getAnswerValue().getValue();
        		mAnswer.setText(String.valueOf(i));
        	}
        }
        System.out.println("--------------------------------------");
        syncColors();
	}

	/**
	 * get the given answer from the answer widget
	 */
   	@Override
    public IAnswerData getAnswer() {
        String s = mAnswer.getText().toString();
        if (s == null || s.equals("")) {
            if(mPrompt.isRequired()){
                return null;
            }else{
                return new DecimalData(Double.parseDouble("0"));
            }
        } else {
        	try {
                return new DecimalData(Double.parseDouble(s));
            } catch (Exception e) {
            	e.printStackTrace();
                return null;
            }
        }
    }
   	
   	/*
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
				// non rimane altro!
				Log.e("DecimalWidget", "I MUST take focus. But I shouldn't!");
			}

		}
	}
	*/
   	
   	/**
	 * set the answer as blank when remove a QuestionWidget
	 * or a QuestionAndStringAnswerWidget
	 */
   	public IAnswerData setAnswer(IAnswerData a)
	{
		a.setValue("");
		return a;
	}
}
