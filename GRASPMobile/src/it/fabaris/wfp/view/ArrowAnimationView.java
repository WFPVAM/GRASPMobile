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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Class not used in GRASP solution
 *
 */

public class ArrowAnimationView extends View {

    private final static String t = "ArrowAnimationView";

    private Animation mAnimation;
    private Bitmap mArrow;

    private int mImgXOffset;


    public ArrowAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(t, "called constructor");
        init();
    }


    public ArrowAnimationView(Context context) {
        super(context);
        init();
    }


    private void init() {
        mArrow = BitmapFactory.decodeResource(getResources(), R.drawable.left_arrow);
        mImgXOffset = mArrow.getWidth() / 2;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mAnimation == null) {
            createAnim(canvas);
        }

        int centerX = canvas.getWidth() / 2;

        canvas.drawBitmap(mArrow, centerX - mImgXOffset, (float) mArrow.getHeight() / 4, null);
    }


    private void createAnim(Canvas canvas) {
        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.start_arrow);
        startAnimation(mAnimation);
    }
}
