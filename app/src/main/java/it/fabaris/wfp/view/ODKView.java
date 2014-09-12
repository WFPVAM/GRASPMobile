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
package it.fabaris.wfp.view;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.utility.ConstantUtility;
import it.fabaris.wfp.widget.IBinaryWidget;
import it.fabaris.wfp.widget.QuestionAndStringAswerWidget;
import it.fabaris.wfp.widget.QuestionWidget;
import it.fabaris.wfp.widget.WidgetFactory;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * This class is the view that contains all object and widgets in the page
 *
 * @author carlhartung
 * @author Fabaris Srl: Leonardo Luciani www.fabaris.it
 */

//CREA UNA VIEW LINEARLAYOUT E ATTACCA TANTI WIDGET QUANTI NE ARRIVANO DA XML
public class ODKView extends ScrollView //implements OnLongClickListener 
{
    // starter random number for view IDs
    private final static int VIEW_ID = 12345;
    private final static String t = "CLASSNAME";
    private LinearLayout mView;
    private LinearLayout.LayoutParams mLayout;
    private static ArrayList<QuestionWidget> widgets;
    private final static int TEXTSIZE = 21;
    public final static String FIELD_LIST = "field-list";
    FormEntryPrompt formEntryPrompt;
    private static int NEXTID = 1;
    private Map<Integer, Integer> widgetsPos = new HashMap<Integer, Integer>();
    private static LinkedHashMap<FormIndex, IAnswerData> answers = new LinkedHashMap<FormIndex, IAnswerData>();
    private List<QuestionWidget> actualViews;


    boolean first = true;

    boolean isRoster = false;

    public boolean firstBlock = true;

    public HashMap<String, IAnswerData> dataMaps = new HashMap<String, IAnswerData>();

    //*********
    private LinkedHashMap<String, IAnswerData> map;
    //public static boolean ROSTER = false;
    private View divider;
    //*********

    public ODKView(Context context, FormEntryPrompt[] questionPrompts, FormEntryCaption[] groups, FormEntryPrompt formEntryPrompt)
    {
        super(context);
        this.formEntryPrompt = formEntryPrompt;
        widgets = new ArrayList<QuestionWidget>();

        mView = new LinearLayout(getContext());
        //ScrollView scroll = new ScrollView(getContext());

        //********  DIVIDER
        divider = new View(getContext());

        mView.setOrientation(LinearLayout.VERTICAL);
        mView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);

        //mView.setGravity(8388611);
        try {
            //mView.setFocusable(true);
            //mView.requestFocus();
            mView.requestFocus(LinearLayout.FOCUS_UP);
            mView.setPadding(0, 0, 0, 0);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        params.bottomMargin = 1000;
        params.setMargins(10, 0, 10, 1000);


        //CONFIGURAZIONE DELLE CARATTERISTICHE DEL LINEARLAYOUT
        mLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f);

        mLayout.gravity = 8388611;
        mLayout.topMargin = 0;
        mLayout.setMargins(0, 0, 0, 0);

        mView.setLayoutParams(mLayout);

        //********************************************************************
        //mView.setDrawingCacheEnabled( false );                     //12/11/2013
        //mView.setPersistentDrawingCache(ViewGroup.PERSISTENT_NO_CACHE);
        //*********************************************************************


        //AGGIUNGO IL LINEARLAYOUT ALLA SCROLLVIEW
        //scroll.addView(mView);

        // display which group you are in as well as the question
        addGroupText(groups);
        boolean first = true;

        for (FormEntryPrompt p : questionPrompts)
        {
            createAndShowView(p, null, !first);
            if (first)
            {
                first = false;
            }
        }

        //***********************************  RIMOSSO DIVIDER
			/*
			View divider = new View(getContext());
			
			WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
				
			divider.setMinimumHeight(display.getHeight()/2);
			mView.addView(divider);
			*/
        //****************************************************

        addView(mView);

    }

    public void refreshVisibility(FormEntryPrompt[] questionPrompts)
    {

        //****** 21/11/2013
        mView.invalidate();
        mView.setDrawingCacheEnabled( false );

        removeDivider();


        Log.e("RefreshVisibility", "Richiamato refreshVisibility in ODKView");
        System.out.println("refreshVisibility: called with questionPrompts " + (questionPrompts == null ? "null" : String.valueOf(questionPrompts.length)));
        QuestionWidget found = null;
        int position = -1;
        actualViews = new ArrayList<QuestionWidget>();
		
		/*
		for (int i = 0; i < widgets.size(); i++) 
		{
			widgets.get(i).setFocusable(true);
			widgets.get(i).setFocusableInTouchMode(true);	
			if(i == 0)
				widgets.get(i).requestFocus();
		}
		//------------------------------------------------------------------------------
		 * 
		 */
        for (int i = 0; i < widgets.size(); i++)
        {
            actualViews.add(widgets.get(i));
        }
        //-------------------------------------------------------------
        for (int e = 0; e < questionPrompts.length; e++)
        {
            found = null;
            position = -1;
            FormEntryPrompt p = questionPrompts[e];
            for (int i = 0; i < widgets.size(); i++)
            {
                QuestionWidget v = widgets.get(i);
                if (checkIdentity(p, v, e, i))
                {
                    found = v;
                    position = e;

                    break;
                }
            }
            if (found != null)
            {
                showView(p, found, e, position);
                //if(found instanceof QuestionWidget)                     ///14/11/2013
                actualViews.remove(found);
            }
            else if(found == null)
            {
                // cerco il valore gia' presente precedente. Il controllo sara'
                // posizionato subito dopo
                int searched = (e == 0 ? 0 : e - 1);

                createAndShowView(p, searched > -1 ? questionPrompts[searched] : null, true);
            }
        }
        //-------------------------------------------------------------------------------------------
        if(actualViews.size()!=0)
        {
            for (int i = 0; i < actualViews.size(); i++)
            {
                hideView(actualViews.get(i));
            }
        }

        //********  21/11/2013
        addDivider();


    }

    /**
     * Hides the view in this linearlayout
     *
     * @param view
     */
    private void hideView(View v)
    {
        // I prefer to compute also before hide. Something can be child of this
        // value
        if (v instanceof QuestionAndStringAswerWidget)
        {
            //((QuestionAndStringAswerWidget) v).clearAnswer();  //pulisco l'editText   22/10/2013
            removeElement((QuestionAndStringAswerWidget) v);
            clearFocus();
            widgets.remove(v);
            ((QuestionAndStringAswerWidget) v).syncAnswerShown();
        }
        if (v.getVisibility() != View.GONE)
        {
            //clearFocus();
            //((QuestionWidget) v).clearAnswer();
            removeElement((QuestionWidget)v);
            clearFocus();
            widgets.remove(v);
            v.setVisibility(View.GONE);
        }
    }

    /**
     * Creates a new view reading attributes from p, and put it in position
     * promptPosition inside mView
     *
     * @param p
     * @param promptPosition
     * @param separatefromprevious
     */
    private void createAndShowView(FormEntryPrompt p, FormEntryPrompt afterThis, boolean separatefromprevious)
    {
        int promptPosition = -1;
        if (afterThis != null)
        {
            for (int i = 0; i < mView.getChildCount(); i++)
            {
                if ((mView.getChildAt(i) instanceof QuestionWidget)	&& ((QuestionWidget) mView.getChildAt(i)).getFormId() == afterThis.getFormElement().getID())
                //if ((mView.getChildAt(i) instanceof QuestionWidget)	&& ((QuestionWidget) mView.getChildAt(i)).getFormId().equals(afterThis.getIndex().getNextLevel().getReference().getName(1).toString()))
                {
                    promptPosition = i + 1;
                    break;
                }
            }
        }

        // if question or answer type is not supported, use text widget
        QuestionWidget qw = WidgetFactory.createWidgetFromPrompt(p,	getContext());
        //qw.setLongClickable(true);														//**14/11/2013
        //qw.setOnLongClickListener(this);

        //--------------------------------------
        //AGGIUNTO PER DARE IL FOCUS AL PRIMO WIDGET
        if(isRoster == false)
        {
            qw.setFocusable(true);
            qw.setFocusableInTouchMode(true);
            if(first == true)
            {
                first = false;
                qw.requestFocus();
            }
        }
        //--------------------------------------

        qw.setId(VIEW_ID + NEXTID++);

        widgets.add(qw);
        if (promptPosition < 0 || promptPosition >= mView.getChildCount())
        {
            mView.addView(qw, mLayout);
        }
        else
        {
            mView.addView(qw, promptPosition, mLayout);
        }
    }

    /**
     * Make sure that the View v is shown, in the position promptPosition. The
     * viewPosition is the index in which v has been found.
     *
     * @param p
     * @param v
     * @param promptPosition
     * @param viewPosition
     */

    private void showView(FormEntryPrompt p, QuestionWidget v, int promptPosition, int viewPosition)
    {
        if (v instanceof QuestionAndStringAswerWidget)
        {
            ((QuestionAndStringAswerWidget) v).syncAnswerShown();
            //v.setVisibility(View.VISIBLE);
        }
        if (v.getVisibility() != View.VISIBLE)
        {
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Returns true if the prompt p is represented by View v. It can use the
     * position, but if the position is different it has to return true: it will
     * be showView to handle postion issues
     *
     * @param p
     * @param v
     * @param promptPosition
     * @param viewPosition
     * @return
     */
    private boolean checkIdentity(FormEntryPrompt p, View v, int promptPosition, int viewPosition)
    {
        if (v instanceof QuestionWidget)
        {
            return p.getFormElement().getID() == ((QuestionWidget) v).getFormId();
            //return p.getIndex().toString().equals(((QuestionWidget) v).getFormId());
        }
        return p.getFormElement().getID() == v.getId();
        //return p.getIndex().toString().equals(v.getId());
    }

    /**
     * @return a HashMap of answers entered by the user for this set of widgets
     */
    public LinkedHashMap<FormIndex, IAnswerData> getAnswers()
    {
        answers = new LinkedHashMap<FormIndex, IAnswerData>();
        String version = "";

        PackageInfo pInfo;
        try {
            pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }


        //Log.e("- getAnswers() in ODKView -", "Mappa answers di JavaRosa per mappare i FormIndex e le IAnswerData");

        Iterator<QuestionWidget> i = widgets.iterator();
        String tmpA;
        String tmpB="";
        Integer contatore = 1;
        while (i.hasNext())
        {
			/*
			 * The FormEntryPrompt has the FormIndex, which is where the answer
			 * gets stored. The QuestionWidget has the answer the user has
			 * entered.
			 */
            QuestionWidget q = i.next();
            FormEntryPrompt p = q.getPrompt();
            tmpA = p.getIndex().toString().split(",")[0];//tempA contiene il numero di pagine nella quale ci troviamo
            if(!tmpA.equals(tmpB) && tmpB != null && tmpB.length()!=0)
            {
                System.out.println("Errore -------------------------  " + tmpA + " == " + tmpB );
            }

            tmpB = p.getIndex().toString().split(",")[0];

            if(p!=null)
            {
                //System.out.println("QuestionWidget:" + p.getIndex()	+ "FormEntryPrompt:" + p.getAnswerText());
                //if(p.getFormElement().getLabelInnerText().equals("Client version"))
                //answers.put(p.getIndex(), new StringData(version));
                answers.put(p.getIndex(), q.getAnswer());
            }

            //CREO UN HASHMAP CON GLI INDICI DELLA formEntryPrompt E IL testo NELLA VIEW
            //dataMaps.put(p.getIndex().getNextLevel().getReference().getName(1).toString(), q.getAnswer());

        }
        firstBlock = false;
        //ODKView.ROSTER = false;
        return answers;
    }

    public HashMap<String, IAnswerData> getDataMaps()
    {
		/*
		map = new LinkedHashMap<String, IAnswerData>();
		//************************
		
		Iterator<QuestionWidget> i = widgets.iterator();
		while (i.hasNext())
		{		
			QuestionWidget q = i.next();
			FormEntryPrompt p = q.getPrompt();
			
			
			String stringa = null;
			String str = p.getIndex().toString();
			if(ROSTER)
			{
				if(p.isReadOnly())
				{
					if(str.contains("_0"))
					{
						if(p.getAnswerText() != null)
							map.put(str, q.getAnswer());
					}
					else
					{
						if(p.getAnswerText() == null)
						{
							String ind = p.getIndex().toString().split("_")[1].split(",")[0];
							answers.put(p.getIndex(), map.get(p.getIndex().toString().replace("_"+ind, "_0")));
						}
					}
				}
			}
			//************************
		}
		*/
        return map;
    }


    /**
     * // * Add a TextView containing the hierarchy of groups to which the
     * question belongs. //
     */

    //LABEL CHE CONTA QUANTI ROSTER SONO STATI VISUALIZZATI
    private void addGroupText(FormEntryCaption[] groups)
    {
        TextView tvTitleTable = new TextView(getContext());
        TextView tvSectionTable = new TextView(getContext());
        StringBuffer s = new StringBuffer("");
        String t = "";
        int i;
        // list all groups in one string
        for (FormEntryCaption g : groups)
        {
            i = g.getMultiplicity() + 1;
            t = g.getLongText();
            if (t != null)
            {
                SpannableString spanString = new SpannableString(s);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                tvTitleTable.setText(spanString);
                tvTitleTable.setText(s);

                tvSectionTable.setText("> " + t);
                int k = g.getIndex().getReference().size();
                if (g.repeats() && i > 0)
                {
                    Log.e("- ROSTER -", "Creo un roster");
                    //tanti roster quanti ne compila l'utente
                    s.append(" < " + i + " >");
                    tvTitleTable.setText("Roster" + s);
                }
                else if(k > 0 && g.getAppearanceHint() == null) //TABELLA
                {
                    Log.e("- TABELLA -", "Creo una tabella");
                    s.append(" < " + t + " >");
                    tvTitleTable.setText("Table: " + s);
					
					/*
					tvTitleTable.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXTSIZE + 4);
					tvSectionTable.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXTSIZE);
					tvTitleTable.setPadding(0, 0, 0, 5);
					mView.addView(tvTitleTable, mLayout);
					mView.addView(tvSectionTable, mLayout);
					
					//--------------------------------------
					//AGGIUNTO PER DARE IL FOCUS AL TITOLO della Tabella WIDGET
					tvTitleTable.setFocusable(true);
					tvTitleTable.setFocusableInTouchMode(true);	
					tvTitleTable.requestFocus();
					*/
                    //--------------------------------------
                }
            }
        }
        // build view //SE LA VIEW SI RIPETE SIAMO NEL ROSTER E AGGIUNGIAMO IL NUMERO
        if (s.length() > 0) {
            //tvTitleTable.setText("Roster" + s);
            tvTitleTable.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXTSIZE + 4);
            tvSectionTable.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXTSIZE);
            tvTitleTable.setPadding(0, 0, 0, 5);
            mView.addView(tvTitleTable, mLayout);
            mView.addView(tvSectionTable, mLayout);

            //--------------------------------------
            //AGGIUNTO PER DARE IL FOCUS AL TITOLO del roster WIDGET
            tvTitleTable.setFocusable(true);
            tvTitleTable.setFocusableInTouchMode(true);
            tvTitleTable.requestFocus();

            isRoster = true;
            //--------------------------------------
        }
    }

    String indexcurrent = new String();

    public void setFocus(final Context context) {
        if (widgets.size() > 0) {
            int answerint = 0;
            if (formEntryPrompt != null) {
                indexcurrent = formEntryPrompt.getIndex().getNextLevel()
                        .toString().replace(" ", "");
                // System.out.println(" indexcurrent: "+indexcurrent);
                String[] indexcurrent1 = indexcurrent.toString().split(",");
                if (indexcurrent1.length == 2) {
                    answerint = Integer.parseInt(indexcurrent1[1]);
                } else {
                    answerint = Integer.parseInt(indexcurrent1[0]);
                }
                if (answerint <= widgets.size() - 1) {
                    widgets.get(answerint).setFocus(context);
                }
            } else if(formEntryPrompt == null)
                widgets.get(0).setFocus(context);
        }
    }

    /**
     * Called when another activity returns information to answer this question.
     *
     * @param answer
     */
    public void setBinaryData(Object answer) {
        boolean set = false;
        for (QuestionWidget q : widgets) {
            if (q instanceof IBinaryWidget) {
                if (((IBinaryWidget) q).isWaitingForBinaryData()) {
                    ((IBinaryWidget) q).setBinaryData(answer);
                    set = true;
                    break;
                }
            }
        }
        if (!set) {
            Log.w(t,
                    "Attempting to return data to a widget or set of widgets no looking for data");
        }
    }

    /**
     * @return true if the answer was cleared, false otherwise.
     */
    public boolean clearAnswer() {
        // If there's only one widget, clear the answer.
        // If there are more, then force a long-press to clear the answer.
        if (widgets.size() == 1 && !widgets.get(0).getPrompt().isReadOnly()) {
            widgets.get(0).clearAnswer();
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<QuestionWidget> getWidgets() {
        return widgets;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        for (int i = 0; i < widgets.size(); i++) {
            QuestionWidget qw = widgets.get(i);
            qw.setOnFocusChangeListener(l);
        }
    }

	/*
	@Override
	public boolean onLongClick(View v) {
		return false;
	}
	*/

    /**
     * not used
     */
    @Override
    public void cancelLongPress() {
		/*
		super.cancelLongPress();
		for (QuestionWidget qw : widgets) {
			qw.cancelLongPress();
		}
		*/
    }

    /**
     * remove a QuestionAndStringAnswerWidget
     * @param v
     */
    public void removeElement(QuestionAndStringAswerWidget v)
    {
        Iterator<QuestionWidget> i = widgets.iterator();
        while (i.hasNext())
        {
			/*
			 * The FormEntryPrompt has the FormIndex, which is where the answer
			 * gets stored. The QuestionWidget has the answer the user has
			 * entered.
			 */
            QuestionWidget q = i.next();
            if(q == v)
            {
                FormEntryPrompt p = v.getPrompt();

                System.out.println("RIMUOVI QuestionWidget:" + p.getIndex()	+ ", RIMUOVI FormEntryPrompt:" + p.getAnswerText());

                Iterator<Map.Entry<FormIndex, IAnswerData>> iter = answers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<FormIndex, IAnswerData> entry = iter.next();
                    if(p.getIndex().equals(entry.getKey()))
                    {
                        answers.put(p.getIndex(), q.setAnswer(new StringData("")));
                        // answers.remove(entry.getKey()); LL 06-03-14 eliminata per problemi di concorrenza "concurrentmodificationexception"
                        iter.remove();
                    }
                }

                //answers.remove(p.getIndex(), "");
            }
			/*
			if(p.getAnswerText() != null)
			{
			*/

            //answers.put(p.getIndex(), q.getAnswer());

            //CREO UN HASHMAP CON GLI INDICI DELLA formEntryPrompt E IL testo NELLA VIEW
            ///dataMaps.put(p.getIndex().getNextLevel().getReference().getName(1).toString(), q.getAnswer());
        }
        //firstBlock = false;
        return;
    }


    /**
     * remove a QuestionWidget
     * @param v the questionWidget to remove
     */
    public void removeElement(QuestionWidget v)
    {
        Iterator<QuestionWidget> i = widgets.iterator();
        while (i.hasNext())
        {
			/*
			 * The FormEntryPrompt has the FormIndex, which is where the answer
			 * gets stored. The QuestionWidget has the answer the user has
			 * entered.
			 */
            QuestionWidget q = i.next();
            if(q == v)
            {
                FormEntryPrompt p = v.getPrompt();

                System.out.println("RIMUOVI QuestionWidget:" + p.getIndex()	+ ", RIMUOVI FormEntryPrompt:" + p.getAnswerText());

                Iterator<Map.Entry<FormIndex, IAnswerData>> iter = answers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<FormIndex, IAnswerData> entry = iter.next();
                    if(p.getIndex().equals(entry.getKey()))
                    {
                        answers.put(p.getIndex(), q.setAnswer(new StringData("")));
                        //answers.remove(entry.getKey());LL 06-03-14 eliminata per problemi di concorrenza "concurrentmodificationexception"
                        iter.remove();
                    }
                }

                //answers.remove(p.getIndex(), "");
            }
			/*
				if(p.getAnswerText() != null)
				{
				*/

            //answers.put(p.getIndex(), q.getAnswer());

            //CREO UN HASHMAP CON GLI INDICI DELLA formEntryPrompt E IL testo NELLA VIEW
            ///dataMaps.put(p.getIndex().getNextLevel().getReference().getName(1).toString(), q.getAnswer());
        }
        //firstBlock = false;
        return;
    }

    /**
     * simply add a divider
     */
    public void addDivider()
    {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        divider.setMinimumHeight(display.getHeight()/2);

        if(divider.getParent() != null){//LL 03-04-2014  se il divider ha gia' un genitore prima glielo tolgo e poi ce lo rimetto altrimenti va in errore se in una pagina c'e' un campo calcolato e prima che questo venga valorizzato,
            mView.removeView(divider);  //(ma i widget degli operandi lo sono) si clicca su uno spinner presente nella stessa pagina
        }
        mView.addView(divider);
        //****************************************************
    }

    /**
     * remove a divider
     */
    public void removeDivider()
    {
        //***********************************  RIMUOVO DIVIDER
        mView.removeView(divider);
        //****************************************************
    }

    /**
     * @return a list of the answers
     */
    public static LinkedHashMap<FormIndex, IAnswerData> getMap()
    {
        return answers;
    }

    /**
     * @return list of the widgets
     */
    public static ArrayList<QuestionWidget> getWidget()
    {
        return widgets;
    }
}