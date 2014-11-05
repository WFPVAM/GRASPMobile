/*******************************************************************************
 * * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 ******************************************************************************/
package it.fabaris.wfp.activities;

import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.listener.AdvanceToNextListener;
import it.fabaris.wfp.listener.FormLoaderListener;
import it.fabaris.wfp.listener.FormSavedListener;
import it.fabaris.wfp.logic.FormController;
import it.fabaris.wfp.logic.PropertyManager;
import it.fabaris.wfp.provider.FormProviderAPI;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.provider.FormProviderAPI.FormsColumns;
import it.fabaris.wfp.provider.InstanceProviderAPI.InstanceColumns;
import it.fabaris.wfp.task.FormLoaderTask;
import it.fabaris.wfp.task.SaveToDiskTask;
import it.fabaris.wfp.utility.ColorHelper;
import it.fabaris.wfp.utility.ConstantUtility;
import it.fabaris.wfp.utility.FileUtils;
import it.fabaris.wfp.view.ODKView;
import it.fabaris.wfp.widget.DecimalWidget;
import it.fabaris.wfp.widget.ImageWidget;
import it.fabaris.wfp.widget.QuestionWidget;
import it.fabaris.wfp.widget.SelectOneWidget;
import it.fabaris.wfp.widget.SpinnerWidget;
import it.fabaris.wfp.widget.StringWidget;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is responsible for displaying questions, animating
 * transitions between questions, and allowing the user to enter data.
 */
public class FormEntryActivity extends Activity implements AnimationListener,
        FormLoaderListener, FormSavedListener, AdvanceToNextListener,
        OnGestureListener {
    private static final String t = "FormEntryActivity";

    // Defines for FormEntryActivity
    private static final boolean EXIT = true;
    private static final boolean DO_NOT_EXIT = false;
    public static final boolean EVALUATE_CONSTRAINTS = true;
    public static final boolean DO_NOT_EVALUATE_CONSTRAINTS = false;

    // Request codes for returning data from specified intent.
    public static final int IMAGE_CAPTURE = 1;
    public static final int BARCODE_CAPTURE = 2;
    public static final int AUDIO_CAPTURE = 3;
    public static final int VIDEO_CAPTURE = 4;
    public static final int LOCATION_CAPTURE = 5;
    public static final int HIERARCHY_ACTIVITY = 6;
    public static final int IMAGE_CHOOSER = 7;
    public static final int AUDIO_CHOOSER = 8;
    public static final int VIDEO_CHOOSER = 9;

    // Extra returned from gp activity
    public static final String LOCATION_RESULT = "LOCATION_RESULT";

    // Identifies the gp of the form used to launch form entry
    public static final String KEY_FORMPATH = "formpath";
    public static final String KEY_INSTANCEPATH = "instancepath";
    public static final String KEY_INSTANCES = "instances";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR = "error";
    public boolean verifica = false;

    // Identifies whether this is a new form, or reloading a form after a screen
    // rotation (or similar)
    private static final String NEWFORM = "newform";

    // private static final int MENU_LANGUAGES = Menu.FIRST;
    // private static final int MENU_HIERARCHY_VIEW = Menu.FIRST + 1;
    // private static final int MENU_SAVE = Menu.FIRST + 2;
    // private static final int MENU_PREFERENCES = Menu.FIRST + 3;
    private static final int PROGRESS_DIALOG = 1;
    private static final int SAVING_DIALOG = 2;
    static protected ColorHelper colorHelper;
    // Random ID
    private static final int DELETE_REPEAT = 654321;

    public static String mFormPath;
    public static String mInstancePath;
    public String mInstanceFolder;

    private GestureDetector mGestureDetector;
    public static FormController mFormController;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    private LinearLayout mLinearLayout;
    public View mCurrentView;


    private AlertDialog mAlertDialog;
    private ProgressDialog mProgressDialog;
    private String mErrorMessage;

    // used to limit forward/backward swipes to one per question
    private boolean mBeenSwiped;
    private FormLoaderTask mFormLoaderTask;
    private SaveToDiskTask mSaveToDiskTask;

    public static String formName;
    static String formNameInstance;
    public static String formId;
    public static String formEnumeratorId;
    public static String formIdDataBase;

    private static final String TAG = "MyActivity";
    public ArrayList<Integer> arrSavestatus = new ArrayList<Integer>();
    public int arrContSavestatus = 0;
    int size = 0;
    int indexcurrent = 0;
    boolean arrTrue = true;
    public static boolean fromHyera = false;
    public static ArrayList<String> arrValidForm;
    public static boolean radioFirstCheck = true;
    private ODKView odkv = null;

    private boolean submitted = false;

    public static boolean ROSTER;
    public static HashMap<String, IAnswerData> readOnlyInRoster;


    //Added by Mureed  22-8-2014
    String mCurrentPhotoPath;


    // private static final List<ColorHelper> colorHelper = new
    // ArrayList<ColorHelper>();

    public enum AnimationType {
        LEFT, RIGHT, FADE
    }

    Intent intent;

    /**
     * onCreate
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ********************************** 12/11/2013
        readOnlyInRoster = new HashMap<String, IAnswerData>();
        // **********************************

        // -- 11/10/2013 -- DO NOT CHANGE -- IT IS CORRECT
        // -----------------------------------------------------------
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // -------------------------------------------------------------------------
        setContentView(R.layout.form_entry);

        // must be at the beginning of any activity that can be called from an
        // external intent
        try {
            Collect.createODKDirs();
            PreferencesActivity.TO_SAVE_FORM = false;
            // PreferencesActivity.verificaArray = false;
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), EXIT);
            return;
        }

        mLinearLayout = (LinearLayout) findViewById(R.id.rl);
        mBeenSwiped = false;
        mAlertDialog = null;
        mCurrentView = null;
        mInAnimation = null;
        mOutAnimation = null;
        mGestureDetector = new GestureDetector(this);

        try {
            // Load JavaRosa modules. needed to restore forms.
            new XFormsModule().registerModule();
            // needed to override rms property manager
            org.javarosa.core.services.PropertyManager
                    .setPropertyManager(new PropertyManager(
                            getApplicationContext()));
        } catch (NullPointerException e) {
            Log.v(TAG, e.toString());
        }

        // needed to override rms property manager
        // org.javarosa.core.services.PropertyManager.setPropertyManager(new
        // PropertyManager(
        // getApplicationContext()));

        // CHECK OF THE REQUEST
        Boolean newForm = true;
        if (savedInstanceState != null) {//se non e' la prima volta che chiamiamo l'onCreate per questa classe
            if (savedInstanceState.containsKey(KEY_FORMPATH)) {
                mFormPath = savedInstanceState.getString(KEY_FORMPATH);
            }
            if (savedInstanceState.containsKey(NEWFORM)) {
                newForm = savedInstanceState.getBoolean(NEWFORM, true);
            }
            if (savedInstanceState.containsKey(KEY_ERROR)) {
                mErrorMessage = savedInstanceState.getString(KEY_ERROR);
            }
        }

        // If a parse error message is showing then nothing else is loaded
        // Dialogs mid form just disappear on rotation.
        if (mErrorMessage != null) {
            createErrorDialog(mErrorMessage, EXIT);
            return;
        }

        // Check to see if this is a screen flip or a new form load.
        Object data = getLastNonConfigurationInstance();
        if (data instanceof FormLoaderTask) {
            mFormLoaderTask = (FormLoaderTask) data;
        } else if (data instanceof SaveToDiskTask) {
            mSaveToDiskTask = (SaveToDiskTask) data;
        } else if (data == null) {

            if (!newForm) {
                // refreshCurrentView(null);
                return;
            }

            // Not a restart from a screen orientation change (or other).
            mFormController = null;
            mInstancePath = null;

            // Intent intent = getIntent();
            // String path =
            // Environment.getExternalStorageDirectory().getAbsolutePath();
            // String file = path+str;

            intent = getIntent();
            // intent.setData(Uri.parse( mFormPath));
            if (intent != null) {
                String keyIdentifer = getPackageName();

                String pathxml = intent.getStringExtra(keyIdentifer + "ciao");
                formName = intent.getStringExtra(keyIdentifer + "ciao1");
                formNameInstance = intent.getStringExtra(keyIdentifer + "ciao2");
                formId = intent.getStringExtra(keyIdentifer + "ciao3");
                formIdDataBase = intent.getStringExtra(keyIdentifer + "ciao4");


                // Uri path = Uri.parse(pathxml);
                intent.getAction();
                intent.getType();

                try {
                    // Uri uri = path;
                    Uri uri = intent.getData();
                    // String formid = formname.substring(5);
                    if (getContentResolver().getType(uri) == InstanceColumns.CONTENT_TYPE) {
                        // Cursor instanceCursor = this.managedQuery(uri, null,
                        // null, null, null);
                        // Cursor instanceCursor =
                        // managedQuery(FormProviderAPI.FormsColumns.CONTENT_URI,
                        // null, null, null,null);
                        String[] selectionArgs1 = {formNameInstance};
                        String selection1 = FormsColumns.DISPLAY_NAME_INSTANCE
                                + " like ?";

                        Cursor instanceCursor = managedQuery(
                                FormProviderAPI.FormsColumns.CONTENT_URI, null,
                                selection1, selectionArgs1, null);
                        if (instanceCursor.getCount() != 1) {
                            this.createErrorDialog(getString(R.string.bad_uri)
                                    + " " + uri, EXIT);
                            return;
                        } else {
                            instanceCursor.moveToFirst();
                            mInstancePath = instanceCursor
                                    .getString(instanceCursor
                                            .getColumnIndex(FormsColumns.INSTANCE_FILE_PATH));
                            String jrFormId = instanceCursor
                                    .getString(instanceCursor
                                            .getColumnIndex(FormsColumns.JR_FORM_ID));
                            String[] selectionArgs = {formNameInstance};

                            String selection = FormsColumns.DISPLAY_NAME_INSTANCE
                                    + " like ?";
                            Cursor formCursor = managedQuery(
                                    FormsColumns.CONTENT_URI, null, selection,
                                    selectionArgs, null);
                            if (formCursor.getCount() == 1) {
                                formCursor.moveToFirst();
                                mFormPath = formCursor.getString(7);
                            } else if (formCursor.getCount() < 1) {
                                this.createErrorDialog(
                                        getString(R.string.parent_form_not_exist),
                                        EXIT);
                                return;
                            } else if (formCursor.getCount() > 1) {
                                this.createErrorDialog(
                                        getString(R.string.more_form), EXIT);
                                return;
                            }
                        }

                    } else if (getContentResolver().getType(uri) == FormsColumns.CONTENT_TYPE) {
                        mFormPath = Collect.FORMS_PATH + "/" + formName
                                + ".xml";
                        mFormLoaderTask = new FormLoaderTask();
                        mFormLoaderTask.execute(mFormPath);
                        showDialog(PROGRESS_DIALOG);
                    } else {
                        Log.e(t, "unrecognized URI");
                        this.createErrorDialog(
                                getString(R.string.unrecognized_uri) + " "
                                        + uri, EXIT
                        );
                        return;
                    }

                    mFormLoaderTask = new FormLoaderTask();
                    mFormLoaderTask.execute(pathxml);
                    showDialog(PROGRESS_DIALOG);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * saved the state before to leave the activity
     */
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_FORMPATH, mFormPath);
        outState.putBoolean(NEWFORM, false);
        outState.putString(KEY_ERROR, mErrorMessage);
    }


//    /*
//     Added by mureed
//     to fix capture image bug
//     */
//    private File createImageFile() throws IOException {
//        // Create an image file name
//        mInstanceFolder = mInstancePath.substring(0,mInstancePath.lastIndexOf("/") + 1);
//        String imageFileName = mInstanceFolder + "/" + System.currentTimeMillis() + ".jpg";
//
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
//        return image;
//    }

    /**
     * called after a barcode capture or an image capture in order to get the result and save it in a file
     *
     * @param requestCode The integer request code, allowing you to identify who this result came from
     * @param resultCode  The integer result code returned by the child activity through its setResult()
     * @param Intent      which can returns result data to the caller
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        ContentValues values;
        Uri imageURI;

        switch (requestCode) {
            case BARCODE_CAPTURE:
                String sb = intent.getStringExtra("SCAN_RESULT");
                ((ODKView) mCurrentView).setBinaryData(sb);
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case IMAGE_CAPTURE:

                /**
                 * We saved the image to the tempfile_path, but we really want it to
                 * be in: /sdcard/odk/instances/[current instnace]/something.jpg so
                 * we move it there before inserting it into the content provider.
                 * Once the android image capture bug gets fixed, (read, we move on
                 * from Android 1.6) we want to handle images the audio and video
                 *
                 * The intent is empty, but we know we saved the image to the temp file
                 */
                File fi = new File(Collect.TMPFILE_PATH);

                mInstanceFolder = mInstancePath.substring(0, mInstancePath.lastIndexOf("/") + 1);

                String s = mInstanceFolder + "/" + System.currentTimeMillis() + ".jpg";

                File nf = new File(s);

                if (!fi.renameTo(nf)) {
                    Log.e(t, "Failed to rename " + fi.getAbsolutePath());
                } else {
                    Log.i(t,
                            "renamed " + fi.getAbsolutePath() + " to "
                                    + nf.getAbsolutePath()
                    );
                }
                /**
                 * Add the new image to the Media content provider so that the
                 * viewing is fast in Android 2.0+
                 */
                values = new ContentValues(6);
                values.put(Images.Media.TITLE, nf.getName());
                values.put(Images.Media.DISPLAY_NAME, nf.getName());
                values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
                values.put(Images.Media.MIME_TYPE, "image/jpeg");
                values.put(Images.Media.DATA, nf.getAbsolutePath());

                imageURI = getContentResolver().insert(
                        Images.Media.EXTERNAL_CONTENT_URI, values);

// /*
//                 * Compress image
//                 */
//                compressImage(s, 400, 400, 100, 0);

                if (mCurrentView != null) {
                    ImageWidget.previewPhoto(s, FormEntryActivity.this);
                    ((ODKView) mCurrentView).setBinaryData(imageURI);
                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                    refreshCurrentView(null);
                } else {
                    System.out.println("Sorry, your image compressed and saved but can't able to view it, Please choose it from the gallery");
//                    createErrorDialog("Sorry, your image compressed and saved but can't able to view it, Please choose it from the gallery", false);
                }

                break;
            case IMAGE_CHOOSER:

                /**
                 * We have a saved image somewhere, but we really want it to be in:
                 * /sdcard/odk/instances/[current instnace]/something.jpg so we move
                 * it there before inserting it into the content provider. Once the
                 * android image capture bug gets fixed, (read, we move on from
                 * Android 1.6) we want to handle images the audio and video
                 */
                String sourceImagePath = null;
                Uri selectedImage = intent.getData();

                if (selectedImage.toString().startsWith("file")) {
                    sourceImagePath = selectedImage.toString().substring(6);
                } else {
                    String[] projection = {Images.Media.DATA};
                    Cursor cursor = managedQuery(selectedImage, projection, null,
                            null, null);
                    startManagingCursor(cursor);
                    int column_index = cursor
                            .getColumnIndexOrThrow(Images.Media.DATA);
                    cursor.moveToFirst();
                    sourceImagePath = cursor.getString(column_index);
                }

                /**
                 *  Copy file to sdcard
                 */
                String mInstanceFolder1 = mInstancePath.substring(0,  mInstancePath.lastIndexOf("/") + 1);
                String destImagePath = mInstanceFolder1 + "/"+ System.currentTimeMillis() + ".jpg";

                File source = new File(sourceImagePath);
                File newImage = new File(destImagePath);
                FileUtils.copyFile(source, newImage);


                if (newImage.exists()) {
                    /**
                     *  Add the new image to the Media content provider so that the
                     *  viewing is fast in Android 2.0+
                     */

                    values = new ContentValues(6);
                    values.put(Images.Media.TITLE, newImage.getName());
                    values.put(Images.Media.DISPLAY_NAME, newImage.getName());
                    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
                    values.put(Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(Images.Media.DATA, newImage.getAbsolutePath());

                    imageURI = getContentResolver().insert(
                            Images.Media.EXTERNAL_CONTENT_URI, values);
                    Log.i(t, "Inserting image returned uri = " + imageURI.toString());

//                      /*
//                 * Compress image
//                 */
//                    compressImage(destImagePath, 400, 400, 100, 0);

                    ((ODKView) mCurrentView).setBinaryData(imageURI);
                    ImageWidget.previewPhoto(destImagePath, FormEntryActivity.this);
                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                } else {
                    Log.e(t, "NO IMAGE EXISTS at: " + source.getAbsolutePath());
                }
                refreshCurrentView(null);
                break;
            case AUDIO_CAPTURE:
            case VIDEO_CAPTURE:
            case AUDIO_CHOOSER:
            case VIDEO_CHOOSER:

                /**
                 *  For audio/video capture/chooser, we get the URI from the content
                 *  provider
                 * then the widget copies the file and makes a new entry in the
                 * content provider.
                 */

                Uri media = intent.getData();
                ((ODKView) mCurrentView).setBinaryData(media);
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                refreshCurrentView(null);
                break;
            case LOCATION_CAPTURE:
                String sl = intent.getStringExtra(LOCATION_RESULT);
                ((ODKView) mCurrentView).setBinaryData(sl);
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                break;
            case HIERARCHY_ACTIVITY:
                /**
                 *  We may have jumped to a new index in hierarchy activity, so
                 *  refresh
                 */
                refreshCurrentView(null);
                break;
        }
    }


    private void compressImage(String imagePath, int newWidth, int newHeight, int quality, float rotateDegree) {

        Bitmap originalImage = BitmapFactory.decodeFile(imagePath);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);
        matrix.postRotate(rotateDegree);
        try {
            Bitmap resizedBitmap = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, true);
            FileOutputStream fos = new FileOutputStream(imagePath);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void galleryAddPic(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * Refreshes the current view. The controller and the displayed view can get
     * out of sync due to dialogs and restarts caused by screen orientation
     * changes, so they're resynchronized here.
     *
     * @param index Is a FormIndex. A FormIndex is an object used to provide information
     *              about the current index of the form, and thanks to it browse through
     *              the form and skip to the right page
     */
    public void refreshCurrentView(FormIndex index) {
        int event = mFormController.getEvent();

        /**
         *  When we refresh, repeat dialog state isn't maintained, so step back
         *  to the previous
         * question.
         * Also, if we're within a group labeled 'field list', step back to the
         * beginning of that
         * group.
         * That is, skip backwards over repeat prompts, groups that are not
         * field-lists,
         * repeat events, and indexes in field-lists that is not the containing
         * group.
         */

        /**
         * first check for the creation and visualization of the form clicked from one of the lists
         * FIRST CHECK FOR THE CREATION AND VISUALIZATION OF THE FORM CLICKED FROM ONE OF THE LISTS
         */
        while (event == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                || (event == FormEntryController.EVENT_GROUP && !mFormController
                .indexIsInFieldList())
                || event == FormEntryController.EVENT_REPEAT
                || (mFormController.indexIsInFieldList() && !(event == FormEntryController.EVENT_GROUP))) {

            event = mFormController.stepToPreviousEvent();
        }

        /**
         * close the keyboard
         */
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        boolean sent = false;
        try {
            sent = intent.getExtras().getBoolean("submitted");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (submitted == sent) {
            View current = createView(event, index, false);
            showView(current, AnimationType.FADE, index);
        } else {
            finish();
        }
    }

    /**
     * @return true if the current View represents a question in the form
     */
    private boolean currentPromptIsQuestion() {
        return (mFormController.getEvent() == FormEntryController.EVENT_QUESTION || mFormController
                .getEvent() == FormEntryController.EVENT_GROUP);
    }

    /**
     * Attempt to save the answer(s) in the current screen to into the data
     * model.
     *
     * @param evaluateConstraints
     * @return false if any error occurs while saving (constraint violated,
     * etc...), true otherwise.
     */
    public boolean saveAnswersForCurrentScreen(boolean evaluateConstraints) {
        /**
         *  only try to save if the current event is a question or a field-list group
         */

		/*
         * //CHIUDO LA TASTIERA InputMethodManager imm =
		 * (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		 * if(imm.isActive()) { try {
		 * //imm.hideSoftInputFromWindow(getWindow().getDecorView
		 * ().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		 * imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
		 * 0); } catch(Exception e) { Log.e("CHIUSURA DELLA TASTIERA",
		 * "LA TASTIERA NON AVEVA IL FOCUS"); e.printStackTrace(); } }
		 */

        // CLEAR DI TUTTI I FOCUS DELLE EDIT TEXT
        // mCurrentView.clearFocus(); //<--------------------------12/11/2013 SE
        // DECOMMENTO VA IN ERRORE NEI ROSTER PER I MULTILINE E SINGLELINE
        // MULTIPLI

        try {
            if (mFormController.getEvent() == FormEntryController.EVENT_QUESTION
                    || (mFormController.getEvent() == FormEntryController.EVENT_GROUP && mFormController
                    .indexIsInFieldList())) {

                if (mCurrentView != null) {

                    HashMap<FormIndex, IAnswerData> answers = ((ODKView) mCurrentView).getAnswers();

                    ArrayList<QuestionWidget> list = ODKView.getWidget();

                    for (int i = 0; i < list.size(); i++) {
                        //if (list.get(i) instanceof SelectOneWidget|| list.get(i) instanceof SpinnerWidget) {//LL eliminato crea delle anomalie nella inizializzazione degli spinner quando ancora non hanno una risposta
                        if (list.get(i) instanceof SelectOneWidget) {
                            answers.put(
                                    list.get(i).getPrompt().getIndex(),
                                    FormEntryActivity.mFormController
                                            .getQuestionPrompt(
                                                    list.get(i).getPrompt()
                                                            .getIndex()
                                            )
                                            .getAnswerValue()
                            );
                        }
                    }

                    Set<FormIndex> indexKeys = answers.keySet();
                    /**
                     * check the contraints before to do swipe
                     */
                int lestIndex=0;
                    for (FormIndex index : indexKeys) {
                        if (mFormController.getEvent(index) == FormEntryController.EVENT_QUESTION) {
                            int saveStatus = saveAnswer(answers.get(index), index,
                                    evaluateConstraints);

//                            if(saveStatus==2)
//                            {
//
//                                list.get(lestIndex).mQuestionText.setBackgroundColor(colorHelper.getMandatoryBackgroundColor());
//                                list.get(lestIndex).mQuestionText.setTextColor(colorHelper.getMandatoryForeColor());
//
//                            }
                            if (arrValidForm.size() == 0) {
                                if (evaluateConstraints
                                        && saveStatus != FormEntryController.ANSWER_OK) {
                                    createConstraintToast(mFormController
                                            .getQuestionPrompt(index)
                                            .getConstraintText(), saveStatus);
                                    return false;
                                }
                            } else {
                                Log.w(t,
                                        "Attempted to save an index referencing something other than a question: "
                                                + index.getReference()
                                );
                            }
                            for (int i = 0; i <= arrValidForm.size() - 1; i++) {
                                String rep = index.toString().trim()
                                        .substring(0, 4);
                                String splitrep[] = rep.split(",");
                                String splitrep2[] = arrValidForm.get(i).toString()
                                        .substring(0, 4).split(", ,");
                                if (splitrep2[0].toString().equals(rep)) {
                                    if (evaluateConstraints
                                            && saveStatus != FormEntryController.ANSWER_OK) {
                                        createConstraintToast(mFormController
                                                .getQuestionPrompt(index)
                                                .getConstraintText(), saveStatus);
                                        arrValidForm.set(i + 1, "false");
                                        return false;
                                    } else {
                                        arrValidForm.set(i + 1, "true");
                                        Log.w(t,
                                                "Attempted to save an index referencing something other than a question: "
                                                        + index.getReference()
                                        );
                                    }
                                }
                                if (i < arrValidForm.size() - 1) {
                                    i = i + 1;
                                }
                            }
                        }
                        lestIndex++;
                    }
                } else {
                    System.out.println("View is null ................ ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Clear the answer on the screen.
     * Every type of widget implements its
     * own clearAnswer method, for example in the
     * ImageWidget we delete the image file
     * in StringWiddget we set to blank the
     * text value of the Widget
     *
     * @param qw is a QuestionWidget. A QuestionWidget is an object that describe the physical layout
     *           of a question and the question and answer container
     */
    private void clearAnswer(QuestionWidget qw) {
        qw.clearAnswer();
    }


    /**
     * clear the focus to the current widget
     *
     * @param qw is a QuestionWidget. A QuestionWidget is an object that describe the physical layout
     *           of a question.
     */
    private void clearFocus(QuestionWidget qw) {
        qw.clearFocus();
    }

    /**
     * If we're loading, then we pass the loading thread to our next instance.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        // if a form is loading, pass the loader task
        if (mFormLoaderTask != null
                && mFormLoaderTask.getStatus() != AsyncTask.Status.FINISHED)
            return mFormLoaderTask;

        // if a form is writing to disk, pass the save to disk task
        if (mSaveToDiskTask != null
                && mSaveToDiskTask.getStatus() != AsyncTask.Status.FINISHED)
            return mSaveToDiskTask;

        // mFormEntryController is static so we don't need to pass it.
        if (mFormController != null && currentPromptIsQuestion()) {
            saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
        }
        return null;
    }

    /**
     * Creates a view given the View type and an event
     *
     * @param event         is an int that represents in what case we are, such as for example if we are at
     *                      the beginning of the form or if we display a page of simple questions, or the first page of a roster
     *                      or the further pages of a roster and so on
     * @param index         Is a FormIndex. A FormIndex is an object used to provide information
     *                      about the current index of the form, and thanks to it browse through
     *                      the form and skip to the right page
     * @param toBeRecreated
     * @return newly created View
     */
    public View createView(int event, FormIndex index, boolean toBeRecreated) {
        // setTitle(getString(R.string.app_name) + " > " +
        // mFormController.getFormTitle());
        Context context = FormEntryActivity.this;

        switch (event) {
            case FormEntryController.EVENT_BEGINNING_OF_FORM:
                if (arrValidForm == null) {
                    arrValidForm = new ArrayList<String>();
                }
                View startView = View
                        .inflate(this, R.layout.form_entry_start, null);
                // setTitle(getString(R.string.app_name) + " > " +
                // mFormController.getFormTitle());
                startView.focusSearch(View.FOCUS_BACKWARD);

                ((TextView) startView.findViewById(R.id.description))
                        .setText(getString(R.string.enter_data_description));

                Drawable image = null;
                String[] projection = {FormsColumns.FORM_MEDIA_PATH};
                String selection = FormsColumns.FORM_FILE_PATH + "=?";
                String[] selectionArgs = {mFormPath};
                String mediaDir = null;
                BitmapDrawable bitImage = null;

                // show the opendatakit zig...
                if (image == null) {
                    image = getResources().getDrawable(R.drawable.opendatakit_zig);
                    ((ImageView) startView.findViewById(R.id.form_start_bling))
                            .setVisibility(View.GONE);
                } else {
                    ((ImageView) startView.findViewById(R.id.form_start_bling))
                            .setImageDrawable(image);
                }
                return startView;

            case FormEntryController.EVENT_END_OF_FORM:

                View endView = View.inflate(this, R.layout.form_entry_end, null);
                ((TextView) endView.findViewById(R.id.description))
                        .setText(getString(R.string.save_enter_data_description,
                                mFormController.getFormTitle()));
                isInstanceComplete(true);

                // edittext to change the displayed name of the instance
                // final EditText saveAs = (EditText)
                // endView.findViewById(R.id.save_name);
                saveDataToDisk(DO_NOT_EXIT, isInstanceComplete(false), null);
                // disallow carriage returns in the name
                InputFilter returnFilter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            if (Character.getType((source.charAt(i))) == Character.CONTROL) {
                                return "";
                            }
                        }
                        return null;
                    }
                };
                // saveAs.setFilters(new InputFilter[] {
                // returnFilter
                // });
                String saveName = mFormController.getFormTitle();
                if (getContentResolver().getType(getIntent().getData()) == InstanceColumns.CONTENT_ITEM_TYPE) {
                    Uri instanceUri = getIntent().getData();
                    Cursor instance = managedQuery(instanceUri, null, null, null,
                            null);
                    if (instance.getCount() == 1) {
                        instance.moveToFirst();
                        saveName = instance.getString(instance
                                .getColumnIndex(InstanceColumns.DISPLAY_NAME));
                    }
                }

                // saveAs.setText(saveName);
                // Create 'save' button

                ((Button) endView.findViewById(R.id.save_exit_button))
                        .setOnClickListener(new OnClickListener() {

                            public void onClick(View v) {
                                for (int j = 1; j <= arrValidForm.size() - 2; j = j + 2) {
                                    if (!arrValidForm.get(j).equalsIgnoreCase(
                                            "true")) {
                                        // Intent iHiera = new
                                        // Intent(getApplicationContext(),
                                        // FormHierarchyActivity.class);
                                        // iHiera.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        // startActivity(iHiera);

                                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                                v.getContext());
                                        builder.setTitle(
                                                getString(R.string.form_uncompleted))
                                                .setMessage(
                                                        getString(R.string.blank_answer))
                                                .setPositiveButton(
                                                        getString(R.string.positive_choise),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int whichButton) {
                                                                verifica = true;
                                                                // TODO: cambiato
                                                                // per provare!!!
                                                                // verifica = false;
                                                                dialog.dismiss();
                                                                Intent i = new Intent(
                                                                        getApplicationContext(),
                                                                        FormHierarchyActivity.class);
                                                                startActivity(i);
                                                            }
                                                        }
                                                );
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                        arrTrue = false;
                                        break;
                                    }
                                }
                                if (arrTrue) {
                                    FormListSavedActivity fm = new FormListSavedActivity();
                                /*LL 14-05-2014 spostata per dismissione del db grasp
                                DatabaseHelper dbh = new DatabaseHelper(
										"forms.db");
								String updatequery = "UPDATE forms SET status='completed' WHERE displayNameInstance = '"
										+ formNameInstance
										+ "' AND status='saved'";
								dbh.getReadableDatabase().execSQL(updatequery);*/

                                    // -----------------------------------------------------
                                    FormListActivity f = new FormListActivity();

                                    /**
                                     *  GESTIONE DELLA DATA
                                     *  gestione del mese
                                     */
                                    // --------------------------------------------------------------------------------------
                                    Calendar rightNow = Calendar.getInstance();
                                    java.text.SimpleDateFormat month = new java.text.SimpleDateFormat(
                                            "MM");
                                    // ----------------------------------------------------------------------------------------

                                    /**
                                     *  data di importazione
                                     */
                                    GregorianCalendar gc = new GregorianCalendar();
                                    String day = Integer.toString(gc
                                            .get(Calendar.DAY_OF_MONTH));
                                    String year = Integer.toString(gc
                                            .get(Calendar.YEAR));

                                    String data = day + "/"
                                            + month.format(rightNow.getTime())
                                            + "/" + year;

                                    String time = getCurrentTimeStamp();

                                    data = data + "  " + time;

                                    DatabaseHelper dbh = new DatabaseHelper(
                                            "forms.db");
                                    String updatequery = "UPDATE forms SET status='completed', completedDate = '" + data + "' WHERE displayNameInstance = '"
                                            + formNameInstance
                                            + "' AND status='saved'";
                                    dbh.getReadableDatabase().execSQL(updatequery);

								/*LL 14-05-2014 eliminato per dismissione del db grasp
                                try {
									f.updateFormsDataToCompleted(formNameInstance,data, getCompletedEnumeratorID(), formIdDataBase); //LL aggiunto reperimento enumeratorID e formIdDataBase per poterla cancellare dalle salvate e metterla nelle complete
									
									//f.updateFormsDataToCompleted(formNameInstance,data, "");//------------------------------------------------------------> qui non ci viene messo il EnumeratorID
									
									Log.i("updateFormsDataToCompleted",getCompletedEnumeratorID());
								}
								catch (XPathExpressionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								catch (ParserConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								catch (SAXException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								// -----------------------------------------------------*/
                                    dbh.close();
                                    finishReturnInstance();
                                }
                                arrTrue = true;
                            }
                        });
                return endView;

            case FormEntryController.EVENT_QUESTION:


                /**
                 * CODE FOR THE GLOBAL MANAGE OF THE FORM
                 * should only be a group hemre if the event_group is a field-list
                 */
            case FormEntryController.EVENT_GROUP:
                try {

                    if (odkv == null || toBeRecreated) {
                        if (odkv != null) {
                            //it keeps the keyboard closed
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(odkv.getWindowToken(), 0);
                        }


                        odkv = new ODKView(context,
                                mFormController.getQuestionPrompts(),  //insieme formato dai formEntryPrompt
                                mFormController.getGroupsForCurrentIndex(),
                                mFormController.getQuestionPrompt(index));

                        // Makes a "clear answer" menu pop up on long-click
                        for (QuestionWidget qw : odkv.getWidgets()) {

                            registerForContextMenu(qw);


                            // *********************** 12/11/2013
                        /*
                         * COMMENTATO PER PERMETTERE L'INFLATE DI UNA VIEW
						 * PERSONALIZZATA CHE NON PASSA IN JAVAROSA (SingleLine
						 * e MultiLine in Roster) if
						 * (!qw.getPrompt().isReadOnly()) {
						 * registerForContextMenu(qw); }
						 */
                            // ************************
                        }

                        Log.e("NUMERO DI ELEMENTI", "" + odkv.getWidgets().size());
                        Log.i(t, "created view for group");
                    } else if (odkv != null) {
                        this.checkVisibility(mFormController.getQuestionPrompts());
                        Log.e("NUM ELEMENT DOPO CHECK VISIBILITY", "" + mFormController.getQuestionPrompts().length);
                    }
                } catch (RuntimeException e) {
                    //createErrorDialog(e.getMessage()+"e1 "+((Integer)event).toString()+ "  " + index.getLocalIndex(), EXIT);
                    createErrorDialog(e.getMessage() + "e1 " + ((Integer) event).toString() + "  " + index.getLocalIndex(), false);
                    e.printStackTrace();
                    // this is badness to avoid a crash.
                    // really a next view should increment the formcontroller,
                    // create the view
                    // if the view is null, then keep the current view and pop an
                    // error.
                    return new View(this);
                }
                return odkv;

            default:
                Log.e(t, "Attempted to create a view that does not exist.");
                return null;
        }
    }


    /**
     * set a flag to avoid the refresh of the view in roster case
     *
     * @param questionPrompts this param contains all the information you need to display a question when
     *                        your current FormIndex references a QuestionEvent
     */
    private void checkVisibility(FormEntryPrompt[] questionPrompts) {
        ConstantUtility.setFlagCalculated("si");//just for some bugs on the rosters  (does not permit the refresh of the view)
        odkv.refreshVisibility(questionPrompts);
        ConstantUtility.setFlagCalculated("no");//just for some bugs on the rosters (flag resetted)
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent mv) {
        boolean handled = mGestureDetector.onTouchEvent(mv);
        if (!handled) {
            return super.dispatchTouchEvent(mv);
        }
        return handled; // this is always true
    }

    /**
     * Determines what should be displayed on the screen. Possible options are:
     * a question, an ask repeat dialog, or the submit screen. Also saves
     * answers to the data model after checking constraints.
     */
    public void showNextView() {
        // verifica = true;
        // TODO: tolto per provare
        verifica = true;
        radioFirstCheck = true;

        if (currentPromptIsQuestion()) {
            if (!saveAnswersForCurrentScreen(EVALUATE_CONSTRAINTS)) {
                // A constraint was violated so a dialog should be showing.
                return;
            }
        }
        if (mFormController.getEvent() != FormEntryController.EVENT_END_OF_FORM) {
            int event;

            group_skip:
            do {
                event = mFormController
                        .stepToNextEvent(FormController.STEP_INTO_GROUP); // repetition event there is a roster

                switch (event) {
                    case FormEntryController.EVENT_QUESTION:
                    case FormEntryController.EVENT_END_OF_FORM:
                        View next = null;
                        next = createView(event, null, true);
                        // next.requestFocus(View.FOCUS_UP);

                        showView(next, AnimationType.RIGHT, null);

                        break group_skip;
                    case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                        createRepeatDialog();
                        break group_skip;
                    case FormEntryController.EVENT_GROUP:
                        /**
                         *  CREATE THE VIEW
                         */
                        if (mFormController.indexIsInFieldList()
                                && mFormController.getQuestionPrompts().length != 0) {
                            View nextGroupView = createView(event, null, true);
                            nextGroupView.requestFocus(View.FOCUS_UP);
                            showView(nextGroupView, AnimationType.RIGHT, null);

                            if (PreferencesActivity.TO_SAVE_FORM) {
                                saveDataToDisk(DO_NOT_EXIT, isInstanceComplete(false), null);

                                break group_skip;
                            } else
                                break group_skip;
                        }
                        /**
                         *  otherwise it's not a field-list group, so just skip it
                         */
                        break;
                    case FormEntryController.EVENT_REPEAT:
                        Log.i(t, "repeat: "
                                + mFormController.getFormIndex().getReference());
                        FormEntryActivity.ROSTER = true;
                        /**
                         *  skip repeats
                         */
                        break;
                    case FormEntryController.EVENT_REPEAT_JUNCTURE:
                        Log.i(t, "repeat juncture: "
                                + mFormController.getFormIndex().getReference());
                        /**
                         *  skip repeat junctures until we implement them
                         */
                        break;
                    default:
                        Log.w(t,
                                "JavaRosa added a new EVENT type and didn't tell us... shame on them.");
                        break;
                }
            } while (event != FormEntryController.EVENT_END_OF_FORM);

        } else {
            mBeenSwiped = false;
        }
    }

    /**
     * screen and displays after a back swip, the appropriate view.
     * Also saves answers to the data model without checking constraints.
     */
    private void showPreviousView() {
        /**
         * The answer is saved on a back swipe, but question constraints are
         * ignored.
         */
        verifica = true;
        radioFirstCheck = true; // controllo sui radioButton

        // TODO: tolto per provare
        // verifica = false;

        if (!saveAnswersForCurrentScreen(EVALUATE_CONSTRAINTS)) {
            /**
             *  A constraint was violated so a dialog should be showing.
             */
            return;
        }
        if (mFormController.getEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM) {
            int event = mFormController.stepToPreviousEvent();

            while (event != FormEntryController.EVENT_BEGINNING_OF_FORM
                    && event != FormEntryController.EVENT_QUESTION
                    && !(event == FormEntryController.EVENT_GROUP
                    && mFormController.indexIsInFieldList() && mFormController
                    .getQuestionPrompts().length != 0)) {
                event = mFormController.stepToPreviousEvent();
            }
            View next = createView(event, null, true);
            showView(next, AnimationType.LEFT, null);
        } else {
            mBeenSwiped = false;
        }
    }

    /**
     * Displays the View specified by the parameter 'next', animating both the
     * current view and next appropriately given the AnimationType. Also updates
     * the progress bar.
     *
     * @param index Is a FormIndex. A FormIndex is an object used to provide information
     *              about the current index of the form, and thanks to it browse through
     *              the form and skip to the right page
     */
    public void showView(View next, AnimationType from, FormIndex index) {

        switch (from) {
            case RIGHT:
                mInAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_left_in);
                mOutAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_left_out);
                break;
            case LEFT:
                mInAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_right_in);
                mOutAnimation = AnimationUtils.loadAnimation(this,
                        R.anim.push_right_out);
                break;
            case FADE:
                mInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                mOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                break;
        }

        if (mCurrentView != null) {
            mCurrentView.startAnimation(mOutAnimation);
            if (!mCurrentView.equals(next)) {
                ConstantUtility.setFlagCalculated("si");//just for the bug in the rosters LL 23-01-14
                mLinearLayout.removeView(mCurrentView);
                ConstantUtility.setFlagCalculated("no");//only for bug in the calculated field, I have to turn it off in case we don't do it before come back here
            }
        }

        mInAnimation.setAnimationListener(this);

        if (mCurrentView == null || !mCurrentView.equals(next)) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            mCurrentView = next;


            mLinearLayout.addView(mCurrentView, lp);
        }
        mCurrentView.startAnimation(mInAnimation);

        // InputMethodManager imm = (InputMethodManager)
        // getSystemService(Context.INPUT_METHOD_SERVICE);
        // imm.hideSoftInputFromWindow(mCurrentView.getWindowToken(), 0);
        // imm.isWatchingCursor(mCurrentView);
        // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (index != null) {
            String[] indexprompt = index.toString().replace(" ", "").split(",");
            if (indexprompt.length == 3) {
                indexcurrent = Integer.parseInt(indexprompt[2]);
            } else {
                indexcurrent = Integer.parseInt(indexprompt[1]);
            }
        }
        // if (mCurrentView instanceof ODKView) {
        // ((ODKView) mCurrentView).setFocus(getApplicationContext());
        // // imm =
        // //
        // (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        // // imm.showSoftInput(mCurrentView, 0);
        // }
        // else {
        // InputMethodManager inputManager = (InputMethodManager)
        // getSystemService(Context.INPUT_METHOD_SERVICE);
        // inputManager.hideSoftInputFromWindow(mCurrentView.getWindowToken(),
        // 0);
        // }
    }

    /**
     * Ideally, we'd like to use Android to manage dialogs with onCreateDialog()
     * and onPrepareDialog(), but dialogs with dynamic content are broken in 1.5
     * (cupcake). We do use managed dialogs for our static loading
     * ProgressDialog. The main issue we noticed and are waiting to see fixed
     * is: onPrepareDialog() is not called after a screen orientation change.
     * http://code.google.com/p/android/issues/detail?id=1639
     */
    /**
     * Creates and displays a dialog displaying the violated constraint.
     */
    public void createConstraintToast(String constraintText, int saveStatus) {
        switch (saveStatus) {
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
                if (constraintText == null) {
                    constraintText = getString(R.string.invalid_answer_error);
                }
                break;
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                constraintText = getString(R.string.required_answer_error);
                break;
        }
        showCustomToast(constraintText, Toast.LENGTH_SHORT);
        mBeenSwiped = false;
    }

    /**
     * Creates a toast with the specified message.
     *
     * @param message string to show as message
     */
    private void showCustomToast(String message, int duration) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.toast_view, null);
        // set the text in the view
        TextView tv = (TextView) view.findViewById(R.id.message);
        tv.setText(message);
        Toast t = new Toast(this);
        t.setView(view);
        t.setDuration(duration);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }

    /**
     * Creates and displays a dialog asking the user if they'd like to create a
     * repeat of the current group.
     */
    private void createRepeatDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        DialogInterface.OnClickListener repeatListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        // yes, repeat
                        try {
                            QuestionWidget.clearColorLabelStoredForRequiredCheckBox();
                            mFormController.newRepeat();
                        } catch (XPathTypeMismatchException e) {
                            FormEntryActivity.this.createErrorDialog(
                                    e.getMessage(), EXIT);
                            return;
                        }
                        showNextView();
                        break;
                    case DialogInterface.BUTTON2:
                        // no, no repeat
                        showNextView();
                        break;
                }
            }
        };
        if (mFormController.getLastRepeatCount() > 0) {
            mAlertDialog.setTitle(getString(R.string.leaving_repeat_ask));
            mAlertDialog.setMessage(getString(R.string.add_another_repeat,
                    mFormController.getLastGroupText()));
            mAlertDialog.setButton(getString(R.string.add_another),
                    repeatListener);
            mAlertDialog.setButton2(getString(R.string.leave_repeat_yes),
                    repeatListener);
        } else {
            mAlertDialog.setTitle(getString(R.string.entering_repeat_ask));
            mAlertDialog.setMessage(getString(R.string.add_repeat,
                    mFormController.getLastGroupText()));
            mAlertDialog.setButton(getString(R.string.entering_repeat),
                    repeatListener);
            mAlertDialog.setButton2(getString(R.string.add_repeat_no),
                    repeatListener);
        }
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
        mBeenSwiped = false;
    }

    /**
     * Creates and displays dialog with the given errorMsg.
     */
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        mErrorMessage = errorMsg;
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setTitle(getString(R.string.error_occured));
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }

    /**
     * Creates a confirm/cancel dialog for deleting repeats.
     */
    private void createDeleteRepeatConfirmDialog() {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        String name = mFormController.getLastRepeatedGroupName();
        int repeatcount = mFormController.getLastRepeatedGroupRepeatCount();

        if (repeatcount != -1) {
            name += " (" + (repeatcount + 1) + ")";
        }
        mAlertDialog.setTitle(getString(R.string.delete_repeat_ask));
        mAlertDialog
                .setMessage(getString(R.string.delete_repeat_confirm, name));
        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        // yes
                        mFormController.deleteRepeat();
                        showPreviousView();
                        break;
                    case DialogInterface.BUTTON2:
                        // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.discard_group), quitListener);
        mAlertDialog.setButton2(getString(R.string.delete_repeat_no),
                quitListener);
        mAlertDialog.show();
    }

    /**
     * Saves data and writes it to disk. If exit is set, program will exit after
     * save completes. Complete indicates whether the user has marked the
     * isntancs as complete. If updatedSaveName is non-null, the instances
     * content provider is updated with the new name
     */
    private boolean saveDataToDisk(boolean exit, boolean complete,
                                   String updatedSaveName) {
        // save current answer

        if (!saveAnswersForCurrentScreen(complete)) {
            Toast.makeText(this, getString(R.string.data_saved_error),
                    Toast.LENGTH_SHORT).show();

            return false;
        }

        // Scrittura del xml su sd card e salvataggio
        // try{
        // Uri form = getIntent().getData();
        // String filepath = form.getPath();
        // File myFile = new File(filepath+".xml");
        // FileOutputStream fOut = new FileOutputStream(myFile);
        // OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
        // myOutWriter.close();
        // fOut.close();
        // }catch (Exception e){
        // e.printStackTrace();
        // }
        // mSaveToDiskTask = new SaveToDiskTask(getIntent().getData(), exit,
        // complete, updatedSaveName);
        updatedSaveName = formNameInstance;
        Uri instanceUri = Uri.parse(Collect.INSTANCES_PATH);
        Log.i("inSaveDataToDisk", "1");
        mSaveToDiskTask = new SaveToDiskTask(instanceUri, exit, complete,
                updatedSaveName);
        mSaveToDiskTask.setFormSavedListener(this);
        mSaveToDiskTask.execute();
        showDialog(SAVING_DIALOG);
        return true;
    }

    /**
     * Create a dialog with options to save and exit, save, or quit without
     * saving
     */
    private void createQuitDialog() {
        String[] items = {getString(R.string.keep_changes),
                getString(R.string.do_not_save)};
        mAlertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(
                        getString(R.string.quit_application,
                                mFormController.getFormTitle())
                )
                .setNeutralButton(getString(R.string.do_not_exit),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                )
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // save and exit
                                saveDataToDisk(EXIT, isInstanceComplete(false),
                                        null);
                            case 1:
                                // discard changes and exit
                                String selection = FormsColumns.INSTANCE_FILE_PATH
                                        + " like '" + mInstancePath + "'";

                                Cursor c = FormEntryActivity.this.managedQuery(
                                        FormsColumns.CONTENT_URI, null, selection,
                                        null, null);

                                // if it's not already saved, erase everything
                                if (c.getCount() < 1) {
                                    int images = 0;
                                    int audio = 0;
                                    int video = 0;

                                    // delete media first
                                    String instanceFolder = mInstancePath.substring(
                                            0, mInstancePath.lastIndexOf("/") + 1);
                                    Log.i(t, "attempting to delete: "
                                            + instanceFolder);

                                    String where = Images.Media.DATA + " like '"
                                            + instanceFolder + "%'";
                                    String[] projection = {Images.ImageColumns._ID};

                                    File f = new File(instanceFolder);
                                    if (f.exists() && f.isDirectory()) {
                                        for (File del : f.listFiles()) {
                                            Log.i(t,
                                                    "deleting file: "
                                                            + del.getAbsolutePath()
                                            );
                                            del.delete();
                                        }
                                        f.delete();
                                    }
                                }
                                finishReturnInstance();
                                break;

                            case 2:
                                // do nothing
                                break;
                        }
                    }
                }).create();
        mAlertDialog.show();
    }

    /**
     * Confirm clear answer dialog
     *
     * @param qw is a QuestionWidget. A QuestionWidget is an object that describe
     *           the physical layout of a question.
     */
    private void createClearDialog(final QuestionWidget qw) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setTitle(getString(R.string.clear_answer_ask));
        String question = qw.getPrompt().getLongText();
        if (question.length() > 50) {
            question = question.substring(0, 50) + "...";
        }
        mAlertDialog.setMessage(getString(R.string.clearanswer_confirm,
                question));

        DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        // yes
                        clearAnswer(qw);
                        saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                        break;
                    case DialogInterface.BUTTON2:
                        // no
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog
                .setButton(getString(R.string.discard_answer), quitListener);
        mAlertDialog.setButton2(getString(R.string.clear_answer_no),
                quitListener);
        mAlertDialog.show();
    }

    /**
     * Creates and displays a dialog allowing the user to set the language for
     * the form.
     */
    private void createLanguageDialog() {
        final String[] languages = mFormController.getLanguages();
        int selected = -1;
        if (languages != null) {
            String language = mFormController.getLanguage();
            for (int i = 0; i < languages.length; i++) {
                if (language.equals(languages[i])) {
                    selected = i;
                }
            }
        }
        mAlertDialog = new AlertDialog.Builder(this)
                .setSingleChoiceItems(languages, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                // Update the language in the content provider
                                // when selecting a new
                                // language
                                ContentValues values = new ContentValues();
                                values.put(FormsColumns.LANGUAGE,
                                        languages[whichButton]);
                                String selection = FormsColumns.FORM_FILE_PATH
                                        + "=?";
                                String selectArgs[] = {mFormPath};
                                int updated = getContentResolver().update(
                                        FormsColumns.CONTENT_URI, values,
                                        selection, selectArgs);
                                Log.i(t, "Updated language to: "
                                        + languages[whichButton] + " in "
                                        + updated + " rows");

                                mFormController
                                        .setLanguage(languages[whichButton]);
                                dialog.dismiss();
                                if (currentPromptIsQuestion()) {
                                    saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
                                }
                                refreshCurrentView(null);
                            }
                        }
                )
                .setTitle(getString(R.string.change_language))
                .setNegativeButton(getString(R.string.do_not_change),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        }
                ).create();
        mAlertDialog.show();
    }

    /**
     * Adds the "Go To Index" item to the option menu, accessible only during the compilation of a form.
     * This item allows the user to see in a sort of preview mode, the current compilation of the form with
     * all its questions and the relative answers, if there are
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final Intent i = new Intent(this, FormHierarchyActivity.class);
        menu.add("Go To Index").setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        verifica = true;
                        // TODO: cambiato per provare!!!
                        // verifica = false;
                        fromHyera = true;
                        refreshCurrentView(null);
                        saveAnswersForCurrentScreen(true);
                        startActivity(i);
                        return true;
                    }
                }
        );
        ;
        // menu.add("Resfresh").setOnMenuItemClickListener(new
        // OnMenuItemClickListener() {
        // public boolean onMenuItemClick(MenuItem item) {
        // saveAnswersForCurrentScreen(EVALUATE_CONSTRAINTS);
        // int event = mFormController.getEvent();
        // View next = createView(event, null);
        // showView(next, AnimationType.FADE, null);
        // return true;
        // }
        // });
        return true;
    }

    /**
     * We use Android's dialog management for loading/saving progress dialogs
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mFormLoaderTask.setFormLoaderListener(null);
                        mFormLoaderTask.cancel(true);
                        finish();
                    }
                };
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setTitle(getString(R.string.loading_form));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel_loading_form),
                        loadingButtonListener);
                return mProgressDialog;
            case SAVING_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener savingButtonListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mSaveToDiskTask.setFormSavedListener(null);
                        mSaveToDiskTask.cancel(true);
                    }
                };
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setTitle(getString(R.string.saving_form));
                mProgressDialog.setMessage(getString(R.string.please_wait));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel),
                        savingButtonListener);
                mProgressDialog.setButton(getString(R.string.cancel_saving_form),
                        savingButtonListener);
                return mProgressDialog;
        }
        return null;
    }

    /**
     * Dismiss any showing dialogs that we manually managed.
     */
    private void dismissDialogs() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    /**
     * when the activity goes in pause state, we save all the questions that are
     * been just answered, in the current screen
     */
    protected void onPause() {
        dismissDialogs();
        // make sure we're not already saving to disk. if we are, currentPrompt
        // is getting constantly updated
        if (mSaveToDiskTask != null
                && mSaveToDiskTask.getStatus() != AsyncTask.Status.RUNNING) {
            if (mCurrentView != null && currentPromptIsQuestion()) {
                saveAnswersForCurrentScreen(DO_NOT_EVALUATE_CONSTRAINTS);
            }
        }
        super.onPause();
    }


    /**
     * When the activity has to be shown, we check if it is just a flip or a new form loading.
     * if it is a new form loading, already exist a model for the form and the task for the
     * form loading has done, then we can refresh the current view
     * <p/>
     * if is is a flip we set the setFomrSavedListener
     */
    protected void onResume() {
        super.onResume();

        Log.e("- onResume() -", "effettuato onResume() da FormEntryActivity");
        if (mFormLoaderTask != null) {
            mFormLoaderTask.setFormLoaderListener(this);
            if (mFormController != null
                    && mFormLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                dismissDialog(PROGRESS_DIALOG);

                refreshCurrentView(null);
            }
        }
        if (mSaveToDiskTask != null) {
            mSaveToDiskTask.setFormSavedListener(this);
        }
        if (mErrorMessage != null
                && (mAlertDialog != null && !mAlertDialog.isShowing())) {
            createErrorDialog(mErrorMessage, EXIT);
            return;
        }
    }


    /**
     * When the user clicks on the back button it is shown a dialog
     * to make sure about the real intentions of the user.
     * On click "yes" button the current answers are saved on the disk
     * and the user goes back to the list of the forms
     * On click "no" button, the dialog is simply closed
     *
     * @param keyCode A key code that represents the button pressed, from KeyEvent
     * @param event   The KeyEvent object that defines the button action
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // createQuitDialog();
                if (PreferencesActivity.TO_SAVE_FORM) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            FormEntryActivity.this);
                    builder.setTitle(R.string.exit_dialog_title_form)
                            .setIcon(R.drawable.icona_app_wfp)
                            .setMessage(R.string.exit_dialog_form)
                            .setPositiveButton(R.string.confirm,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int arg1) {
                                            Log.i("clickOKsuDialog", "1");
                                            dialog.dismiss();
                                            saveDataToDisk(EXIT,
                                                    isInstanceComplete(false), null);
                                        }
                                    }
                            )
                            .setNegativeButton(getString(R.string.negative),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int id) {
                                            dialog.dismiss();
                                        }
                                    }
                            );
                    builder.show();
                }

			/*
			 * else { String selection = FormsColumns.INSTANCE_FILE_PATH +
			 * " like '" + mInstancePath + "'"; Cursor c =
			 * FormEntryActivity.this.managedQuery( FormsColumns.CONTENT_URI,
			 * null, selection, null, null); // if it's not already saved, erase
			 * everything if (c.getCount() < 1) { int images = 0; int audio = 0;
			 * int video = 0; // delete media first String instanceFolder =
			 * mInstancePath.substring(0, mInstancePath.lastIndexOf("/") + 1);
			 * Log.i(t, "attempting to delete: " + instanceFolder); String where
			 * = Images.Media.DATA + " like '" + instanceFolder + "%'"; String[]
			 * projection = { Images.ImageColumns._ID }; File f = new
			 * File(instanceFolder); if (f.exists() && f.isDirectory()) { for
			 * (File del : f.listFiles()) { Log.i(t, "deleting file: " +
			 * del.getAbsolutePath()); del.delete(); } f.delete(); } }
			 * finishReturnInstance(); return true; } case
			 * KeyEvent.KEYCODE_DPAD_RIGHT: if (event.isAltPressed() &&
			 * !mBeenSwiped) { mBeenSwiped = true; showNextView(); return true;
			 * } break; case KeyEvent.KEYCODE_DPAD_LEFT: if
			 * (event.isAltPressed() && !mBeenSwiped) { mBeenSwiped = true;
			 * showPreviousView(); return true; } break; }
			 */
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * When the Activity is distroyed we have to termine and destroy all the
     * persistent objects as the fec, in order to be sure next time
     * we will have all clean objects
     */
    @Override
    protected void onDestroy() {
        if (mFormLoaderTask != null) {
            mFormLoaderTask.setFormLoaderListener(null);
            // We have to call cancel to terminate the thread, otherwise it
            // lives on and retains the FEC in memory.
            // but only if it's done, otherwise the thread never returns
            if (mFormLoaderTask.getStatus() == AsyncTask.Status.FINISHED) {
                mFormLoaderTask.cancel(true);
                mFormLoaderTask.destroy();
            }
        }
        if (mSaveToDiskTask != null) {
            mSaveToDiskTask.setFormSavedListener(null);
            // We have to call cancel to terminate the thread, otherwise it
            // lives on and retains the FEC in memory.
            if (mSaveToDiskTask.getStatus() == AsyncTask.Status.FINISHED) {
                mSaveToDiskTask.cancel(false);
            }
        }
        super.onDestroy();
    }

    /**
     * called when the swipe animation is done
     */
    @Override
    public void onAnimationEnd(Animation arg0) {
        mBeenSwiped = false;
    }

    /**
     * not used
     */
    @Override
    public void onAnimationRepeat(Animation animation) {
        // Added by AnimationListener interface.
    }

    /**
     * not used
     */
    @Override
    public void onAnimationStart(Animation animation) {
        // Added by AnimationListener interface.
    }

    /**
     * loadingComplete() is called by FormLoaderTask once it has finished
     * loading a form.
     *
     * @param fc is a FormController. A formController object is an object that set the form engine
     *           and wraps some object provided by FormEntryController
     */
    public void loadingComplete(FormController fc) {
        dismissDialog(PROGRESS_DIALOG);
        mFormController = fc;
        // Set saved answer path
        if (mInstancePath == null) {
            // Create new answer folder.
            String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
                    .format(Calendar.getInstance().getTime());
            String file = mFormPath.substring(mFormPath.lastIndexOf('/') + 1,mFormPath.lastIndexOf('.'));
            String path = Collect.INSTANCES_PATH + "/" + file + "_" + time;
            if (FileUtils.createFolder(path)) {
                mInstancePath = path + "/" + file + "_" + time + ".xml";
            }
        } else {
            // we've just loaded a saved form, so start in the hierarchy view
            Intent i = new Intent(this, FormHierarchyActivity.class);
            // Intent i = new Intent(this, FormEntryActivity.class);
            startActivity(i);
            return; // so we don't show the intro screen before jumping to the
            // hierarchy
        }
        // Set the language if one has already been set in the past
        String[] languageTest = mFormController.getLanguages();
        if (languageTest != null) {
            String defaultLanguage = mFormController.getLanguage();
            String newLanguage = "";
            String selection = FormsColumns.FORM_FILE_PATH + "=?";
            String selectArgs[] = {mFormPath};
            Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, selection,
                    selectArgs, null);
            if (c.getCount() == 1) {
                c.moveToFirst();
                newLanguage = c.getString(c
                        .getColumnIndex(FormsColumns.LANGUAGE));
            }
            // if somehow we end up with a bad language, set it to the default
            try {
                mFormController.setLanguage(newLanguage);
            } catch (Exception e) {
                mFormController.setLanguage(defaultLanguage);
            }
        }
        // REFRESH DELLA VISTA PER LA PRIMA IMPAGINAZIONE
        refreshCurrentView(null);
    }

    /**
     * called by the FormLoaderTask if something goes wrong
     * during the form Loading from the disk.
     *
     * @param erroMsg the string to show as message
     */
    @Override
    public void loadingError(String errorMsg) {
        dismissDialog(PROGRESS_DIALOG);
        if (errorMsg != null) {
            createErrorDialog(errorMsg, EXIT);
        } else {
            createErrorDialog(getString(R.string.parse_error), EXIT);
        }
    }

    /**
     * Called by SavetoDiskTask if everything has been saved correctly.
     *
     * @param saveStatus it gives the result of the saving
     */
    @Override
    public void savingComplete(int saveStatus) {
        dismissDialog(SAVING_DIALOG);
        switch (saveStatus) {
            case SaveToDiskTask.SAVED:
                Toast.makeText(this, getString(R.string.data_saved_ok),
                        Toast.LENGTH_SHORT).show();
                break;
            case SaveToDiskTask.SAVED_AND_EXIT:
                Toast.makeText(this, getString(R.string.data_saved_ok),
                        Toast.LENGTH_SHORT).show();
                finishReturnInstance();
                break;
            case SaveToDiskTask.SAVE_ERROR:
                Toast.makeText(this, getString(R.string.data_saved_error),
                        Toast.LENGTH_LONG).show();
                break;
            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
                refreshCurrentView(null);
                // an answer constraint was violated, so do a 'swipe' to the next
                // question to display the proper toast(s)
                next();
                break;
        }
    }

    /**
     * Attempts to save an answer to the specified index.
     *
     * @param answer              the answer given from the user
     * @param index               Is a FormIndex. A FormIndex is an object used to provide information
     *                            about the current index of the form, and thanks to it browse through
     *                            the form and skip to the right page
     * @param evaluateConstraints says if it is needed to check the constraint or not
     * @return status as determined in FormEntryController
     */
    public int saveAnswer(IAnswerData answer, FormIndex index,
                          boolean evaluateConstraints) {
        int result = 0;
        if (evaluateConstraints) {
            // inserito il 19/09
            // -----------------
            result = mFormController.answerQuestion(index, answer);
            // if(result == FormEntryController.ANSWER_OK){
            // mFormController.saveAnswer(index, answer);
            // }
            return result;
        } else {
            mFormController.saveAnswer(index, answer);
            return FormEntryController.ANSWER_OK;
        }
    }

    /**
     * Checks the database to determine if the current instance being edited has
     * already been 'marked completed'. A form can be 'unmarked' complete and
     * then resaved.
     *
     * @param end says if we are at the end of the form or not
     * @return true if form has been marked completed, false otherwise.
     */
    private boolean isInstanceComplete(boolean end) {
        // default to false if we're mid form
        boolean complete = false;
        // if we're at the end of the form, then check the preferences
        if (end) {
            // First get the value from the preferences
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(this);
            // complete =
            // sharedPreferences.getBoolean(SettingsActivity.KEY_COMPLETED_DEFAULT,
            // true);
        }
        // Then see if we've already marked this form as complete before
        String selection = FormsColumns.INSTANCE_FILE_PATH + "=?";
        String[] selectionArgs = {mInstancePath};
        Cursor c = getContentResolver().query(FormsColumns.CONTENT_URI, null,
                selection, selectionArgs, null);
        startManagingCursor(c);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String status = c.getString(c.getColumnIndex(FormsColumns.STATUS));
            if (FormProviderAPI.STATUS_COMPLETED.compareTo(status) == 0) {
                complete = true;
            }
        }
        return complete;
    }

    /**
     * called when we have to show the next page of questions
     */
    public void next() {
        if (!mBeenSwiped) {
            mBeenSwiped = true;
            showNextView();
        }
    }

    /**
     * Returns the instance that was just filled out to the calling activity, if
     * requested.
     */
    private void finishReturnInstance() {
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action)
                || Intent.ACTION_EDIT.equals(action)) {
            // caller is waiting on a picked form
            String selection = FormsColumns.INSTANCE_FILE_PATH + "=?";
            String[] selectionArgs = {mInstancePath};
            Cursor c = managedQuery(FormsColumns.CONTENT_URI, null, selection,
                    selectionArgs, null);
            if (c.getCount() > 0) {
                // should only be one...
                c.moveToFirst();
                String id = c.getString(c.getColumnIndex(FormsColumns._ID));
                Uri instance = Uri.withAppendedPath(FormsColumns.CONTENT_URI,
                        id);
                setResult(RESULT_OK, new Intent().setData(instance));
            }
        }
        QuestionWidget.clearColorLabelStoredForRequiredCheckBox();
        finish();
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    /**
     * check the swipe event, used by the user to pass  from a page to another
     * @return true if form has been swiped.
     */
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {


        // Looks for user swipes. If the user has swiped, move to the
        // appropriate screen.
        // for all screens a swipe is left/right of at least
        // .25" and up/down of less than .25"
        // OR left/right of > .5"
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int xPixelLimit = (int) (dm.xdpi * .25);
        int yPixelLimit = (int) (dm.ydpi * .25);
        if ((Math.abs(e1.getX() - e2.getX()) > xPixelLimit && Math.abs(e1
                .getY() - e2.getY()) < yPixelLimit)
                || Math.abs(e1.getX() - e2.getX()) > xPixelLimit * 2) {
            if (velocityX > 0) {
                mBeenSwiped = true;
                // storeCurrentWidgetsLayout();

                mCurrentView.clearFocus();
                // mCurrentView.invalidate();
                // mCurrentView.setDrawingCacheEnabled( false );

                showPreviousView();
                return true;
            } else {
                mBeenSwiped = true;
                // storeCurrentWidgetsLayout();
                mCurrentView.clearFocus();
                // mCurrentView.invalidate();
                // mCurrentView.setDrawingCacheEnabled( false );
                showNextView();
                return true;
            }
        }
        return false;
    }

    // private void storeCurrentWidgetsLayout(){
    // List<ColorHelper> colors = new ArrayList<ColorHelper>();
    // if(this.odkv != null && !this.odkv.getWidgets().isEmpty()){
    // for (QuestionWidget qws : this.odkv.getWidgets()) {
    // ColorHelper helper = new ColorHelper(qws.getContext(),
    // qws.getResources());
    // FormEntryActivity.colorHelper.add(helper);
    // }
    // }
    // }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    /**
     * The onFling() captures the 'up' event so our view thinks it gets long
     * pressed.
     * We don't want that, so cancel it.
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        // The onFling() captures the 'up' event so our view thinks it gets long
        // pressed.
        // We don't wnat that, so cancel it.
        if (mCurrentView != null)
            mCurrentView.cancelLongPress();
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void advance() {
        next();
    }

    /**
     * called to get the identifier of the person that has compiled the form, directly from the xml
     *
     * @return author the identifier as a string
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws XPathExpressionException
     */
    private String getCompletedEnumeratorID() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        String substr = "";
        String author = "";
        File fXmlFile = new File(mInstancePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression inputcount = xpath.compile("//data/*");
        NodeList nodes = (NodeList) inputcount.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            substr = (nodes.item(i).getTextContent());
            String nodename = (nodes.item(i).getNodeName());
            if (nodename.equalsIgnoreCase("enumerator_1")) {
                author = substr;
            }
        }
        return author;

    }

    /**
     * called to get the current timestemp
     *
     * @return the timestamp as a string
     */
    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date()); // Find todays date

            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

}