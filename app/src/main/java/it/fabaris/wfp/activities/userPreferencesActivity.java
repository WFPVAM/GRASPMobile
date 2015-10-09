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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.task.HttpCheckPostTask;
import it.fabaris.wfp.utility.ConstantUtility;
import utils.ApplicationExt;

/**
 * Class that manage the preferences of the app
 */
public class userPreferencesActivity extends PreferenceActivity  {


    public static String KEY_BUTTON_CHECK = "button_check_conn";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.user_settings);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.settings));


        Preference buttonCheckConn = (Preference) findPreference(KEY_BUTTON_CHECK);
        buttonCheckConn.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String httpServer = settings.getString(PreferencesActivity.KEY_SERVER_URL, getString(R.string.default_server_url));
                String numClient = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE, getString(R.string.default_client_telephone));

                //SCELTA CONNESSIONE A RICHIESTA
                String onRequest = settings.getString(PreferencesActivity.KEY_REQUEST_CHOISE, getString(R.string.on_request));

                String http = httpServer + "/test";
                String phone = numClient;
                String data = "test";
                HttpCheckPostTask asyncTask = new HttpCheckPostTask(userPreferencesActivity.this, http, phone, data);
                asyncTask.execute();
                return true;
            }

        });

    }


    /**
     * while leaving the activity, unregister the SharedPreferencesListener
     */
    @Override
    protected void onPause() {
        super.onPause();
       // getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * while resuming the activity, register the SharedPreferencesListener
     * in order to manages possible changes in the preferences
     */
    @Override
    protected void onResume() {
        super.onResume();

       // getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }


}