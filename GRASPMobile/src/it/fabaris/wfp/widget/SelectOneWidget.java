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
import it.fabaris.wfp.view.MediaLayout;
import it.fabaris.wfp.view.ODKView;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import it.fabaris.wfp.activities.*;

import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Fabaris Srl: Leonardo Luciani www.fabaris.it
 */
public class SelectOneWidget extends QuestionWidget implements
		OnCheckedChangeListener {

	private static final int RANDOM_BUTTON_ID = 4853487;
	Vector<SelectChoice> mItems;
	Vector<RadioButton> buttons;
	Vector<MediaLayout> layout;
	Context context;
	String s = null;
	FormEntryPrompt prompt;
	Boolean sema=false;
	
	public boolean selected = false;
	
	public String in;					//30/09/2013
	private LinkedHashMap<FormIndex, IAnswerData> answers;
	private FormIndex index;
	
	//private final RadioButton[] buttons;

	public SelectOneWidget(final Context context, FormEntryPrompt prompt) {
		super(context, prompt);
		this.context = context;
		this.mPrompt = prompt;

		mItems = prompt.getSelectChoices();
		buttons = new Vector<RadioButton>();
		layout = new Vector<MediaLayout>();

		int j = prompt.getFormElement().getID();
		s = null;
		
		//**************************************************
		/*
		//creo il gruppo di radio button
		int dim = 0;
        if ( mPrompt.getSelectChoices() != null ) {
        	dim = mPrompt.getSelectChoices().size();
        }
        if(dim == 0) {
        	buttons = null; 
        } 
        else {
        	buttons = new RadioButton[dim];
        }		
        */
		//*********************************************
		
		//Selection itemSel = new Selection(mPrompt.getAnswerValue().getValue().toString());
	
		if (mPrompt.getAnswerValue() != null)
		{
			/*
			String[] array = mPrompt.getAnswerValue().getDisplayText().split("=>");
			s = array[0].trim();                                         //STRINGHE
			*/
			/*
			int size = mPrompt.getSelectChoices().size();
			for(int i = 0; i < size; i++)
			{
				if(mPrompt.getAnswerValue().getDisplayText().toString().equals(mPrompt.getSelectChoices().get(i).getValue()))
				{
					s = ((String) prompt.getAnswerValue().getValue());
					new SelectOneData(new Selection(s));
					
				}
			}
			*/
			
			s = ((Selection) prompt.getAnswerValue().getValue()).getValue();             //old
			//s = prompt.getAnswerValue().uncast().getString();
		}
		makeLabelColored(prompt, s);
		if (prompt.isRequired()) {
			if(QuestionWidget.colorTheLabel.containsKey(this.getPrompt().getFormElement().getBind().getReference().toString())){
				assignStandardColors();
			}else{
				assignMandatoryColors();
			}
		} else if (prompt.isRequired() && prompt.getAnswerValue() != null){
			assignStandardColors();
		}		
	}

	public void makeLabelColored(FormEntryPrompt prompt, String s) {
		
		if (prompt.getSelectChoices() != null) 
		{
			//---------------------------------
			String select = prompt.getSelectChoices().get(0).getValue();
			if(!(select.equals("0")))
			{
				System.out.println("Il valore di Select è " + select.toString());
				selected = true;
				s = prompt.getSelectChoices().get(0).getValue();
			}
			//--------------------------------
			
			for (int i = 1; i < mItems.size(); i++) 
			{
				RadioButton r = new RadioButton(getContext());
				r.setOnCheckedChangeListener(this);
				String data = prompt.getSelectChoiceText(mItems.get(i));
				r.setText(prompt.getSelectChoiceText(mItems.get(i)));
				r.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);

				r.setId(i + RANDOM_BUTTON_ID);
				r.setEnabled(!prompt.isReadOnly());
				r.setFocusable(!prompt.isReadOnly());
				
				buttons.add(r);
				
				int status = 0;
				if(s != null)
				{
					String[] array = s.split("=>");
					if (mItems.get(i).getValue().equals(array[0].toString().trim()))
					{
						r.setChecked(true);
						
						status = ((FormEntryActivity) context).saveAnswer(mPrompt.getAnswerValue(), SelectOneWidget.this.getPrompt().getIndex(), true);
						IAnswerData d = FormEntryActivity.mFormController.getQuestionPrompt(SelectOneWidget.this.getPrompt().getIndex()).getAnswerValue();
						//ODKView.getMap().put(SelectOneWidget.this.getPrompt().getIndex(), d);
						//index = SelectOneWidget.this.getPrompt().getIndex();
					}
				}
				String audioURI = null;
				audioURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
						FormEntryCaption.TEXT_FORM_AUDIO);

				String imageURI = null;
				imageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
						FormEntryCaption.TEXT_FORM_IMAGE);

				String videoURI = null;
				videoURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
						"video");

				String bigImageURI = null;
				bigImageURI = prompt.getSpecialFormSelectChoiceText(
						mItems.get(i), "big-image");

				MediaLayout mediaLayout = new MediaLayout(getContext());
				mediaLayout
						.setAVT(r, audioURI, imageURI, videoURI, bigImageURI);
				addView(mediaLayout);
				layout.add(mediaLayout);

				// Last, add the dividing line (except for the last element)
				ImageView divider = new ImageView(getContext());
				divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
				if (i != mItems.size()) {
					mediaLayout.addDivider(divider);
				}
			}
		}
	}

	@Override
	public void clearAnswer() {
		for (RadioButton button : this.buttons) {
			if (button.isChecked()) {
				button.setChecked(false);
				return;
			}
		}
	}

	public IAnswerData getAnswer(){
		int i = getCheckedId();	
		IAnswerData result = null;
		if(i != -1) 
		{                                                                             //SELECTION  //old corretto
			SelectChoice sc = mPrompt.getSelectChoices().elementAt(i - RANDOM_BUTTON_ID);
			Selection s = sc.selection();
			//return new SelectOneData(s);  
			result = new SelectOneData(new Selection(sc));

			
			/*
			//STRINGHE
			SelectChoice sc = mItems.elementAt(i - RANDOM_BUTTON_ID);
			return new StringData(sc.toString().split("=>")[0].trim());   ///STRINGA 
			*/
		}else{
			result = null; //devo forzare il comportamento altrimenti mi restituisce un valore non valido
		}
		return result;
	}

	@Override
	public void setFocus(Context context) {
		for (RadioButton button : this.buttons) {
			if (button.isChecked()) {
				button.setFocusableInTouchMode(true);
				button.requestFocus();
			}
		}
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
		// FormEntryActivity.radioFirstCheck = true;
	}

	public int getCheckedId() {
		
		for (RadioButton button : this.buttons) 
		{
			if(button.getText().equals(in))						//29/11/2013   corretto
				return button.getId();
		}
		return -1;
		
		
		
		/*
		if ( buttons != null ) {
	        for ( int i = 1 ; i < buttons.length ; ++i ) {
	          RadioButton b = buttons[i];
	          if (b.isChecked()) {
	        	  return b.getId();
	          }
	        }
		}
		return -1;
		*/
	}
	

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			QuestionWidget.colorTheLabel.put(this.getPrompt().getFormElement().getBind().getReference().toString(),Boolean.TRUE);
			if (sema)
				return;
			sema=true;
			if (!FormEntryActivity.fromHyera) 
			{
				in = (String) buttonView.getText();
				System.out.println("LA SELEZIONE E' " + in.toString());
	
				FormEntryPrompt o = SelectOneWidget.this.getPrompt();
				System.out.println("L'oggetto ha come indici " + o.getIndex());
				
				try
				{
					answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
					index = SelectOneWidget.this.getPrompt().getIndex();
					
					for (RadioButton button : this.buttons)
					{
						if ( !(button.getText().equals(answers.get(index).getDisplayText())) ) {
							button.setChecked(false);
						}
						else if(button.getText().equals(answers.get(index).getDisplayText()))
						{
							button.setChecked(true);
							if (button.isChecked() && (buttonView == button)) 
							{
								button.setChecked(true);
													
								((FormEntryActivity) context).saveAnswer(new SelectOneData(new Selection(mPrompt.getSelectChoices().elementAt(getCheckedId() - RANDOM_BUTTON_ID))), index, true);               
								//((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
								
								((FormEntryActivity) context).refreshCurrentView(index);															
							}
						}
					}
					if (isChecked && answers != null)
					{
						//button.setChecked(true);
						FormEntryActivity.radioFirstCheck = false;	
						
						((FormEntryActivity) context).saveAnswer(new SelectOneData(new Selection(mPrompt.getSelectChoices().elementAt(getCheckedId() - RANDOM_BUTTON_ID))), index, true);    
						//((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
						
						assignStandardColors();
						((FormEntryActivity) context).refreshCurrentView(index);	            
					
					}
					FormEntryActivity.fromHyera = false;
					((FormEntryActivity) context).refreshCurrentView(index);	
					assignStandardColors();			
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					sema=false;
					//answers = null;
					//index = null;
				}
	
				
				/*   //----------------------------------------------------- ORIGINALE ---------------------------------------------------
				try
				{
					HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
					
					FormIndex index = SelectOneWidget.this.getPrompt().getIndex();
					((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
					
					if (isChecked && FormEntryActivity.radioFirstCheck) 
					{
						FormEntryActivity.radioFirstCheck = false;
						answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
						index = SelectOneWidget.this.getPrompt().getIndex();
						((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
						assignStandardColors();
						((FormEntryActivity) context).refreshCurrentView(index);
						
						//((FormEntryActivity) context).verifica = false;
						// TODO: tolto per provare
						((FormEntryActivity) context).verifica = true;
					}
					//if(mPrompt.isRequired())
					//	assignMandatoryColors();
					else if(isChecked && !FormEntryActivity.radioFirstCheck)  //if(isChecked) //RadioButton già selezionato
					{
						assignStandardColors();
						((FormEntryActivity) context).verifica = true;
					}
					
					for (RadioButton button : this.buttons) 
					{
						if (button.isChecked() && !(buttonView == button)) 
						{
							button.setChecked(false);
							answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
							index = SelectOneWidget.this.getPrompt().getIndex();
							((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
							assignStandardColors();
							((FormEntryActivity) context).refreshCurrentView(index);
							((FormEntryActivity) context).verifica = true;
						}
					}
					
					//buttonView.setChecked(isChecked);
					//assignStandardColors();
					
					buttonView.setChecked(isChecked);
					FormEntryActivity.fromHyera = false;
					assignStandardColors();
				} catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					sema=false;
				}
				*/
				
			}
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		for (RadioButton r : buttons) {
			r.setOnLongClickListener(l);
		}
	}

	@Override
	public void cancelLongPress() {
		/*
		super.cancelLongPress();
		for (RadioButton button : this.buttons) {
			button.cancelLongPress();
		}
		*/
	}

	@Override
	public IAnswerData setAnswer(IAnswerData a) {
		// TODO Auto-generated method stub
		return null;
	}
}