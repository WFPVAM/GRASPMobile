/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 ******************************************************************************/
package it.fabaris.wfp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.javarosa.core.model.Constants;

import java.util.ArrayList;
import java.util.List;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.logic.HierarchyElement;
import it.fabaris.wfp.view.HierarchyElementView;

/**
 * Class that defines the single object in the index of the form
 *
 */
public class HierarchyListAdapter extends BaseAdapter {

    private Context mContext;
    private List<HierarchyElement> mItems = new ArrayList<HierarchyElement>();


    public HierarchyListAdapter(Context context) {
        mContext = context;
    }


    @Override
    public int getCount() {
        return mItems.size();
    }


    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HierarchyElementView hev;
        if (convertView == null) {
            hev = new HierarchyElementView(mContext, mItems.get(position));
        } else {
            hev = (HierarchyElementView) convertView;
            hev.setPrimaryText(mItems.get(position).getPrimaryText());
            hev.setSecondaryText(mItems.get(position).getSecondaryText());
            hev.setIcon(mItems.get(position).getIcon());
            if(FormEntryActivity.mFormController.isQuestion(mItems.get(position).getFormIndex())) {
                if (FormEntryActivity.mFormController.getQuestionPrompt(mItems.get(position).getFormIndex()).isRequired() && mItems.get(position).getSecondaryText() == null) {
                    hev.setBackColor(Color.parseColor("#ff0000"));
                } else {
                    hev.setBackColor(mItems.get(position).getBackColor());
                }
            }
            else {
                hev.setBackColor(mItems.get(position).getBackColor());
            }
        }

        if (mItems.get(position).getSecondaryText() == null
                || mItems.get(position).getSecondaryText().equals("") || mItems.get(position).getDataType()== Constants.DATATYPE_BINARY) {
            hev.showSecondary(false);
        } else {
            hev.showSecondary(true);
        }
        return hev;

    }


    /**
     * Sets the list of items for this adapter to use.
     */
    public void setListItems(List<HierarchyElement> it) {
        mItems = it;
    }

}
