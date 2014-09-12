package it.fabaris.wfp.widget;

import java.util.HashMap;
import java.util.Map;

import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.utility.ColorHelper;
import it.fabaris.wfp.view.MediaLayout;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.graphics.Color;

/**
 * 
 * Class that implements the basic properties of all widgets.
 * It declare and initialize the TextView Android Widget
 * that represents the question to answer called mQuestionText
 * and append the question to the layout.
 * The text of the question is taken from the FormEntryPrompt object.
 */
public abstract class QuestionWidget extends LinearLayout 
{
	@SuppressWarnings("unused")
	private final static String t = "QuestionWidget";
	private LinearLayout.LayoutParams mLayout;
	protected FormEntryPrompt mPrompt;
	protected final int mQuestionFontsize;
	protected final int mAnswerFontsize;
	static protected ColorHelper colorHelper;
	static protected Map<String,Boolean> colorTheLabel = new HashMap<String, Boolean>();
	public TextView mQuestionText;//represents the question to answer
	public TextView mHelpText;
	private int formId = -1;
	//private String formId = null;
	
	public int getFormId()
	{
		return formId;
	}
	public void setFormId(int formId)
	{
		this.formId = formId;
	}

	/**
	 * Create the LinerLayout that will contain the question
	 * and its relative answer
	 * @param context
	 * @param p  p is a FormEntryPrompt object and has the FormIndex, which is where the answer
	 *			 gets stored
	 */
	public QuestionWidget(Context context, FormEntryPrompt p) 
	{
		super(context);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		if (colorHelper == null) 
		{
			colorHelper = new ColorHelper(context, getResources());
		}
		String question_font = settings.getString(PreferencesActivity.KEY_FONT_SIZE, Collect.DEFAULT_FONTSIZE);
		mQuestionFontsize = Integer.valueOf(question_font).intValue();
		mAnswerFontsize = mQuestionFontsize + 2;

		mPrompt = p;

		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.TOP);
		setPadding(0, 7, 0, 0);

		mLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLayout.setMargins(10, 0, 10, 0);

		addQuestionText(p);
		addHelpText(p);
		
	}

	public FormEntryPrompt getPrompt() 
	{
		return mPrompt;
	}

	/**
	 * 
	 *to get the given answer from the widget
	 */
	// Abstract methods
	public abstract IAnswerData getAnswer();

	/**
	 *to clean the widget from the given answer 
	 */
	public abstract void clearAnswer();
	
	public abstract IAnswerData setAnswer(IAnswerData a);

	public abstract void setFocus(Context context);

	public abstract void setOnLongClickListener(OnLongClickListener l);	

	
	/**
	 * Add a View containing the question text, audio (if applicable), and
	 * image (if applicable). To satisfy the RelativeLayout constraints, we add
	 * the audio first if it exists, then the TextView to fit the rest of the
	 * space, then the image if applicable.
	 * We set the text of the question.
	 */
	protected void addQuestionText(FormEntryPrompt p) {
		String imageURI = p.getImageText();
		String audioURI = p.getAudioText();
		String videoURI = p.getSpecialFormQuestionText("video");

		// shown when image is clicked
		String bigImageURI = p.getSpecialFormQuestionText("big-image");

		// Add the text view. Textview always exists, regardless of whether
		// there's text.
		mQuestionText = new TextView(getContext());
		mQuestionText.setText(p.getLongText());//get the text from the FormEntryPrompt object
		mQuestionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize);
		mQuestionText.setTypeface(null, Typeface.BOLD);
		mQuestionText.setPadding(0, 0, 0, 7);
		assignStandardColors();

		mQuestionText.setId(38475483); // assign random id

		// Wrap to the size of the parent view
		mQuestionText.setHorizontallyScrolling(false);

		if (p.getLongText() == null) 
		{
			mQuestionText.setVisibility(GONE);
		}
		
		// Create the layout for audio, image, text
		MediaLayout mediaLayout = new MediaLayout(getContext());
		mediaLayout.setAVT(mQuestionText, audioURI, imageURI, videoURI, bigImageURI);

		addView(mediaLayout, mLayout);
	}
	
	/**
	 * assign the right layout colors to the TextView Android widget 
	 * that represents the question to answer called mQuestionText
	 * In case of: question not yet answered.
	 */
	protected void assignStandardColors() 
	{
		if (this.mPrompt.isReadOnly() && this.mPrompt.getDataType() == 3 && !this.mPrompt.isRequired() && !mPrompt.getFormElement().getBind().getReference().toString().toLowerCase().contains("vis"))
		{	
			mQuestionText.setTextColor(colorHelper.getReadOnlyForegroundInvisible());
			mQuestionText.setBackgroundColor(colorHelper.getReadOnlyBackgroundInvisible());
		}
		else if (this.mPrompt.isReadOnly())
		{	
			mQuestionText.setBackgroundColor(colorHelper.getReadOnlyBackgroundColor());
			mQuestionText.setTextColor(colorHelper.getReadOnlyForeColor());
		}
		else 
		{
			mQuestionText.setBackgroundColor(colorHelper.getDefaultBackgroundColor());
			mQuestionText.setTextColor(colorHelper.getDefaultForeColor());
		}	
	}

	/**
	 * assign the right layout colors to the TextView Android widget 
	 * that represents the question to answer called mQuestionText
	 * In case of: valid answer.
	 */
	protected void assignMandatoryColors() 
	{
		if (this.mPrompt.isReadOnly())
		{
			mQuestionText.setBackgroundColor(colorHelper.getMandatoryBackgroundColor());
			mQuestionText.setTextColor(colorHelper.getMandatoryBackgroundColor());
		} 
		else if(this.mPrompt != null)
		{
			mQuestionText.setBackgroundColor(colorHelper.getMandatoryBackgroundColor());
			mQuestionText.setTextColor(colorHelper.getMandatoryForeColor());
		}
	}

	/**
	 * assign the right layout colors to the TextView Android widget 
	 * that represents the question to answer called "mQuestionText".
	 * In case of: not valid answer
	 */
	protected void assignErrorColors()
	{
		if (this.mPrompt.isReadOnly())
		{
			mQuestionText.setBackgroundColor(colorHelper.getErrorReadOnlyBackgroundColor());
			mQuestionText.setTextColor(colorHelper.getErrorReadOnlyForeColor());
		}  
		else if(this.mPrompt != null)
		{
			mQuestionText.setBackgroundColor(colorHelper.getErrorBackgroundColor());
			mQuestionText.setTextColor(colorHelper.getErrorForeColor());
		}
	}
	
	/**
	 * assign the color for a question not yet answered for RadioButton
	 */
	protected void assignMandatoryColors(RadioButton onThis) 
	{
		onThis.setBackgroundColor(colorHelper.getMandatoryBackgroundColor());
		onThis.setTextColor(colorHelper.getMandatoryForeColor());
	}

	/**
	 * assign the color for a question correctly answered for RadioButton
	 */
	protected void assignStandardColors(RadioButton onThis)
	{
		onThis.setBackgroundColor(colorHelper.getDefaultBackgroundColor());
		onThis.setTextColor(colorHelper.getDefaultForeColor());
	}

	/**
	 * assign the color for a question not correctly answered for RadioButton
	 */
	protected void assignErrorColors(RadioButton onThis)
	{
		onThis.setBackgroundColor(colorHelper.getErrorBackgroundColor());
		onThis.setTextColor(colorHelper.getErrorForeColor());
	}

	/**
	 * assign the color for a question not yet answered for CheckBox
	 */
	protected void assignStandardColors(CheckBox onThis)
	{
		onThis.setBackgroundColor(colorHelper.getDefaultBackgroundColor());
		onThis.setTextColor(colorHelper.getDefaultForeColor());
	}

	/**
	 * assign the color for a question correctly answered for CheckBox
	 */
	protected void assignMandatoryColors(CheckBox onThis) 
	{
		onThis.setBackgroundColor(colorHelper.getMandatoryBackgroundColor());
		onThis.setTextColor(colorHelper.getMandatoryForeColor());
	}
	
	/**
	 * not used
	 */
	public void changeColor()
	{
		//mQuestionText.setBackgroundColor(colorHelper.getErrorReadOnlyBackgroundColor());
		//mQuestionText.setTextColor(R.color.fg_readonly_err);
		//mQuestionText.setVisibility(View.GONE);
		mQuestionText.invalidate();
		mQuestionText.setBackgroundColor(R.color.fg_readonly_err);
		mQuestionText.setTextColor(0xFFFFA500); 
	}

	/**
	 * Add a TextView containing the help text.
	 */
	private void addHelpText(FormEntryPrompt p) {

		String s = p.getHelpText();

		if (s != null && !s.equals(""))
		{
			mHelpText = new TextView(getContext());
			mHelpText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mQuestionFontsize - 3);
			mHelpText.setPadding(0, -5, 0, 7);
			// wrap to the widget of view
			mHelpText.setHorizontallyScrolling(false);
			mHelpText.setText(s);
			mHelpText.setTypeface(null, Typeface.ITALIC);

			addView(mHelpText, mLayout);
		}
	}

	/**
	 * Every subclassed widget should override this, adding any views they may
	 * contain, and calling super.cancelLongPress()
	 */
	public void cancelLongPress() {
		super.cancelLongPress();
		if (mQuestionText != null) {
			mQuestionText.cancelLongPress();
		}
		if (mHelpText != null) {
			mHelpText.cancelLongPress();
		}
	}
	
	public static void clearColorLabelStoredForRequiredCheckBox(){
		if(!QuestionWidget.colorTheLabel.isEmpty())//if there are references to a given checkbox
			QuestionWidget.colorTheLabel.clear();
	}
	
	
	public void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {		
	}
}
