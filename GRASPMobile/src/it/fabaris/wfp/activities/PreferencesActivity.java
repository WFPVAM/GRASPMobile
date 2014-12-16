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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import utils.ApplicationExt;

import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.task.HttpCheckPostTask;
import it.fabaris.wfp.utility.ConstantUtility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TimePicker;
/**
 * 
 * Class that manage the preferences of the app
 *
 */
public class PreferencesActivity extends PreferenceActivity implements
	OnSharedPreferenceChangeListener{
	
	public static String SERVER_ONLINE = "NO";
	public static String KEY_FONT_SIZE = "font_size";
	public static String KEY_SERVER_URL = "server_url";
	public static String KEY_REQUEST_CHOISE = "on_request";
	public static String APP_URL = "app_url";
	public static String KEY_SERVER_TELEPHONE = "server_telephone";
	public static String KEY_CLIENT_TELEPHONE = "client_telephone";
	public static String KEY_CONNECTION_TYPE = "select_type";
	public static String KEY_TIME_RANGE = "insert_time";
	public static String KEY_BUTTON_CHECK_NEW_APPLICATION = "button_check_new_app";
	public static String KEY_BUTTON_CHECK = "button_check_conn";
	public static String TEXT_BACKGROUND_COLOR = "textcolor_background";
	public static String TEXT_FOREGROUND_COLOR = "textcolor_foreground";
	public static String TEXT_MANDATORY_BACKGROUND_COLOR = "textcolor_mandatory_background";
	public static String TEXT_MANDATORY_FOREGROUND_COLOR = "textcolor_mandatory_foreground";
	public static String TEXT_ERROR_BACKGROUND_COLOR = "textcolor_error_background";
	public static String TEXT_ERROR_FOREGROUND_COLOR = "textcolor_error_foreground";
	
	public static String KEY_PROTOCOL = "list_protocol";
	public static String KEY_IP = "server_ip";
	public static String KEY_PORT = "server_port";
	
	
	
	
	
	
	public static boolean TO_SAVE_FORM = false;
	public static boolean verificaArray = false;
	private int mTimeRangeHour = 0;
	private int mTimeRangeMinute = 0;
	
	private EditTextPreference mServerTelephonePreference;
	private EditTextPreference mClientTelephonePreference;
	private EditTextPreference mServerUrlPreference;
	private Preference mTimeRangePreference;
	
	
	ProgressDialog mProgressDialog;
	
	private String protocol = new String();
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setTitle(getString(R.string.app_name) + " > " + getString(R.string.settings));
		
		final SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		
		//Progress dialog
		mProgressDialog = new ProgressDialog(PreferencesActivity.this);
		mProgressDialog.setMessage("Download...");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		//inizializzo i Summary dell'ip del protocol e della porta se sono state settate in precedenza
		String ip = settings.getString(PreferencesActivity.KEY_IP,null);
		String port = settings.getString(PreferencesActivity.KEY_PORT,null);
		
		
		if(ip != null){
			EditTextPreference mIpPreference = (EditTextPreference) findPreference(KEY_IP);
			mIpPreference.setSummary(mIpPreference.getText());
		}
		if(port != null){
			EditTextPreference mPortPreference = (EditTextPreference) findPreference(KEY_PORT);
			mPortPreference.setSummary(mPortPreference.getText());
		}
		
		
		
		//setto il valore del protocollo allo stesso valore scelto in MenuActivity
		//se e' stato impostato
			protocol = settings.getString(PreferencesActivity.KEY_PROTOCOL, null);
		ListPreference listprotocolo = (ListPreference) findPreference(KEY_PROTOCOL);
		if( protocol.equals("http") || protocol.equals("1") ){//metto a video http nel radio group
			listprotocolo.setValueIndex(0);
			listprotocolo.setSummary("http");
		}else{
			listprotocolo.setValueIndex(1);//metto a video https nel radio group
			listprotocolo.setSummary("https");
		}
	
		
		Preference buttonTimePreference = (Preference)findPreference(KEY_TIME_RANGE);
		buttonTimePreference.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference pref){
				AlertDialog.Builder builder = new AlertDialog.Builder(pref.getContext());
				final TimePicker timePick = new TimePicker(pref.getContext());
				timePick.setIs24HourView(true);
				timePick.setCurrentHour(mTimeRangeHour);
				timePick.setCurrentMinute(mTimeRangeMinute);
				timePick.clearFocus();
				final LinearLayout lila1 = new LinearLayout(pref.getContext());
				lila1.setOrientation(1);
				lila1.addView(timePick);
				builder.setView(lila1);
				builder.setMessage("Hours and Minutes before enabling re-submit");
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mTimeRangeHour = timePick.getCurrentHour();
						mTimeRangeMinute = timePick.getCurrentMinute();
						
					}
				});
				
				AlertDialog alert = builder.create();
				alert.show();
				
				return true;
			}
		});
		
		Preference buttonCheckConn = (Preference)findPreference(KEY_BUTTON_CHECK);
		buttonCheckConn.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference pref)
			{
				SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				String httpServer = settings.getString(PreferencesActivity.KEY_SERVER_URL, getString(R.string.default_server_url));
				String numClient = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE, getString(R.string.default_client_telephone));
				
				//SCELTA CONNESSIONE A RICHIESTA
				String onRequest = settings.getString(PreferencesActivity.KEY_REQUEST_CHOISE, getString(R.string.on_request));
				
				String http = httpServer+"/test";
				String phone = numClient;
				String data = "test";
				HttpCheckPostTask asyncTask = new HttpCheckPostTask(PreferencesActivity.this, http, phone, data);
				asyncTask.execute();
				return true;
			}
		
		});
		
		Preference buttonCheckNewApplication = (Preference)findPreference(KEY_BUTTON_CHECK_NEW_APPLICATION);
		buttonCheckNewApplication.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference pref)
			{
				SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
				String httpServer = settings.getString(PreferencesActivity.APP_URL, getString(R.string.new_app_url));
				
				DownloadFile downloadFile = new DownloadFile();
				downloadFile.execute(getString(R.string.new_app_url));
				return true;
			}
		});
		
		
		ListPreference protocolRadioButton = (ListPreference) findPreference(KEY_PROTOCOL);
		OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {    
		    @Override
		    public boolean onPreferenceChange(Preference preference, Object newValue) {
		        // newValue is the value you choose
		    	SharedPreferences.Editor editor = settings.edit();
		    	if(newValue.toString().equals("1")){
		    		editor.putString(PreferencesActivity.KEY_PROTOCOL, "http");
		    		
		    		String serverurl = new String();
		    		
					String ip = settings.getString(PreferencesActivity.KEY_IP, getString(R.string.server_IP));
					String port = settings.getString(PreferencesActivity.KEY_PORT, getString(R.string.server_port));
					
		    		editor.commit();
		    		serverurl = "http" + "://" + ip +":" + port;
		    		editor.putString(PreferencesActivity.KEY_SERVER_URL, serverurl);
		    		editor.commit();
		    		
		    		ListPreference listprotocolo = (ListPreference) findPreference(KEY_PROTOCOL);
		    		listprotocolo.setValueIndex(0);
		    		
		    		listprotocolo.setSummary("http");
		    		
		    	}else{
		    		editor.putString(PreferencesActivity.KEY_PROTOCOL, "https");
		    		String serverurl = new String();
					String ip = settings.getString(PreferencesActivity.KEY_IP, getString(R.string.server_IP));
					String port = settings.getString(PreferencesActivity.KEY_PORT, getString(R.string.server_port));
					editor.commit();
		    		serverurl = "https" + "://" + ip +":" + port;
		    		editor.putString(PreferencesActivity.KEY_SERVER_URL, serverurl);
		    		editor.commit();
		    		ListPreference listprotocolo = (ListPreference) findPreference(KEY_PROTOCOL);
		    		listprotocolo.setValueIndex(1);
		    		
		    		listprotocolo.setSummary("https");
		    		
		    	}
		    	
	    		
		        return false;
		    }
		};

		protocolRadioButton.setOnPreferenceChangeListener(listener);
	}
	
		
	
	@Override
	 protected void onPause() {
		 super.onPause();
		 getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	 }
	 
	 @Override
	 protected void onResume() {
		 super.onResume();
		 getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		 updateFontSize();
		 updateServerTelephone();
		 updateClientTelephone();
		 //updateServerUrl(); LL
		 updateConnectType();
	 }
	 
	 @Override
	 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		 /*if (key.equals(KEY_SERVER_URL)) {
			 updateServerUrl();    LL l'ho appena tolto io!!!
	     }else */if (key.equals(KEY_SERVER_TELEPHONE)) {
	         updateServerTelephone();
	     }else if (key.equals(KEY_CLIENT_TELEPHONE)) {
	         updateClientTelephone();
	     }else if (key.equals(KEY_FONT_SIZE)){
	    	 updateFontSize();
	     }else if(key.equals(KEY_CONNECTION_TYPE)){
	    	 updateConnectType();
	     }else if(key.equals(KEY_TIME_RANGE)){
	    	 updateTimeRange();
	     }else if(key.equals(KEY_IP)){
	    	 updateServerIP();
	     }else if(key.equals(KEY_PORT)){
	    	 updateServerPort();
	     }
	 }
	 
	 private String getProtocolFromRadioButton(String sceltaradio){
		 String protocollo = new String();
		 if(sceltaradio.equals("1")){
			 protocollo = "https";
			 ConstantUtility.protocol = "https";
		 }else{
			 protocollo = "http";
			 ConstantUtility.protocol = "http";
		 }
		 
		 return protocollo;
	}
	 
	 private void updateServerUrl() {
		 mServerUrlPreference = (EditTextPreference) findPreference(KEY_SERVER_URL);
		while (mServerUrlPreference.getText().endsWith("/")) {
		    mServerUrlPreference.setText(mServerUrlPreference.getText().substring(0,
		        mServerUrlPreference.getText().length() - 1));
		}
		mServerUrlPreference.setSummary(mServerUrlPreference.getText());
	 }
	 
	 
	 private void updateServerIP(){
		 
		//SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
			//String httpServerurl = settings.getString(PreferencesActivity.KEY_SERVER_URL, getString(R.string.server_url));
			
			EditTextPreference mIpPreference = (EditTextPreference) findPreference(KEY_IP);
			mIpPreference.setSummary(mIpPreference.getText());
			SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String serverurl = new String();
			String protoc = getProtocolFromRadioButton(getString(R.string.portocol_choice));
			String ip = settings.getString(PreferencesActivity.KEY_IP, getString(R.string.server_IP));
			String port = settings.getString(PreferencesActivity.KEY_PORT, getString(R.string.server_port));
			serverurl = protoc + "://" + ip +":" + port;  
			
			SharedPreferences.Editor editor = settings.edit();
	 		editor.putString(PreferencesActivity.KEY_SERVER_URL, serverurl);
	 		editor.commit();
		 
	 }
	 
	 private void updateServerPort(){
		 EditTextPreference mPortPreference = (EditTextPreference) findPreference(KEY_PORT);
		 mPortPreference.setSummary(mPortPreference.getText());
		SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String serverurl = new String();
		String protoc = getProtocolFromRadioButton(getString(R.string.portocol_choice));
		String ip = settings.getString(PreferencesActivity.KEY_IP, getString(R.string.server_IP));
		String port = settings.getString(PreferencesActivity.KEY_PORT, getString(R.string.server_port));
		serverurl = protoc + "://" + ip +":" + port;  
		
		SharedPreferences.Editor editor = settings.edit();
 		editor.putString(PreferencesActivity.KEY_SERVER_URL, serverurl);
 		editor.commit();
	 }
	 
	 private void updateServerTelephone() {
		mServerTelephonePreference = (EditTextPreference) findPreference(KEY_SERVER_TELEPHONE);
		mServerTelephonePreference.setSummary(mServerTelephonePreference.getText());
		mServerTelephonePreference.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
	 }
	 
	 private void updateClientTelephone() {
		 mClientTelephonePreference = (EditTextPreference) findPreference(KEY_CLIENT_TELEPHONE);
		 mClientTelephonePreference.setSummary(mClientTelephonePreference.getText());
		 mClientTelephonePreference.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
	 }
	 
	 private void updateFontSize() {
	        ListPreference lp = (ListPreference) findPreference(KEY_FONT_SIZE);
	        lp.setSummary(lp.getEntry());
	 }
	 
	 private void updateConnectType() {
	        ListPreference lp1 = (ListPreference) findPreference(KEY_CONNECTION_TYPE);
	        lp1.setSummary(lp1.getEntry());
	 }
	 
	 private void updateTimeRange() {
		 mTimeRangePreference = (EditTextPreference) findPreference(KEY_TIME_RANGE);
		 mTimeRangePreference.setSummary(String.valueOf(mTimeRangeHour)+String.valueOf(mTimeRangeMinute));
	 }
	 
	 private class DownloadFile extends AsyncTask<String, Integer, String> 
	 {
		 protected void onPreExecute()
		 {
			 super.onPreExecute();
			 mProgressDialog.show();
		 }
		 
		 @Override
		 protected String doInBackground(String... sUrl)
		 {
			 try 
			 {
				 URL url = new URL(sUrl[0]);
				 HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				 connection.connect();
				 
				 
				 /**
				  *  download the file
				  */
				 InputStream input = new BufferedInputStream(url.openStream());
				 
				 /**
				  * CREO UNA CARTELLA TEMPORANEA
				  */
				 File temporaryDirectory = new File(Environment.getExternalStorageDirectory().getPath()+"/temporary/");
				 temporaryDirectory.mkdirs();
				 
				 
				 File output = new File(Environment.getExternalStorageDirectory().getPath() + "/temporary/grasp.apk");
				 //File output = new File(Environment.getExternalStorageDirectory().getPath() + "/GRASP/grasp.apk");
				 //File output = new File(PreferencesActivity.this.getCacheDir() + "/GRASP/grasp.apk");
				 
				 FileOutputStream fileOutput = new FileOutputStream(output);
				 
				/**
				 *  this will be useful so that you can show a typical 0-100% progress bar
				 */
				 int fileLength = connection.getContentLength();
				 
				 byte data[] = new byte[1024];
				 long total = 0;
				 int count;
				 while ((count = input.read(data)) != -1) 
				 {
					 total += count;
					 /**
					  *  publishing the progress....
					  */
					 publishProgress((int) (total * 100 / fileLength));
					 Log.i("downloaded", "scaricati "+ total);
					 fileOutput.write(data, 0, count);
				 }
				 fileOutput.flush();
				 fileOutput.close();
				 input.close();
			 }	 
			 catch (Exception e) 
			 {
				 e.printStackTrace();
		     }
			 return null;
		 }
		 protected void onProgressUpdate(Integer... progress) 
		 {
			 super.onProgressUpdate(progress);
			 mProgressDialog.setProgress(progress[0]);
		 }
		 
		 protected void onPostExecute(String sResponse)
		 {
			 mProgressDialog.dismiss();
			 if(sResponse == null)
			 {
				 DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        switch (which){
					        case DialogInterface.BUTTON_POSITIVE:
					            //Yes button clicked
					        	
					        	
					        	/**
					        	 * RIMOZIONE GLOBALE DI TUTTE LE FORM PRECEDENTI E DELLE CARTELLE PRECEDENTI
					        	 */
								((ApplicationExt) ApplicationExt.getInstance()).clearApplicationData();
								File dir = ApplicationExt.getInstance().getCacheDir();
								ApplicationExt.deleteDir(dir);
								
								
								/**
								 * CANCELLAZIONE DEL DB
								 */
								PreferencesActivity.this.deleteDatabase("grasp.db");
								PreferencesActivity.this.deleteDatabase("forms.db");
								//**********************************************
								
								Intent newApp = new Intent(Intent.ACTION_VIEW);
								newApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								newApp.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/temporary/" + "grasp.apk")), "application/vnd.android.package-archive");
								//newApp.setDataAndType(Uri.fromFile(new File(PreferencesActivity.this.getCacheDir() + "/GRASP/" + "grasp.apk")), "application/vnd.android.package-archive");
								startActivity(newApp);
					            finish();
								 		
					            
					            break;

					        case DialogInterface.BUTTON_NEGATIVE:
					            //No button clicked
					        	Intent updateApp = new Intent(Intent.ACTION_VIEW);
					        	updateApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					        	updateApp.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/temporary/" + "grasp.apk")), "application/vnd.android.package-archive");
					            startActivity(updateApp);
					            finish();
					        	
					            break;
					        }
					    }
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(PreferencesActivity.this);
					builder.setMessage(getString(R.string.hard_reset))
					.setPositiveButton(getString(R.string.yes_pref), dialogClickListener)
					.setNegativeButton(getString(R.string.no_pref), dialogClickListener).show();
	         }
		 }
	 }
	
}