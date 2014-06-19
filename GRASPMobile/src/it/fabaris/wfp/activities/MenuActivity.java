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

import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.utility.ConstantUtility;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Class that defines the base activity for the visualization of the menu
 *
 */
public class MenuActivity extends Activity 
{
	private boolean doubleBackToExitPressedOnce = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basicmenu);
        setTitle(getString(R.string.app_name) + " > " + "Menu");
        try {
            Collect.createODKDirs();
        }catch (Exception e) {
            e.printStackTrace();
            return;
        }
        final SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	String numModem = settings.getString(PreferencesActivity.KEY_SERVER_TELEPHONE, getString(R.string.default_server_telephone));
     	String urlServer = settings.getString(PreferencesActivity.KEY_SERVER_URL, getString(R.string.default_server_url));
     	//setto come predefinito il valore del protocollo ad http
     	
     	if(numModem.equalsIgnoreCase("")&&urlServer.equalsIgnoreCase("")){
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    		alert.setTitle("Server Configurations");
    		alert.setMessage("The server number or url are not inserted, " +
    				"please compile number preceded by country code and url preceded by (http://)");
    		ScrollView alertSV = new ScrollView(this);//aggiunta la scroll view perchè non entra tutto nello schermo!!!
    		LinearLayout lila1 = new LinearLayout(this);
    	    lila1.setOrientation(1);
    	    
    	    SharedPreferences.Editor meditor = settings.edit();
            meditor.putString(PreferencesActivity.KEY_PROTOCOL, "http");
            meditor.commit();
    	    
    	    final EditText input1 = new EditText(this);
    	    input1.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    	    input1.setInputType(InputType.TYPE_CLASS_PHONE);
    	    input1.setText(numModem);
    	    
    	    //-------------------------------------------------------
    	    ////imposto il radio button per la scelta del protocollo
    	    final RadioButton http_Option = new RadioButton(this);
    	    http_Option.setText("http");//id 0
    	    //http_Option.setChecked(true);
    	    
    	    final RadioButton https_Option = new RadioButton(this);
    	    https_Option.setText("https");//id 1
    	    
    	    final RadioGroup protocolRadioGroup = new RadioGroup(this); //create the RadioGroup
    	    protocolRadioGroup.setOrientation(RadioGroup.HORIZONTAL);
    	    protocolRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
    	    {
    	        public void onCheckedChanged(RadioGroup group, int checkedId) {
    	        	// checkedId is the RadioButton selected
    	        	for(int i=0; i<protocolRadioGroup.getChildCount(); i++) {
    	                   RadioButton btn = (RadioButton) protocolRadioGroup.getChildAt(i);
    	                   if(btn.getId() == checkedId) {
    	                        String text = (String) btn.getText();
    	                        SharedPreferences.Editor editor = settings.edit();
    	                        // do something with text
    	                        if(text.equals("http")){
    	                        	SharedPreferences.Editor meditor = settings.edit();
	    	         	            meditor.putString(PreferencesActivity.KEY_PROTOCOL, "http");
	    	         	            meditor.commit();
    	                        	//ConstantUtility.protocol = "http";
	    	         	        }else{
    	                        	Log.i("https scelto","https");
    	                        	SharedPreferences.Editor meditor = settings.edit();
	    	         	            meditor.putString(PreferencesActivity.KEY_PROTOCOL, "https");
	    	         	            meditor.commit();
	    	         	            
	    	         	           
    	                        	//ConstantUtility.protocol = "https";
    	                        }
    	                        //Log.i("choosedProtcol",protocol);
    	                    	return;
    	                   }
    	        	}
    	        }
    	    });
    	    protocolRadioGroup.addView(http_Option); //the RadioButtons are added to the radioGroup instead of the layout
    	    protocolRadioGroup.addView(https_Option); //the RadioButtons are added to the radioGroup instead of the layout
    	    
    	    final EditText ETip = new EditText(this);
    	    ETip.setInputType(InputType.TYPE_CLASS_PHONE);
    	    ETip.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    	    ETip.addTextChangedListener(new TextWatcher() {
    	    	 
    	    	   public void afterTextChanged(Editable s) {
    	    		   SharedPreferences.Editor meditor = settings.edit();
    	               meditor.putString(PreferencesActivity.KEY_IP, ETip.getEditableText().toString());
    	               meditor.commit();
    	    	   }
    	    	 
    	    	   public void beforeTextChanged(CharSequence s, int start, 
    	    	     int count, int after) {
    	    	   }
    	    	 
    	    	   public void onTextChanged(CharSequence s, int start, 
    	    	     int before, int count) {
    	    	   
    	    	   }
    	    	  });
    	    
    	    
    	    
    	        	    
    	    final EditText ETport = new EditText(this);
    	    //ETport.setInputType(InputType.TYPE_CLASS_NUMBER);
    	    ETport.setInputType(InputType.TYPE_CLASS_TEXT);
    	    
    	    
    	    ETport.setImeOptions(EditorInfo.IME_ACTION_NEXT);
    	    ETport.addTextChangedListener(new TextWatcher() {
    	    	 
    	    	   public void afterTextChanged(Editable s) {
    	    		   SharedPreferences.Editor meditor = settings.edit();
    	               meditor.putString(PreferencesActivity.KEY_PORT, ETport.getEditableText().toString());
    	               meditor.commit();
    	    	   }
    	    	 
    	    	   public void beforeTextChanged(CharSequence s, int start, 
    	    	     int count, int after) {
    	    	   }
    	    	 
    	    	   public void onTextChanged(CharSequence s, int start, 
    	    	     int before, int count) {
    	    	   
    	    	   }
    	    });
    	    
    	    
    	    
    	    //-----------------------------------
    	    
    	    TextView text1 =  new TextView(MenuActivity.this);
    	    TextView TVserverulr =  new TextView(MenuActivity.this);
    	    TextView TVprotocol =  new TextView(MenuActivity.this);
    	    TextView TVip =  new TextView(MenuActivity.this);
    	    TextView TVport =  new TextView(MenuActivity.this);
    	    
    	    text1.setText("Server Telephone");
    	    text1.setTextSize(17);
    	    TVserverulr.setText("Server URL");
    	    TVserverulr.setTextSize(17);
    	    TVprotocol.setText("Protocol");
    	    TVip.setText("IP");
    	    TVport.setText("Port");
    	    
    	    lila1.addView(text1);//Server telephone
    	    lila1.addView(input1);
    	    lila1.addView(TVserverulr);//phone number
    	    lila1.addView(TVprotocol);//procolo
    	    lila1.addView(protocolRadioGroup);//you add the whole RadioGroup to the layout
    	    lila1.addView(TVip);//ip
    	    lila1.addView(ETip);
    	    lila1.addView(TVport);//port
    	    lila1.addView(ETport);
    	    lila1.setPadding(20, 0, 20, 0);
    	    alertSV.addView(lila1);//metto il layout della dialog in una scroll view
    	    alert.setView(alertSV);
    	    
    	    
    		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    		  String numTel = input1.getEditableText().toString();
    		  
    		   
    		  String ip = null;
    		  String port = null;
    		  
    		  
    		  ip = ETip.getEditableText().toString(); //PreferencesActivity.KEY_IP;
    		  
    		  port = ETport.getEditableText().toString(); //PreferencesActivity.KEY_PORT;
    		  String serverurl = new String();
    		  String protocol = settings.getString(PreferencesActivity.KEY_PROTOCOL,null);
    		  
    		  
    		  //if(!protocol.equals("list_protocol") && !port.isEmpty() && !ip.isEmpty()){
    		  //if(!protocol.isEmpty()&& !ip.isEmpty() && !port.isEmpty()){
    		//  if(protocol != null && !ip.isEmpty() && !port.isEmpty()){
    		if(protocol != null && ip != null && port != null){
    			  serverurl = protocol+"://"+ip+":"+port;
    		 }
    		  
    		  SharedPreferences.Editor editor = settings.edit();
    		  editor.putString(PreferencesActivity.KEY_SERVER_TELEPHONE, numTel);
    		  editor.putString(PreferencesActivity.KEY_SERVER_URL, serverurl);
    		  editor.commit();
    		  
    		  }
    		});

    		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		  public void onClick(DialogInterface dialog, int whichButton) {
    		  }
    		});

    		alert.show();
    	}
    	
        /**
         * forms button
         */
        final Button buttonForms = (Button)findViewById(R.id.forms);
		buttonForms.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				Intent myIntent = new Intent(v.getContext(),FormListActivity.class);
//				myIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(myIntent);
			}
		});
		/**
		 * SMS button
		 */
        final Button buttonSms = (Button)findViewById(R.id.sms);
		buttonSms.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				Intent myIntent = new Intent(v.getContext(),SmsListActivity.class);
				startActivity(myIntent);
			}
		});
		/**
		 * settings button
		 */
        final Button buttonSettings = (Button)findViewById(R.id.settings);
		buttonSettings.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				
				Intent myIntent = new Intent(v.getContext(), ControlActivity.class);
				startActivity(myIntent);
			}
		});
		/**
		 * credits button
		 */
        final Button buttonCredits = (Button)findViewById(R.id.credits);
        buttonCredits.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				Intent myIntent = new Intent(v.getContext(), CreditsActivity.class);
				startActivity(myIntent);
			}
		});
     	/**
     	 * help button
     	 */
        final Button buttonHelp = (Button)findViewById(R.id.help);
        buttonHelp.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				Intent myIntent = new Intent(v.getContext(), HelpActivity.class);
				startActivity(myIntent);
			}
		});
        
        final Button buttonExit = (Button)findViewById(R.id.exit);
        buttonExit.setOnClickListener(new OnClickListener() 
        {
			public void onClick(View v) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
				builder.setTitle(R.string.exit_dialog_title)
				.setIcon(R.drawable.icona_app_wfp)
		    	.setMessage(R.string.exit_dialog)
		    	.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener()
		    	{
					public void onClick(DialogInterface arg0, int arg1) 
					{
						finish();
			            System.exit(0);						
					}
		    	})
		    	.setNegativeButton(getString(R.string.negative), new DialogInterface.OnClickListener()
		    	{
		    		public void onClick(DialogInterface dialog, int id)
		    		{
		    			dialog.cancel();
		    		}
		    	});
				builder.show();
			}
		});
    }
    
    
    /**
     * ABILITA TASTO BACK DI SISTEMA NELLA HOME AD ACCETTARE DUE TOCCHI PER USCIRE (nei 2 secondi)
     */
    public void onBackPressed()  
    {
        if (doubleBackToExitPressedOnce) 
        {
        	super.onBackPressed();
        	return;
        }
        
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(getBaseContext(), getString(R.string.double_back),Toast.LENGTH_SHORT).show();
        
        new Handler().postDelayed(new Runnable() 
        {
        	public void run()
        	{
        		doubleBackToExitPressedOnce = false;
			}
		}, 2000);
    }
  
}
