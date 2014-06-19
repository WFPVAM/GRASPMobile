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
import it.fabaris.wfp.utility.ConstantUtility;
import it.fabaris.wfp.view.ODKView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;

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
						//HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
						String ind = mPrompt.getIndex().toString().split("_")[1].split(",")[0];
						String ind2 = mPrompt.getIndex().toString().replace("_"+ind, "_0");
						
						int num = ind2.length();
						try {
							IAnswerData value = FormEntryActivity.readOnlyInRoster.get(ind2);
							mAnswer.setText(value.getDisplayText().toString());
							
							//*****salva
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
			}
			syncColors();
		}
	}


	public void clearAnswer() {
		mAnswer.getText().replace(0, mAnswer.getText().length(), "", 0, 0);

	}

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
				// non rimane altro!
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
}