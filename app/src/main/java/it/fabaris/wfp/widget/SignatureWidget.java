package it.fabaris.wfp.widget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import it.fabaris.wfp.activities.FormEntryActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.utility.FileUtils;

/**
 * Widget that allows user to take signature and add it to the form.
 * @author :Mureed Al-Barghouthi (UNOPS team)
 */
public class SignatureWidget extends QuestionWidget implements IBinaryWidget {
    private final static String t = "MediaWidget";

    private Button button_save;
    private GestureOverlayView gesture;

    private String mBinaryName;

    private String mInstanceFolder;
    private boolean mWaitingForData;

    private static TextView mErrorTextView;

    private static int RESULT_OK = 5;
    private static int RESULT_CANCELED = 8;


    public SignatureWidget(Context context, final FormEntryPrompt prompt) {
        super(context, prompt);

        mWaitingForData = false;
        mInstanceFolder =
            FormEntryActivity.mInstancePath.substring(0,
                FormEntryActivity.mInstancePath.lastIndexOf("/") + 1);

        setOrientation(LinearLayout.VERTICAL);

        mErrorTextView = new TextView(context);
        mErrorTextView.setText("Selected file is not a valid signature");

        // setup save button
        button_save = new Button(getContext());
        button_save.setText(getContext().getString(R.string.take_signature));
        button_save.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        button_save.setPadding(20, 20, 20, 20);
        button_save.setEnabled(!prompt.isReadOnly());

        button_save.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                mErrorTextView.setVisibility(View.GONE);
                saveSignature();
            }
        });

        addView(gesture);
        addView(button_save);
        addView(mErrorTextView);
        mErrorTextView.setVisibility(View.GONE);
        mBinaryName = prompt.getAnswerText();
    }


    private void saveSignature() {
        try {
            Bitmap gestureImg = gesture.getGesture().toBitmap(100, 100,
                    8, Color.WHITE);
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            File file = new File(path, "signature.jpg");
            fOut = new FileOutputStream(file);
            gestureImg.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            Intent returnIntent = new Intent();
            returnIntent.putExtra("data", gestureImg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void deleteMedia() {
        // get the file path and delete the file

        // There's only 1 in this case, but android 1.6 doesn't implement delete on
        // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI only on
        // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI + a #
        String[] projection = {
            Images.ImageColumns._ID
        };
        Cursor c =
            getContext().getContentResolver().query(
                Images.Media.EXTERNAL_CONTENT_URI, projection,
                "_data='" + mInstanceFolder + mBinaryName + "'", null, null);
        int del = 0;
        if (c.getCount() > 0) {
            c.moveToFirst();
            String id = c.getString(c.getColumnIndex(Images.ImageColumns._ID));

            Log.i(
                t,
                "attempting to delete: "
                        + Uri.withAppendedPath(
                            Images.Media.EXTERNAL_CONTENT_URI, id));
            del =
                getContext().getContentResolver().delete(
                    Uri.withAppendedPath(
                        Images.Media.EXTERNAL_CONTENT_URI, id), null,
                    null);
        }
        c.close();

        // clean up variables
        mBinaryName = null;
        Log.i(t, "Deleted " + del + " rows from media content provider");
    }

    public void clearAnswer() {
        // remove the file
        deleteMedia();
        mErrorTextView.setVisibility(View.GONE);
    }


    public IAnswerData getAnswer() {
        if (mBinaryName != null) {
        	return new StringData(mBinaryName.toString());
        } else {
            return null;
        }
    }


    private String getPathFromUri(Uri uri) {
        if (uri.toString().startsWith("file")) {
            return uri.toString().substring(6);
        } else {
            // find entry in content provider
            Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
            c.moveToFirst();

            // get data path
            String colString = c.getString(c.getColumnIndex("_data"));
            c.close();
            return colString;
        }
    }


    public void setBinaryData(Object binaryuri) {
        // you are replacing an answer. delete the previous image using the
        // content provider.
        if (mBinaryName != null) {
            deleteMedia();
        }
        String binarypath = getPathFromUri((Uri) binaryuri);
        File f = new File(binarypath);
        mBinaryName = f.getName();
        Log.i(t, "Setting current answer to " + f.getName());

        mWaitingForData = false;
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    public boolean isWaitingForBinaryData() {
        return mWaitingForData;
    }


    public void setOnLongClickListener(OnLongClickListener l) {
        button_save.setOnLongClickListener(l);
        if (gesture != null) {
            gesture.setOnLongClickListener(l);
        }
    }


    public void cancelLongPress() {
        super.cancelLongPress();
        button_save.cancelLongPress();
        if (gesture != null) {
            gesture.cancelLongPress();
        }
    }


	@Override
	public IAnswerData setAnswer(IAnswerData a) {
		return null;
	}

}