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

import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.logic.HierarchyElement;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 
 * Implement the view that hold a hyerarchy element
 *
 */
public class HierarchyElementView extends RelativeLayout {

    private TextView mPrimaryTextView;
    private TextView mSecondaryTextView;
    private ImageView mIcon;

    public HierarchyElementView(Context context, HierarchyElement it) {
        super(context);

        setBackColor(it.getBackColor());

        mIcon = new ImageView(context);
        mIcon.setImageDrawable(it.getIcon());
        mIcon.setId(1);
        mIcon.setPadding(0, 4, 4, 25);//mIcon.setPadding(0, dipToPx(4), dipToPx(4), dipToPx(25));

        addView(mIcon, new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        mPrimaryTextView = new TextView(context);
        mPrimaryTextView.setTextAppearance(context, R.style.small);
        mPrimaryTextView.setText(it.getPrimaryText());
        mPrimaryTextView.setTextAppearance(context, Typeface.BOLD);
        mPrimaryTextView.setId(2);
        mPrimaryTextView.setGravity(Gravity.CENTER_VERTICAL);
        mPrimaryTextView.setPadding(4, 4, 4, 4);
        mPrimaryTextView.setTextColor(Color.parseColor("#bdbdbd"));//mPrimaryTextView.setTextColor(it.getTextColor());
        LayoutParams l =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        l.addRule(RelativeLayout.RIGHT_OF, mIcon.getId());
        addView(mPrimaryTextView, l);

        mSecondaryTextView = new TextView(context);
        mSecondaryTextView.setText(it.getSecondaryText());
        mSecondaryTextView.setTextAppearance(context, R.style.large);
        mSecondaryTextView.setGravity(Gravity.CENTER_VERTICAL);
        mSecondaryTextView.setPadding(4, 4, 4, 4);
        mSecondaryTextView.setTextColor(Color.parseColor("#ffffff"));//mSecondaryTextView.setTextColor(it.getTextColor());

        LayoutParams lp =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.BELOW, mPrimaryTextView.getId());
        lp.addRule(RelativeLayout.RIGHT_OF, mIcon.getId());
        addView(mSecondaryTextView, lp);

        setPadding(dipToPx(8), dipToPx(4), dipToPx(8), dipToPx(8));
    }

    public void setPrimaryText(String text) {
        mPrimaryTextView.setText(text);
    }


    public void setSecondaryText(String text) {
        mSecondaryTextView.setText(text);
    }


    public void setIcon(Drawable icon) {
        mIcon.setImageDrawable(icon);
    }


    public void setBackColor(int color) {
        setBackgroundColor(color);
    }
    
    public void setTextColor(int color) {
        setTextColor(color);
    }

    public void showSecondary(boolean bool) {
        if (bool) {
            mSecondaryTextView.setVisibility(VISIBLE);
            setMinimumHeight(dipToPx(64));

        } else {
            mSecondaryTextView.setVisibility(GONE);
            setMinimumHeight(dipToPx(32));

        }
    }
    
    public int dipToPx(int dip) {
        return (int) (dip * getResources().getDisplayMetrics().density + 0.5f);
    }

}