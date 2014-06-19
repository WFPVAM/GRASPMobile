package it.fabaris.wfp.widget;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.view.ODKView;

import java.util.HashMap;
import java.util.Set;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

public class StringMultiLineWidget extends QuestionWidget 
{
	protected boolean mReadOnly = false;
	protected EditText mAnswer;
	protected Context context;
	
	public StringMultiLineWidget(final Context context, FormEntryPrompt prompt) 
	{
		super(context, prompt);	
		
		/*
		mAnswer.addTextChangedListener(new TextWatcher() 
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
			}
			@Override
			public void afterTextChanged(Editable s) 
			{
//				if(before==count)return;
				try{
					HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
					Set<FormIndex> indexKeys = answers.keySet();
						
						final FormIndex index = StringMultiLineWidget.this.getPrompt().getIndex();
						
						int saveStatus = ((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);
						switch (saveStatus) {
						case 0:
							assignStandardColors();
							if(mReadOnly)
							{
								break;
							}
							//mAnswer.setBackgroundColor(getResources().getColor(R.color.white));
							mAnswer.setOnFocusChangeListener(new OnFocusChangeListener() {
								@Override
								public void onFocusChange(View v, boolean hasFocus) {
									 if(!(hasFocus || ((FormEntryActivity) context).verifica)){
										((FormEntryActivity) context).refreshCurrentView(index);
										 mAnswer.setFocusable(true);									
									 }
									 ((FormEntryActivity) context).verifica = false;	
									 //------------------------------ 16/10/2013
								}
							});
							break;
						case 1:	
							if((mAnswer.getText().toString()).equals("")){
								assignMandatoryColors();
								//mAnswer.setBackgroundColor(getResources().getColor(R.color.red));
							}else {
								assignStandardColors();
								break;
							}
							//costanti violate
						case 2:
							assignErrorColors();
							//mAnswer.setBackgroundColor(getResources().getColor(R.color.red));
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
		*/
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

	@Override
	public void clearAnswer() {
		mAnswer.getText().replace(0, mAnswer.getText().length(), "", 0, 0);
	}

	@Override
	public IAnswerData setAnswer(IAnswerData a) {
		a.setValue("");
		return a;
	}

	@Override
	public void setFocus(Context context) 
	{
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
				Log.e("StringMultiLineWidget", "I MUST take focus. But I shouldn't!");
			}
		}
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
	}
}
