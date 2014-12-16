/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package it.fabaris.wfp.activities;

import java.io.File;
import java.io.IOException;

import utils.ApplicationExt;
import database.DbAdapterGrasp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Class that manage the splashscreen at the start of app
 */
public class SplashScreenActivity extends Activity {
    private Thread splashTread;
    String numClient = null;
    String numTel = null;
    EditText input;
    SharedPreferences settings;
    
    private String LOG_TAG = "GRASP Application";
    private static DbAdapterGrasp mDb;
    public String basepath; 
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        String s = null;
        TelephonyManager tMgr =(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = ((TelephonyManager) tMgr).getLine1Number();
        
                
        settings  =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        numClient = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE,getString(R.string.default_client_telephone));
        if(numClient.equalsIgnoreCase("")){
        	setContentView(R.layout.settings_screen);
        	input = (EditText) findViewById(R.id.phoneNumber);
        	input.setInputType(InputType.TYPE_CLASS_PHONE);
  		  	Button buttonForms = (Button)findViewById(R.id.phoneNumberDone);
  		  	buttonForms.setOnClickListener(new View.OnClickListener(){
  			@Override
  			public void onClick(View v){
  				numTel = input.getEditableText().toString();
  				if(numTel.equalsIgnoreCase("")){
  					Toast toast = Toast.makeText(getApplicationContext(), R.string.telephone_number,Toast.LENGTH_LONG);
  					toast.show();
  				}else if(numTel.substring(0, 1).equalsIgnoreCase("+")){
  					final SharedPreferences.Editor editor = settings.edit();
  	  				editor.putString(PreferencesActivity.KEY_CLIENT_TELEPHONE, numTel);
  	  				editor.commit();
  					Intent myIntent = new Intent(v.getContext(),MenuActivity.class);

  	  				startActivity(myIntent);
  	  				finish();
  				}else{
  					Toast toast = Toast.makeText(getApplicationContext(), R.string.telephone_number_country,Toast.LENGTH_LONG);
  					toast.show();
  				}
  			}
  		});
        }else{
        	setContentView(R.layout.splash_screen);
            final SplashScreenActivity sPlashScreen = this; 
            splashTread = new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized(this){
                                wait(2000);
                        }

                    } catch(InterruptedException e) {}
                    finally {
                        finish();
                        Intent i = new Intent();
                        i.setClass(sPlashScreen, MenuActivity.class);
                        startActivity(i);
                        finish();
                    }
                }
            };
            splashTread.start();
        }
    }
    
    
    private void FirstRun() throws IOException  
    { 
        SharedPreferences settings = this.getSharedPreferences("GRASP", 0); 
        boolean firstrun = settings.getBoolean("firstrun", true); 
        if (firstrun)  
        {  
            /**
             *  Checks to see if we've ran the application  
             */
            SharedPreferences.Editor e = settings.edit(); 
            e.putBoolean("firstrun", false); 
            e.commit(); 
            /**
             *  If not, run these methods: 
             */
            SetDirectory(); 
        }  
        else 
        {  
            // Otherwise start the application here: 
        } 
    } 
      
    private void SetDirectory() throws IOException  
    { 
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))  
        { 
            File grasp = new File(basepath); //+ "grasp"); 
            if (!grasp.exists()) 
            { 
                grasp.mkdirs();     
            } 
            copyGraspDatabaseToSD(""); // Then run the method to copy the file. 
  
        }  
        else if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED_READ_ONLY))  
        { 
            Toast.makeText(getApplicationContext(), getString(R.string.update_toast), Toast.LENGTH_SHORT).show(); 
        } 
  
    } 
      
    private void copyGraspDatabaseToSD(String path) throws IOException  
    { 
    	mDb = new DbAdapterGrasp(this);
    	/*
    	//CORRETTO
        AssetManager assetManager = this.getAssets(); 
        String assets[] = null; 
        try 
        { 
            Log.i("tag", "copyGraspToSD() " + path); 
            assets = assetManager.list(path); 
            if (assets.length == 0) 
            { 
                copyFile(path); 
            }  
            else
            { 
                String fullPath =  basepath +  path; 
                Log.i("tag", "path= "+ fullPath); 
              
                File dir = new File(fullPath); 
                if (!dir.exists() && !path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) 
                    if (!dir.mkdirs()); 
                        Log.i("tag", "Non può creare la cartella "+ fullPath); 
                for (int i = 0; i < assets.length; ++i) 
                { 
                    String p; 
                    if (path.equals("")) 
                        p = ""; 
                    else 
                        p = path + "/"; 
  
                    if (!path.startsWith("images") && !path.startsWith("sounds") && !path.startsWith("webkit")) 
                    	copyGraspToSD( p + assets[i]); 
                } 
            } 
        }  
        catch (IOException ex)  
        { 
            Log.e("tag", "I/O Exception", ex); 
        } 
      
        /* 
        for(int i=0; i<files.length; i++) 
        { 
            InputStream in = null; 
            OutputStream out = null; 
            try 
            { 
                in = assetManager.open(files[i]); 
                out = new FileOutputStream(basepath + "/" + files[i]); 
                copyFile(in, out); 
                in.close(); 
                in = null; 
                out.flush(); 
                out.close(); 
                out = null; 
            } 
            catch (Exception e)  
            { 
                // TODO: handle exception 
                Log.e(TAG, e.getMessage()); 
            } 
        } 
        */
    } 
    
    /**
     * TASK DI COPIA DEI CONTENUTI
     * @author UtenteSviluppo
     *
     */
    private class CopyTask extends AsyncTask<Void, Void, Void>
    {   
        @Override
        protected Void doInBackground(Void... params) 
        {
            Looper.prepare();
            try
            {
                FirstRun();
            } 
            catch (IOException e1) 
            {
                e1.printStackTrace();
            }
            return null;
        }
        protected void onProgressUpdate(Void... unsued) 
        {
            Toast messaggio = Toast.makeText(getApplicationContext(), "E' in corso la copia dei contenuti...", Toast.LENGTH_LONG);  
            messaggio.show();   
        }
    }
}