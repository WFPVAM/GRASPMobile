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
import java.util.Set;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;

/**
 * Widget that restricts values to integers.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Fabaris Srl: Leonardo Luciani
 * 	www.fabaris.it
 */
public class IntegerWidget extends StringWidget {

    public IntegerWidget(final Context context, FormEntryPrompt prompt) {
        super(context, prompt);
        
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
							
							final FormIndex index = IntegerWidget.this.getPrompt().getIndex();
							
							int saveStatus = ((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
							switch (saveStatus) {
							case 0:
								assignStandardColors();
								if(mReadOnly)
								{
									break;
								}
								mAnswer.setOnFocusChangeListener(new OnFocusChangeListener() {
									@Override
									public void onFocusChange(View v, boolean hasFocus) {
										 if(!(hasFocus || ((FormEntryActivity) context).verifica)){
											((FormEntryActivity) context).refreshCurrentView(index);
											 mAnswer.setFocusable(true);
										 }
										 ((FormEntryActivity) context).verifica = false;
									}
								});
								break;
							case 1:	
								if((mAnswer.getText().toString()).equals("")){
									assignMandatoryColors();
								}else {
									assignStandardColors();
									break;
								}
								//costanti violate
							case 2:
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
    }

	public void syncAnswerShown() {
		Integer i = null;
        if (mPrompt.getAnswerValue() != null)
        	i = (Integer) mPrompt.getAnswerValue().getValue();

        if (i != null) {
        	mAnswer.setText(i.toString());
        }
        
        syncColors();
	}


    @Override
    public IAnswerData getAnswer() {
        String s = mAnswer.getText().toString();
        if (s == null || s.equals("")) {
            return null;
        } else {
            try {
                return new IntegerData(Integer.parseInt(s));
            } catch (Exception NumberFormatException) {
                return null;
            }
        }
    }

}
