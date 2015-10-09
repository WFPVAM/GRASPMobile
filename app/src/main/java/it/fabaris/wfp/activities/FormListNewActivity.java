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
package it.fabaris.wfp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import content.FormNewAdapter;
import it.fabaris.wfp.provider.FormProvider;
import it.fabaris.wfp.provider.FormProviderAPI;
import object.FormInnerListProxy;

/**
 * Class that defines the tab for the list of the new forms
 *
 */
public class FormListNewActivity extends Activity
{
    public interface FormListHandlerNew
    {
        public ArrayList<FormInnerListProxy> getNewForm();
    }
    public FormListHandlerNew formListHandler;

    public String form;
    public String formInstance;
    public String formPath;
    public String formid;
    public FormNewAdapter adapter;

    private ArrayList<FormInnerListProxy> nuove;

    public static String portrait;
    private SharedPreferences settings;

    private static Notification notification;
    private NotificationManager nm;


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabnew);

        settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        portrait=settings.getString(PreferencesActivity.KEY_BUTTON_PORTRAIT,"");

        if(portrait.equalsIgnoreCase("enabled")){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        Log.i("inFormListNewActivity","1");

        nuove = new ArrayList<FormInnerListProxy>();
        nuove = getIntent().getExtras().getParcelableArrayList("new");

        final ListView listview = (ListView) findViewById(R.id.listViewNew);
        listview.setCacheColorHint(00000000);
        listview.setClickable(true);

        adapter = new FormNewAdapter(FormListNewActivity.this, nuove);
        listview.setAdapter(adapter);

        /**
         * when the user clicks on one of the items in the new forms list, then,
         * the FormEntryActivity is called and the user can start compiling the form
         */
        listview.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                try{
                    Context context = getApplicationContext();
                    Intent intent = new Intent (context, FormEntryActivity.class);
                    String keyIdentifer  = "ciao";
                    String keyIdentifer1  = "ciao1";
                    String keyIdentifer2  = "ciao2";
                    String keyIdentifer3  = "ciao3";
                    String pkgName = getPackageName();
                    formPath = nuove.get(position).getPathForm();
                    form = nuove.get(position).getFormName();
                    formid = nuove.get(position).getFormId();


                    Long time = System.currentTimeMillis();
                    time.toString();
                    formInstance = form+"_"+time;
                    intent.putExtra(pkgName+keyIdentifer, formPath);
                    intent.putExtra(pkgName+keyIdentifer1, form);
                    intent.putExtra(pkgName+keyIdentifer2, formInstance);
                    intent.putExtra(pkgName+keyIdentifer3, formid);

                    String action = getIntent().getAction();
                    if (Intent.ACTION_PICK.equals(action)) {
                        setResult(RESULT_OK, new Intent().setData(Uri.parse(nuove.get(position).getPathForm())));
                    }
                    else
                    {
                        intent.setAction(Intent.ACTION_EDIT);
                        String extension = MimeTypeMap.getFileExtensionFromUrl(nuove.get(position).getPathForm()).toLowerCase();
                        String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        intent.setDataAndType(FormProviderAPI.FormsColumns.CONTENT_URI, mimeType);
                        startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FormListNewActivity.this);
                builder.setMessage(getString(R.string.delete_form))
                        .setCancelable(false)
                        .setPositiveButton(R.string.confirm,
                                new DialogInterface.OnClickListener(){
                                    public void onClick(DialogInterface dialog, int id)
                                    {
							/*LL 14-05-2014 eliminato per dismissione del db grasp
							//int positionSalvati = getRightCompletedParcelableObject(saved.get(position).getFormName()); LL eliminato per dismissione del db grasp
							ApplicationExt.getDatabaseAdapter().open().delete("SAVED", saved.get(position).getFormName());
							ApplicationExt.getDatabaseAdapter().close();
							*/
                                        dialog.dismiss();
                                        FormProvider.DatabaseHelper dbh = new FormProvider.DatabaseHelper("forms.db");
                                        String query1 = "UPDATE forms SET status='cancelled' WHERE displayName = '"
                                                + nuove.get(position).getFormName()+"'";
                                               // + "' AND status='new',status='completed,status='saved'";

                                        dbh.getWritableDatabase().execSQL(query1);
                                        dbh.close();

                                        Toast.makeText(FormListNewActivity.this, getString(R.string.cancelform) + " " +nuove.get(position).getFormName(), Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                })
                        .setNegativeButton(getString(R.string.negative_choise),	new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog,	int id)
                            {
                                dialog.dismiss();
                            }
                        }).show();


                return false;
            }
        });
    }

    public void onResume()
    {
        super.onResume();
        //adapter =
        //adapter.notifyDataSetChanged();
    }

}
