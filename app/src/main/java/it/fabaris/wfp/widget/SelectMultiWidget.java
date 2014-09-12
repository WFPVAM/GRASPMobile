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
import it.fabaris.wfp.view.MediaLayout;
import it.fabaris.wfp.view.ODKView;

import java.util.HashMap;
import java.util.Vector;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Fabaris Srl: Leonardo Luciani
 * 	www.fabaris.it
 */
public class SelectMultiWidget extends QuestionWidget{
    private final static int CHECKBOX_ID = 100;
    boolean mCheckboxInit= false ;
    Vector<SelectChoice> mItems;
    CheckBox c;

    private Vector<CheckBox> mCheckboxes;

    @SuppressWarnings("unchecked")
    public SelectMultiWidget(final Context context, final FormEntryPrompt prompt) {
        super(context, prompt);
        c = new CheckBox(getContext());
        mCheckboxes = new Vector<CheckBox>();
        mItems = prompt.getSelectChoices();
        setOrientation(LinearLayout.VERTICAL);
        Vector<Selection> ve = new Vector<Selection>();
        if (prompt.getAnswerValue() != null) {
        	ve = (Vector<Selection>) prompt.getAnswerValue().getValue();
        }
        
        if (mItems != null) {
            for (int i = 0; i < mItems.size(); i++) {
                // no checkbox group so id by answer + offset
                // when clicked, check for readonly before toggling
                c.setId(CHECKBOX_ID + i);
                c.setText(prompt.getSelectChoiceText(mItems.get(i)));
                c.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
                c.setFocusable(!prompt.isReadOnly());
                c.setEnabled(!prompt.isReadOnly());
                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (mItems.get(i).getValue().equals(ve.elementAt(vi).getValue())) {
                        c.setChecked(true);
                        break;
                    }
                }
                
                mCheckboxes.add(c);

                if(!FormEntryActivity.fromHyera){
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    	//((FormEntryActivity) context).verifica = false;
						// TODO: tolto per provare
						((FormEntryActivity) context).verifica = true;
	    				HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
	                   	FormIndex index = SelectMultiWidget.this.getPrompt().getIndex();   
	                  	((FormEntryActivity) context).saveAnswer(answers.get(index), index, true); 	
	                  	((FormEntryActivity) context).refreshCurrentView(index);	
                    }
                });
                }
                FormEntryActivity.fromHyera=false;
                String audioURI = null;
                audioURI =	prompt.getSpecialFormSelectChoiceText(mItems.get(i),
                        FormEntryCaption.TEXT_FORM_AUDIO);
                String imageURI = null;
                imageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
                        FormEntryCaption.TEXT_FORM_IMAGE);

                String videoURI = null;
                videoURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i), "video");
                String bigImageURI = null;
                bigImageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i), "big-image");

                MediaLayout mediaLayout = new MediaLayout(getContext());
                mediaLayout.setAVT(c, audioURI, imageURI, videoURI, bigImageURI);
                addView(mediaLayout);

                // Last, add the dividing line between elements (except for the last element)
                ImageView divider = new ImageView(getContext());
                divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                if (i != mItems.size() - 1) {
                    addView(divider);
                }
            }
        }
       // mCheckboxInit = false;
    }

    /**
     * reset the value of the answer
     */
	@Override
    public void clearAnswer() {
        int j = mItems.size();
        for (int i = 0; i < j; i++) {
            // no checkbox group so find by id + offset
            c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                c.setChecked(false);
                break;
            }
        }
    }

	/**
	 * get the answer from the answer Widget
	 */
    @Override
    public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
        for (int i = 0; i < mItems.size(); i++) {
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                vc.add(new Selection(mItems.get(i)));
            }
        }

        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }
    }

    /**
     * hide the soft keyboard if it is showing
     */
    @Override
    public void setFocus(Context context) {
    	for (CheckBox check : this.mCheckboxes) {
//   		 if (check.isChecked()){
    		check.setFocusableInTouchMode(true);
   			check.requestFocus();
//   		 }
   	 }
//    	Hide the soft keyboard if it's showing.
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (CheckBox c : mCheckboxes) {
            c.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (CheckBox c : mCheckboxes) {
            c.cancelLongPress();
        }
    }

	@Override
	public IAnswerData setAnswer(IAnswerData a) {
		// TODO Auto-generated method stub
		return null;
	}

}