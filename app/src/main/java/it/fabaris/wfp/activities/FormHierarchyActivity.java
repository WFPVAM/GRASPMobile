/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 *
 ******************************************************************************/
package it.fabaris.wfp.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;

import java.util.ArrayList;
import java.util.List;

import it.fabaris.wfp.adapters.HierarchyListAdapter;
import it.fabaris.wfp.logic.FormController;
import it.fabaris.wfp.logic.HierarchyElement;
import it.fabaris.wfp.utility.ColorHelper;

/**
 * Class is responsible for displaying the index of the form
 *
 */
public class FormHierarchyActivity extends ListActivity
{
    private static final String t = "FormHierarchyActivity";
    int state;

    public static ArrayList<FormEntryPrompt> requiredButEmpty;
    private final String mIndent = "     ";
    public static List<HierarchyElement> formList;
    private ColorHelper colorHelper;

    // List<HierarchyElement> formList;
    TextView mPath;
    FormIndex mStartIndex;
    ArrayList<Boolean> arrRequired;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        colorHelper = new ColorHelper(super.getBaseContext(), getResources());

        setContentView(R.layout.hierarchy_layout);
        FormEntryActivity.fromHyera = true;

        // We use a static FormEntryController to make jumping faster.
        mStartIndex = FormEntryActivity.mFormController.getFormIndex();

        setTitle(getString(R.string.app_name) + " > " + FormEntryActivity.mFormController.getFormTitle());

        mPath = (TextView) findViewById(R.id.pathtext);

        // kinda slow, but works.
        // this scrolls to the last question the user was looking at


        if(!FormListSavedActivity.SAVE == true)
        {
            getListView().post(new Runnable()
            {
                public void run()
                {
                    int position = 0;
                    for (int i = 0; i < formList.size(); i++)
                    {
                        HierarchyElement he = (HierarchyElement) getListAdapter().getItem(i);
                        if (mStartIndex.equals(he.getFormIndex()))
                        {
                            position = i;
                            break;
                        }
                    }
                    getListView().setSelection(position);
                }
            });


            refreshView();
        }
        else
        {
            FormListSavedActivity.SAVE = false;
            finish();
        }
    }

    /**
     *  Step out of any group indexes that are present.
     */
    private void goUpLevel() {
        FormIndex index = stepIndexOut(FormEntryActivity.mFormController.getFormIndex());
        int currentEvent = FormEntryActivity.mFormController.getEvent();

        /**
         *  Step out of any group indexes that are present.
         */
        while (index != null && FormEntryActivity.mFormController.getEvent(index) == FormEntryController.EVENT_GROUP)
        {
            index = stepIndexOut(index);
        }

        if (index == null)
        {
            FormEntryActivity.mFormController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
        }
        else
        {
            if (currentEvent == FormEntryController.EVENT_REPEAT)
            {
                /**
                 *  We were at a repeat, so stepping back brought us to then previous level
                 */
                FormEntryActivity.mFormController.jumpToIndex(index);
            }
            else
            {
                /**
                 *  We were at a question, so stepping back brought us to either:
                 *  The beginning. or The start of a repeat. So we need to step
                 *  out again to go passed the repeat.
                 */
                index = stepIndexOut(index);
                if (index == null)
                {
                    FormEntryActivity.mFormController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
                }
                else
                {
                    FormEntryActivity.mFormController.jumpToIndex(index);
                }
            }
        }
        refreshView();
    }

    private String getCurrentPath() {
        FormIndex index = stepIndexOut(FormEntryActivity.mFormController
                .getFormIndex());

        String path = "";
        while (index != null) {

            path = FormEntryActivity.mFormController.getCaptionPrompt(index)
                    .getLongText()
                    + " ("
                    + (FormEntryActivity.mFormController
                    .getCaptionPrompt(index).getMultiplicity() + 1)
                    + ") > " + path;
            index = stepIndexOut(index);
        }
        // return path?
        return path.substring(0, path.length() - 2);
    }

    /**
     * refresh the View
     */
    public void refreshView() {

        int j = 0;

        FormEntryActivity.fromHyera = true;
        arrRequired = new ArrayList<Boolean>();
        /**
         *  Record the current index so we can return to the same place if the
         *  user hits 'back'.
         */
        FormIndex currentIndex = FormEntryActivity.mFormController
                .getFormIndex();

        /**
         *  If we're not at the first level, we're inside a repeated group so we
         *  want to only display
         *  everything enclosed within that group.
         */
        String enclosingGroupRef = "";
        formList = new ArrayList<HierarchyElement>();

        /**
         *  If we're currently at a repeat node, record the name of the node and
         *  step to the next
         *  node to display.
         */
        if (FormEntryActivity.mFormController.getEvent() == FormEntryController.EVENT_REPEAT)
        {
            enclosingGroupRef = FormEntryActivity.mFormController.getFormIndex().getReference().toString(false);
            FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP);
        }
        else
        {
            FormIndex startTest = stepIndexOut(currentIndex);
            /**
             *  If we have a 'group' tag, we want to step back until we hit a
             *  repeat or the
             *  beginning.
             */
            while (startTest != null && FormEntryActivity.mFormController.getEvent(startTest) == FormEntryController.EVENT_GROUP)
            {
                startTest = stepIndexOut(startTest);
            }
            if (startTest == null)
            {
                /**
                 *  check to see if the question is at the first level of the
                 *  hierarchy. If it is,
                 *  display the root level from the beginning.
                 */
                FormEntryActivity.mFormController.jumpToIndex(FormIndex.createBeginningOfFormIndex());
            }
            else
            {
                // otherwise we're at a repeated group
                FormEntryActivity.mFormController.jumpToIndex(startTest);
            }

            /**
             *  now test again for repeat. This should be true at this point or
             *  we're at the
             *  beginning
             */
            if (FormEntryActivity.mFormController.getEvent() == FormEntryController.EVENT_REPEAT)
            {
                enclosingGroupRef = FormEntryActivity.mFormController.getFormIndex().getReference().toString(false);
                FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP);
            }
        }

        int event = FormEntryActivity.mFormController.getEvent();
        if (event == FormEntryController.EVENT_BEGINNING_OF_FORM)
        {
            /**
             *  The beginning of form has no valid prompt to display.
             */
            FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP);
            mPath.setVisibility(View.GONE);
            /**
             *  jumpPreviousButton.setEnabled(false);
             */
        }
        else
        {
            mPath.setVisibility(View.VISIBLE);
            mPath.setText(getCurrentPath());
            /**
             *  jumpPreviousButton.setEnabled(true);
             */
        }

        /**
         *  Refresh the current event in case we did step forward.
         */
        event = FormEntryActivity.mFormController.getEvent();

        /**
         *  There may be repeating Groups at this level of the hierarchy, we use
         *  this variable to
         *  keep track of them.
         */
        String repeatedGroupRef = "";

        event_search: while (event != FormEntryController.EVENT_END_OF_FORM)
        {
            switch (event)
            {
                case FormEntryController.EVENT_QUESTION:
                    if (!repeatedGroupRef.equalsIgnoreCase(""))
                    {
                        /**
                         *  We're in a repeating group, so skip this question and
                         *  move to the next
                         *  index.
                         */
                        event = FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP);
                        continue;
                    }
                    FormEntryPrompt fp = FormEntryActivity.mFormController.getQuestionPrompt();
                    if(fp != null)
//                        if (fp.isRequired() && fp.getAnswerText()==null){
//                            Log.i("whooo","mmm");
//                            for(int i=0;i< ODKView.getWidget().size();i++){
//                                requiredButEmpty.add(fp);
//                            }
//                        }
                    {
                        try
                        {
                            arrRequired.add(fp.isRequired());
                            if ((fp.getAnswerText() == null || fp.getAnswerText() == "")
                                    && fp.isRequired()) {
                                if (!(!fp.isRequired() && fp.isReadOnly() && !fp.getFormElement().getBind().getReference().toString().toLowerCase().contains("vis") && fp.getDataType() == 3)) {
                                    formList.add(new HierarchyElement(fp.getLongText(), fp
                                            .getAnswerText(), null, colorHelper, HierarchyElement.QUESTION,fp.getDataType(), fp
                                            .getIndex()));
                                }
                            } else if ((fp.getAnswerText() != null || fp.getAnswerText() != "")) {
                                if (!(!fp.isRequired() && fp.isReadOnly() && !fp.getFormElement().getBind().getReference().toString().toLowerCase().contains("vis") && fp.getDataType() == 3)) {

                                    formList.add(new HierarchyElement(fp.getLongText(), fp
                                            .getAnswerText(), null, colorHelper, HierarchyElement.QUESTION,fp.getDataType(), fp
                                            .getIndex()));
                                }
                            }

                            break;
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                case FormEntryController.EVENT_GROUP:
                    // ignore group events
                    break;
                case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                    if (enclosingGroupRef.compareTo(FormEntryActivity.mFormController.getFormIndex().getReference().toString(false)) == 0)
                    {
                        /**
                         *  We were displaying a set of questions inside of a
                         *  repeated group. This is
                         *  the end of that group.
                         */
                        break event_search;
                    }
                    if (repeatedGroupRef.compareTo(FormEntryActivity.mFormController.getFormIndex().getReference().toString(false)) != 0)
                    {
                        /**
                         *  We're in a repeating group, so skip this repeat prompt
                         *  and move to the
                         *  next event.
                         */
                        event = FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP);
                        continue;
                    }
                    if (repeatedGroupRef.compareTo(FormEntryActivity.mFormController.getFormIndex().getReference().toString(false)) == 0)
                    {
                        /**
                         *  This is the end of the current repeating group, so we
                         *  reset the
                         *  repeatedGroupName variable
                         */
                        repeatedGroupRef = "";
                    }
                    break;
                case FormEntryController.EVENT_REPEAT:
                    FormEntryCaption fc = FormEntryActivity.mFormController.getCaptionPrompt();
                    if (enclosingGroupRef.compareTo(FormEntryActivity.mFormController.getFormIndex().getReference().toString(false)) == 0)
                    {
                        /**
                         *  We were displaying a set of questions inside a repeated
                         *  group. This is
                         *  the end of that group.
                         */
                        break event_search;
                    }
                    if (repeatedGroupRef.equalsIgnoreCase("") && fc.getMultiplicity() == 0)
                    {
                        /**
                         *  This is the start of a repeating group. We only want to
                         *  display
                         *  "Group #", so we mark this as the beginning and skip all
                         *  of its children
                         */
                        HierarchyElement group = new HierarchyElement(fc.getLongText(), "", getResources().getDrawable(R.drawable.expander_ic_minimized), colorHelper, HierarchyElement.COLLAPSED, Constants.DATATYPE_NULL, fc.getIndex());
                        repeatedGroupRef = FormEntryActivity.mFormController.getFormIndex().getReference().toString(false);
                        arrRequired.add(true);
                        formList.add(group);
                    }
                    if (repeatedGroupRef.compareTo(FormEntryActivity.mFormController.getFormIndex().getReference().toString(false)) == 0)
                    {
                        /**
                         *  Add this group name to the drop down list for this
                         *  repeating group.
                         */
                        HierarchyElement h = formList.get(formList.size() - 1);
                        if (fc.getLongText() == null) {
                            h.addChild(new HierarchyElement(mIndent + "" + " " + (fc.getMultiplicity() + 1), null, null, colorHelper, HierarchyElement.CHILD,Constants.DATATYPE_NULL, fc.getIndex()));
                        } else {
                            h.addChild(new HierarchyElement(mIndent + fc.getLongText() + " " + (fc.getMultiplicity() + 1), null, null, colorHelper, HierarchyElement.CHILD,Constants.DATATYPE_NULL, fc.getIndex()));
                        }

                    }
                    break;
            }

            event = FormEntryActivity.mFormController.stepToNextEvent(FormController.STEP_OVER_GROUP);

            System.out.println("Evento numero"+ event + ", ciclo "+(++j));
            System.out.println("lista di elementi "+ formList.size());
        }
        if (FormEntryActivity.arrValidForm == null)
        {
            FormEntryActivity.arrValidForm = new ArrayList<String>();
        }
        FormEntryActivity.arrValidForm = new ArrayList<String>();
        System.out.println("formlist size: " + formList.size());
        System.out.println("arrrequired size: " + arrRequired.size());

        for (int i = 0; i <= formList.size() - 1; i++)
        {
            String index = formList.get(i).getFormIndex().toString();
            String value = formList.get(i).getSecondaryText();

            if ((value == null || value.equalsIgnoreCase("")) && arrRequired.get(i))
            {
                FormEntryActivity.arrValidForm.add(index);
                FormEntryActivity.arrValidForm.add("false");
            }
            else
            {
                FormEntryActivity.arrValidForm.add(index);
                FormEntryActivity.arrValidForm.add("true");
            }
        }

        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);

        /**
         *  set the controller back to the current index in case the user hits
         *  'back'
         */
        FormEntryActivity.mFormController.jumpToIndex(currentIndex);
    }

    /**
     * used to go up one level in the formIndex. That is, if you're at 5_0, 1
     * (the second question in a repeating group), this method will return a
     * FormInex of 5_0 (the start of the repeating group). If your at index 16
     * or 5_0, this will return null;
     *
     * @param index
     * @return index
     */
    public FormIndex stepIndexOut(FormIndex index)
    {
        if (index.isTerminal())
        {
            return null;
        }
        else
        {
            return new FormIndex(stepIndexOut(index.getNextLevel()), index);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        HierarchyElement h = (HierarchyElement) l.getItemAtPosition(position);
        if (h.getFormIndex() == null)
        {
            goUpLevel();
            return;
        }

        switch (h.getType())
        {
            case HierarchyElement.EXPANDED:
                h.setType(HierarchyElement.COLLAPSED);
                ArrayList<HierarchyElement> children = h.getChildren();
                for (int i = 0; i < children.size(); i++)
                {
                    formList.remove(position + 1);
                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_minimized));
                h.ToggleHit();
                break;
            case HierarchyElement.COLLAPSED:
                h.setType(HierarchyElement.EXPANDED);
                ArrayList<HierarchyElement> children1 = h.getChildren();
                for (int i = 0; i < children1.size(); i++)
                {
                    Log.i(t, "adding child: " + children1.get(i).getFormIndex());
                    formList.add(position + 1 + i, children1.get(i));
                }
                h.setIcon(getResources().getDrawable(R.drawable.expander_ic_maximized));
                h.ToggleHit();
                break;
            case HierarchyElement.QUESTION:
                FormEntryActivity.mFormController.jumpToIndex(h.getFormIndex());
                h.ToggleHit();
                setResult(RESULT_OK);
                finish();
                return;
            case HierarchyElement.CHILD:
                FormEntryActivity.mFormController.jumpToIndex(h.getFormIndex());
                h.ToggleHit();
                setResult(RESULT_OK);
                refreshView();
                return;
        }

        /**
         *  Should only get here if we've expanded or collapsed a group
         */
        HierarchyListAdapter itla = new HierarchyListAdapter(this);
        itla.setListItems(formList);
        setListAdapter(itla);
        getListView().setSelection(position);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                FormEntryActivity.mFormController.jumpToIndex(mStartIndex);
        }
        return super.onKeyDown(keyCode, event);
    }

}