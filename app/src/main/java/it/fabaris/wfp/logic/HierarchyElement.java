/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.fabaris.wfp.logic;

import it.fabaris.wfp.utility.ColorHelper;

import java.util.ArrayList;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 *
 * Class that implements the element of the index
 *
 */
public class HierarchyElement {

    public static final int CHILD = 1;
    public static final int EXPANDED = 2;
    public static final int COLLAPSED = 3;
    public static final int QUESTION = 4;

    private String mPrimaryText = "";
    private String mSecondaryText = "";
    private Drawable mIcon;
    private ColorHelper colorHelper;
    private Boolean inError = false;
    private Boolean toggleHit = false;
    int mType;
    private int dataType= Constants.DATATYPE_NULL;
    FormIndex mFormIndex;
    ArrayList<HierarchyElement> mChildren;

	public HierarchyElement(String text1, String text2, Drawable bullet,
			ColorHelper colorHelper, int type,int dataType, FormIndex f) {
		mIcon = bullet;
		mPrimaryText = text1;
		mSecondaryText = text2;
		this.colorHelper = colorHelper;
		mFormIndex = f;
		mType = type;
		this.dataType = dataType;
		mChildren = new ArrayList<HierarchyElement>();
	}

    public String getPrimaryText() {
        return mPrimaryText;
    }

    public String getSecondaryText() {
        return mSecondaryText;
    }

    public void setPrimaryText(String text) {
        mPrimaryText = text;
    }

    public void setSecondaryText(String text) {
        mSecondaryText = text;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public FormIndex getFormIndex() {
        return mFormIndex;
    }

    public int getType() {
        return mType;
    }

    public void setType(int newType) {
        mType = newType;
    }

    public ArrayList<HierarchyElement> getChildren() {
        return mChildren;
    }

    public void addChild(HierarchyElement h) {
        mChildren.add(h);
    }

    public void setChildren(ArrayList<HierarchyElement> children) {
        mChildren = children;
    }

    public int getDefaultBackgroundColor() {
        return colorHelper.getDefaultBackgroundColor();
    }

    public int getDefaultForeColor() {
        return colorHelper.getDefaultForeColor();
    }

    public int getMandatoryBackgroundColor() {
        return colorHelper.getMandatoryBackgroundColor();
    }

    public int getMandatoryForeColor() {
        return colorHelper.getMandatoryForeColor();
    }

    public int getErrorBackgroundColor() {
        return colorHelper.getErrorBackgroundColor();
    }

    public int getErrorForeColor() {
        return colorHelper.getErrorForeColor();
    }

    public int getReadOnlyBackgroundColor() {
        return colorHelper.getReadOnlyBackgroundColor();
    }

    public int getReadOnlyForeColor() {
        return colorHelper.getReadOnlyForeColor();
    }

    public Boolean isInError() {
        return inError;
    }

    public void setInError(Boolean inError) {
        this.inError = inError;
    }

    public Boolean isExpanded() {
        return this.mType == EXPANDED;
    }

    public int getDataType() { return dataType; }

    public void setDataType(int dataType) { this.dataType = dataType; }

    public int getTextColor() {
		if (!this.toggleHit) {
			return Color.WHITE;
		}
		if (isInError()) {
			return getErrorForeColor();
		}
		return getDefaultForeColor();
	}

    public int getBackColor() {
        if (!this.toggleHit) {
            return Color.BLACK;
        }
        if (isInError()) {
            return getErrorBackgroundColor();
        }
        return getDefaultBackgroundColor();
    }

    public void ToggleHit() {
        this.toggleHit = !this.toggleHit;
    }

}