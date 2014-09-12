package it.fabaris.wfp.widget;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.utility.ConstantUtility;
import it.fabaris.wfp.view.ODKView;

import java.util.HashMap;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathNumericLiteral;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class that implements the basic properties of all widgets 
 * that have a string as answer.
 */
public abstract class QuestionAndStringAswerWidget extends QuestionWidget implements SyncViewWidget
{

	protected boolean mReadOnly = false;
	protected EditText mAnswer;//represents the prompt for the answer.
	protected Context context;
	protected FormIndex index;
	protected Boolean checking = false;
	private String questionAnswerTmp = null;
	
	public static boolean err = false;

	/**
	 * set the layout of the answer, text, textcolor and background color,
	 * and others
	 * @param context
	 * @param prompt
	 */
	public QuestionAndStringAswerWidget(final Context context, final FormEntryPrompt prompt)
	{
		super(context, prompt);
		this.context = context;
	
		mAnswer = new EditText(context);
		mAnswer.setTextColor(Color.BLACK);
		mAnswer.setBackgroundColor(Color.WHITE);

		mReadOnly = (mPrompt != null && mPrompt.isReadOnly());

		//NOT EDITABLE FIELDS
		if (mReadOnly)
		{
			//setFocusableInTouchMode(false);
			//setFocusable(false);
			//setClickable(false);
			mAnswer.setFocusable(false);
			mAnswer.setEnabled(false);			
			mAnswer.setFocusableInTouchMode(false);
			mAnswer.setClickable(false);	
		}
		
		if(mReadOnly && mPrompt.getDataType() == 3 && !mPrompt.isRequired() && !mPrompt.getFormElement().getBind().getReference().toString().toLowerCase().contains("vis"))
			mAnswer.setBackgroundColor(Color.BLACK);
			
		//method added for the evaluation of the constraints 
		if (!FormEntryActivity.fromHyera) 
		{
			/**
			 * called when an answer is written
			 */
			mAnswer.addTextChangedListener(new TextWatcher() 
			{
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) 
				{
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) 
				{
					
				}

				/**
				 * after an answer has been written then we 
				 * can refresh the layout of the question
				 * and save the answer to the disk
				 */
				@Override
				public void afterTextChanged(Editable s) {
					if (checking == true)
						return;
					else 
					{
						checking = true;
						if (!mAnswer.toString().equalsIgnoreCase("") && !mReadOnly)
						{
								PreferencesActivity.TO_SAVE_FORM = true;
						}
						//else to hide the ready only calculated fields
						else if(!mAnswer.toString().equalsIgnoreCase("") && mReadOnly)
						{						
							String text = mAnswer.getText().toString();
							try
							{	
								int num = Integer.parseInt(text);
								Log.i("QuestionAndStringAnswerWidget", num + " is a number");
								//mAnswer.setVisibility(View.GONE);          					//to hidden the calculated read only fields
								//((FormEntryActivity) context).refreshCurrentView(index);  //refresh just in case of calculated fields
								//------------------------------------------------------------------------------------------------
								PreferencesActivity.TO_SAVE_FORM = true; 
								//------------------------------------------------------------------------------------------------
							}
							catch(NumberFormatException e)
							{
								Log.i("QuestionAndStringAnswerWidget", text + " is not a number");
								//PreferencesActivity.TO_SAVE_FORM = true;    //19/09
							}
						}
						try
						{
							if(((FormEntryActivity) context).mCurrentView instanceof ODKView){
                                HashMap<FormIndex, IAnswerData> answers = ((ODKView) ((FormEntryActivity) context).mCurrentView).getAnswers();
                                index = QuestionAndStringAswerWidget.this.getPrompt().getIndex();

                                int 	saveStatus = ((FormEntryActivity) context).saveAnswer(answers.get(index), index, true);

                                /**
                                 * assigns color to the question
                                 */
                                switch (saveStatus)
                                {
                                    case 0: // risposta OK
										/*
										if (mReadOnly)
										{
											break;
										}
										else
										{
										*/
                                        //SIMPLE FIELD
                                        assignStandardColors();
                                        if (s.length() > 0	&& !((FormEntryActivity) context).verifica)
                                        {
                                            //manage the page change on the fields value
                                            //**************************************************************
                                            //((FormEntryActivity) context).refreshCurrentView(index);
                                            ((FormEntryActivity) context).saveAnswer(answers.get(index), index, false);

                                            //**************************************************************
                                            break;
                                        }
                                        ((FormEntryActivity) context).verifica = false;
										/*
										}
										*/
                                        break;

                                    case 1: 	// Answer required
                                        if ((mAnswer.getText().toString()).equals(""))
                                        {
                                            assignMandatoryColors();
                                            ((FormEntryActivity) context).saveAnswer(answers.get(index), index, false);
                                        }
                                        else
                                        {
                                            assignStandardColors();
                                        }
                                        break;

                                    case 2: 	// Constraint violated
										/*
										if(mReadOnly)
										{
											changeColor();
											//break;
										}
										*/
                                        assignErrorColors();
                                        break;

                                    default:
                                        break;
                                }
                            }else{
                                return;
                            }
						}catch (Exception e)
						{
							e.printStackTrace();
							return;
						}
						finally 
						{
							checking = false;
						}
					}
				}
			});
				
			//-----------------------------------------------------------------------
			TextView.OnEditorActionListener exampleListener = new TextView.OnEditorActionListener()
			{

				public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) 
				{
					if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN ) 
					{ 
						try
						{
							//mAnswer.requestFocus(View.FOCUS_FORWARD);
							//Toast.makeText(getContext(),	mAnswer.getText(), Toast.LENGTH_LONG).show();
							mAnswer.clearFocus();
							mAnswer.setSelection(View.FOCUS_FORWARD);
						}
						catch(Exception e)
						{
							return false;
						}
						return true;
					}
					return false;
				}
			};
			
			//-----------------------------------------------------------------------
			
			//-----------------------------------------------------------------------	
			mAnswer.setOnKeyListener(new OnKeyListener()
			{
				public boolean onKey(View v, int keyCode, KeyEvent event) 
				{
					// If the event is a key-down event on the "enter" button
					if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
					{
						// Perform action on Enter key press
						//mAnswer.clearFocus();
						//mAnswer.requestFocus();
						//mAnswer.moveCursorToVisibleOffset();
						try
						{
							//mAnswer.requestFocus(View.FOCUS_FORWARD);
							//Toast.makeText(getContext(),	mAnswer.getText(), Toast.LENGTH_LONG).show();
							mAnswer.clearFocus();
							mAnswer.setSelection(View.FOCUS_FORWARD);
						}
						catch(Exception e)
						{
							return false;
						}
						return true;
					}
					mAnswer.requestFocus();
					mAnswer.setFocusable(true);
					mAnswer.setFocusableInTouchMode(true);
					//mAnswer.setSelection(mAnswer.getText().length());
					return false;
				}
			});
			//-------------------------------------------------------------------------

				
			
		}
		FormEntryActivity.fromHyera = false;
		mAnswer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		params.setMargins(7, 5, 7, 5);
		mAnswer.setLayoutParams(params);
		mAnswer.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		// capitalize the first letter of the sentence
		mAnswer.setKeyListener(new TextKeyListener(Capitalize.NONE, false));
		mAnswer.setImeOptions(EditorInfo.IME_ACTION_NEXT
				| EditorInfo.IME_FLAG_NO_ENTER_ACTION
				& EditorInfo.IME_MASK_ACTION);
		// needed to make long read only text scroll
		mAnswer.setHorizontallyScrolling(false);
		
		//-----------------------------
		//ONLY ONE ROW FOR THE TEXT
		//mAnswer.setSingleLine(true);
		//-----------------------------

		mAnswer.setSingleLine(false);
		addView(mAnswer);
		syncAnswerShown();

		// TODO: NL check if needed: i'd prefer to get it from preferences.
		// if (prompt.getAnswerText() == null && !mReadOnly &&
		// prompt.isRequired()) {
		// mAnswer.setBackgroundColor(getResources().getColor(R.color.yellow));
		// }

		//addView(mAnswer);   //03/10/2013
		
	}

	public void syncColors() {
		if (mPrompt.isRequired() && "".equals(mAnswer.getText().toString()) ) {
			assignMandatoryColors();
		}
		/*
		else if (mPrompt.isReadOnly() && "".equals(mAnswer.getText().toString()) ) {
			assignMandatoryColors();
		}
		*/
		else if(err)  // modified by Mureed to view the red fields in decimal errors
		{
			err = false;
			assignErrorColors();
		}else
		{
			assignStandardColors();
		}
	}
	
	public void syncReadOnlyColors() 
	{
		if (mPrompt.isRequired() && "".equals(mAnswer.getText().toString()) ) {
			assignMandatoryColors();
		} else	{
			assignStandardColors();
		}
	}

	public abstract void syncAnswerShown();

}

