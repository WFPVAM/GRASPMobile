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
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.utility.ConstantUtility;
import it.fabaris.wfp.view.ODKView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;

import content.BrainsAdapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.SlidingDrawer;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * SpinnerWidget handles select-one fields. Instead of a list of buttons it uses
 * a spinner, wherein the user clicks a button and the choices pop up in a
 * dialogue box. The goal is to be more compact. If images, audio, or video are
 * specified in the select answers they are ignored.
 * 
 * @author Jeff Beorse (jeff@beorse.net)
 * @author Fabaris Srl: Leonardo Luciani www.fabaris.it
 */
public class SpinnerWidget extends QuestionWidget implements OnItemSelectedListener 
{
	Vector<SelectChoice> mItems;
	Spinner spinner;
	String[] choices;
	Context context;
	boolean verspinner = false;
	
	public boolean selected = false;
	public String s = null;

	public SpinnerWidget(final Context context, FormEntryPrompt prompt) {
		super(context, prompt);

		mItems = prompt.getSelectChoices();
		spinner = new Spinner(context);
		choices = new String[mItems.size()];
		this.context = context;
		
		//---------------------------------
		String select = prompt.getSelectChoices().get(0).getValue();
		if(!(select.equals("0")))
		{
			selected = true;
			s = prompt.getSelectChoices().get(0).getValue();
		}
		//--------------------------------
		// ---  14/10/2013 ------
		else if(select.equals("0"))
		{
			selected = false;
		}
		//-----------------------

		for (int i = 0; i < mItems.size(); i++)
		{
			choices[i] = prompt.getSelectChoiceText(mItems.get(i));
		}
		// The spinner requires a custom adapter. It is defined below
		final SpinnerAdapter adapter = new SpinnerAdapter(getContext(),
				android.R.layout.simple_spinner_item, choices,
				TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);
		
	
		spinner.setAdapter(adapter);
		spinner.setPrompt(prompt.getQuestionText());
		//spinner.setEnabled(!prompt.isReadOnly());
		//spinner.setFocusable(!prompt.isReadOnly());
		// spinner.setFocusableInTouchMode(true);
		
		spinner.setOnItemSelectedListener(this);   //commentato onitem
		
		
		//***************************************************   08/11/2013
		/*
		if (!FormEntryActivity.fromHyera) {
			try {
				spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View arg1, int position, long arg3) {
						if (verspinner == false) {
							verspinner = true;
							return;
						} else {
							//((FormEntryActivity) context).verifica = false;
							// TODO: tolto per provare
							((FormEntryActivity) context).verifica = true;
							HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView)
									.getAnswers();
							FormIndex index = SpinnerWidget.this.getPrompt()
									.getIndex();
							((FormEntryActivity) context).saveAnswer(
									answers.get(index), index, true);
							((FormEntryActivity) context)
									.refreshCurrentView(index);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}
				});

				FormEntryActivity.fromHyera = false;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
		//********************************************************************************************************
		
		// Fill in previous answer from javarosa saved 
		if (mPrompt.getAnswerValue() != null) 
		{
			s = ((Selection) mPrompt.getAnswerValue().getValue()).getValue();
			if (s != null)
			{			
				for (int i = 0; i < mItems.size(); ++i) {
					String sMatch = mItems.get(i).getValue();
					
					//-------------------				
					if (sMatch.equals(s)) {
						spinner.setSelection(i);
					}
				}
			}
		}
		
		
		/*
		 *    28/11/2019
		else if(prompt.getAnswerValue() == null)
		{
			LinkedHashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
			FormIndex ind = prompt.getIndex();
			IAnswerData data = answers.get(ind);
			if(data!=null)
			{
				s = data.toString();
				for (int i = 0; i < mItems.size(); ++i) {
					String sMatch = mItems.get(i).getValue();
					
					//-------------------				
					if (sMatch.equals(s)) {
						spinner.setSelection(i);
					}
				}
			}
		}
		*/
		
		addView(spinner);
		spinner.getSelectedItem().toString();
	}

	@Override
	public IAnswerData getAnswer() {
		final int i = spinner.getSelectedItemPosition();
		IAnswerData result = null;
		if (i == -1) {
			result = null;
		} else 
		{
			SelectChoice sc = mItems.elementAt(i); // - RANDOM_BUTTON_ID);
			if ((mItems.elementAt(i).toString()).equalsIgnoreCase("Select => 0")) {
				result = null;
			}else{
				result = new SelectOneData(new Selection(sc));
			}
		}
		
		return result;
	}

	@Override
	public void clearAnswer() {
		// It seems that spinners cannot return a null answer. This resets the
		// answer
		// to its original value, but it is not null.
		spinner.setSelection(0);
	}

	@Override
	public void setFocus(Context context) {
		spinner.setFocusableInTouchMode(true);
		spinner.requestFocus();
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	private class SpinnerAdapter extends ArrayAdapter<String> {
		Context context;
		String[] items = new String[] {};
		int textUnit;
		float textSize;

		public SpinnerAdapter(final Context context,
				final int textViewResourceId, final String[] objects,
				int textUnit, float textSize) {
			super(context, textViewResourceId, objects);
			this.items = objects;
			this.context = context;
			this.textUnit = textUnit;
			this.textSize = textSize;
		}

		@Override
		// Defines the text view parameters for the drop down list entries
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.custom_spinner_item,
						parent, false);
			}
			TextView tv = (TextView) convertView
					.findViewById(android.R.id.text1);
			/*
			if(selected == false)
			{
				tv.setTypeface(null, Typeface.BOLD_ITALIC);
				tv.setText(items[0]);
			}
			*/
			//tv.setTypeface(null, Typeface.NORMAL);
			if (items[position].toString().equalsIgnoreCase("Select"))
				tv.setTypeface(null, Typeface.ITALIC);
			
			tv.setText(items[position]);
			//tv.setTextSize(textUnit, textSize);
			tv.setPadding(10, 10, 10, 10); // Are these values OK?
			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(
						android.R.layout.simple_spinner_item, parent, false);
			}
			TextView tv = (TextView) convertView
					.findViewById(android.R.id.text1);
			tv.setText(items[position]);
			if (items[position].toString().equalsIgnoreCase("Select")
					&& getPrompt().isRequired()) {
				assignMandatoryColors();
				tv.setTypeface(null, Typeface.BOLD_ITALIC);
			} else {
				assignStandardColors();
			}
			tv.setTextSize(textUnit, textSize);

			convertView.setFocusableInTouchMode(true);
			return convertView;
		}

	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		spinner.setOnLongClickListener(l);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		spinner.cancelLongPress();
	}

	/**
	 * set the answer as blank when remove a QuestionWidget
	 */
	@Override
	public IAnswerData setAnswer(IAnswerData a) {
		return null;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) 
	{
		
			int k = 0;
			FormIndex index = null;
			if (!FormEntryActivity.fromHyera) 
			{
				//((FormEntryActivity) context).verifica = false;
					((FormEntryActivity) context).verifica = true;
					LinkedHashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView)
							.getAnswers();
					index = SpinnerWidget.this.getPrompt().getIndex();
					IAnswerData currentAnswer = answers.get(index);
					k = ((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
											
					try {
						if(currentAnswer!=null){
							Thread.sleep(1000);
						}
						else{
							Thread.sleep(200);
						}
						if(k==0){
							ConstantUtility.setFlagCalculated("si");
						}
						//((FormEntryActivity) context).refreshCurrentView(index);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
					}
					
			}
			FormEntryActivity.fromHyera = false;
			if(k==0){
				((FormEntryActivity) context).refreshCurrentView(index);
			}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}