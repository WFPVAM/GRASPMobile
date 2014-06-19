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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import object.FormInnerListProxy;

import org.w3c.dom.Document;

import content.FormPendingAdapter;

import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.task.HttpCheckAndSendPostTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Class that defines the tab for the list of the finalized forms
 *
 */
public class FormListFinalizedActivity extends Activity implements MyCallback
{
	public interface FormListHandlerFinalized
	{
		public ArrayList<FormInnerListProxy> getFinalizedForm();
		public void catchCallBackFinalized(String[] finalized);
	}
	public FormListHandlerFinalized formListHandler;
	
	
	private FormPendingAdapter adapter;
	
	private ArrayList<FormInnerListProxy> finalizzate;
	private ListView listview;
	
	private String numClient;
	private String numModem;
	private String encodeXml;
	private String httpServer;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabpending);

		finalizzate = new ArrayList<FormInnerListProxy>();		
		finalizzate = getIntent().getExtras().getParcelableArrayList("finalized");
		

		 listview = (ListView)findViewById(R.id.listViewPending);
		 listview.setCacheColorHint(00000000);
		 listview.setClickable(true);
			
			adapter = new FormPendingAdapter(this, finalizzate);
	        listview.setAdapter(adapter);
	        
	        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			listview.setOnItemClickListener(new OnItemClickListener()
			{	
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
				{
						/*
					path = (pthformFinalizzate[position]).toString();
					form = (formnameFinalizzate[position]).toString();
					formName = (formNameAutoGenFinalizzate[position]).toString();
					final SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					final String connectionType = settings.getString(PreferencesActivity.KEY_CONNECTION_TYPE, getString(R.string.default_connection_type));
					
					builder.setMessage(getString(R.string.send_form_by_network))
				       .setCancelable(false)
				       .setPositiveButton(R.string.positive_choise, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) 
				           {
				        	   
				        	   try
				        	   {
				        		   InputStream  fileInput = new FileInputStream(path);
				        		   Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fileInput);
				        		   TransformerFactory transfac = TransformerFactory.newInstance();
				        		   Transformer trans = transfac.newTransformer();
				        		   trans.setOutputProperty(OutputKeys.METHOD, "xml");
				        		   trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		 	                       StringWriter sw = new StringWriter();
		 	                       StreamResult result = new StreamResult(sw);
		 	                       DOMSource source = new DOMSource(doc);
		 	                       trans.transform(source, result);
		 	                       String xmlString = sw.toString();
		 	                       String apos = "apos=\"'\"";
		 	                       xmlString = xmlString.replace(apos, "");
		 	                       // add unique code to data xml response
		 	                       xmlString = xmlString+"?formidentificator?"+ form;
		 	                       // add autogenerated name to data xml response
		 	                       xmlString = xmlString+"?formname?"+ formName;
		 	                       // add date and time to data xml response
		 	                       GregorianCalendar gc = new GregorianCalendar();
		 	                       String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
		 	                       String month = Integer.toString(gc.get(Calendar.MONTH));
		 	                       String year = Integer.toString(gc.get(Calendar.YEAR));
		 	                       String hour = Integer.toString(gc.get(Calendar.HOUR_OF_DAY));
		 	                       String date = day+"/"+month+"/"+year;
		 	                       xmlString = xmlString+"?formhour?"+ date+"_"+hour;
		 	                       
		 	                       numClient = settings.getString(PreferencesActivity.KEY_CLIENT_TELEPHONE,getString(R.string.default_client_telephone));
		 	                       numModem = settings.getString(PreferencesActivity.KEY_SERVER_TELEPHONE,getString(R.string.default_server_telephone));
		 	                       httpServer = settings.getString(PreferencesActivity.KEY_SERVER_URL,getString(R.string.default_server_url));
		 	                       encodeXml = FormListCompletedActivity.encodeSms(xmlString);
				        	   }
				        	   catch (Exception e) 
				        	   {
				        		   e.printStackTrace();
				        	   }	
		                       
				        	   ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				        	   if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected() == true) 
				        	   {
				        		   //invio tramite sms
				        		   if(connectionType.equalsIgnoreCase("sms"))
				        		   {
				        			   if(numModem.equalsIgnoreCase("")||numModem==null)
				        			   {
				        				   Toast toast = Toast.makeText(getApplicationContext(), R.string.number_error, Toast.LENGTH_LONG);
				        				   toast.setGravity(Gravity.CENTER, 40, 40);
				        				   toast.show();
				        			   }
				        			   else
				        			   {
				        				   FormListCompletedActivity formToBeSend = new FormListCompletedActivity(); 
				        				   formToBeSend.sendSmsNetWorkOn(numModem, encodeXml, FormListFinalizedActivity.this);
				        			   }
				 	                    	//invio tramite 2g/3g
			 	                       		}
			 	                       		else if(connectionType.equalsIgnoreCase("gprs/umts"))
			 	                       		{
				 	                    	  if(httpServer.equalsIgnoreCase("")||httpServer==null)
				 	                    	  {
				 	                    		  Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.server_url_not_inserted), Toast.LENGTH_LONG);
				 	                    		  toast.setGravity(Gravity.CENTER, 40, 40);
				 	                    		  toast.show();
				 	                    	  }
				 	                    	  else
				 	                    	  {
				 	                    		  String http = httpServer;
				 	                    		  String phone = numClient;
				 	                    		  String data = encodeXml;
				 	                    		  HttpCheckAndSendPostTask asyncTask = new HttpCheckAndSendPostTask(FormListFinalizedActivity.this,http, phone, data, FormListFinalizedActivity.this);
				 	                    		  asyncTask.execute();
				 	                    		 
				 	                    		  //queryCompletedForm();
				 	                    		  //adapter.notifyDataSetChanged();
				 	                    	  }
			 	                       		}
			 	                       		else if(connectionType.equalsIgnoreCase(getString(R.string.on_request)))
			 	                       		{
			 	                       			CharSequence[] items = {"gprs/umts", "sms"};
			 	                       			new AlertDialog.Builder(FormListFinalizedActivity.this)
			 	                       			.setSingleChoiceItems(items, 0, null)
			 	                       			.setPositiveButton(R.string.positive_choise, new DialogInterface.OnClickListener() 
			 	                       			{
			 	                       				public void onClick(DialogInterface dialog, int whichButton) 
			 	                       				{
			 	                       					dialog.dismiss();
			 	                       					int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
			 	                       					if(selectedPosition == 0)
			 	                       					{
			 	                       					if(httpServer.equalsIgnoreCase("")||httpServer==null)
			 				 	                    	  {
			 				 	                    		  Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.server_url_not_inserted), Toast.LENGTH_LONG);
			 				 	                    		  toast.setGravity(Gravity.CENTER, 40, 40);
			 				 	                    		  toast.show();
			 				 	                    	  }
			 				 	                    	  else
			 				 	                    	  {
			 				 	                    		  String http = httpServer;
			 				 	                    		  String phone = numClient;
			 				 	                    		  String data = encodeXml;
			 				 	                    		  HttpCheckAndSendPostTask asyncTask = new HttpCheckAndSendPostTask(FormListFinalizedActivity.this,http, phone, data, FormListFinalizedActivity.this);
			 				 	                    		  asyncTask.execute();
			 				 	                    		  
			 				 	                    		  //queryCompletedForm();
			 				 	                    		  //adapter.notifyDataSetChanged();
			 				 	                    	  }
			 	                       					}
			 	                       					else if(selectedPosition == 1)
			 	                       					{
			 	                       						if(numModem.equalsIgnoreCase("")||numModem==null)
			 	                       						{
			 	                       							Toast toast = Toast.makeText(getApplicationContext(), R.string.number_error, Toast.LENGTH_LONG);
			 	                       							toast.setGravity(Gravity.CENTER, 40, 40);
			 	                       							toast.show();
			 	                       						}
			 	                       						else
			 	                       						{
			 	                       							FormListCompletedActivity formToBeSend = new FormListCompletedActivity(); 
			 	                       							formToBeSend.sendSmsNetWorkOn(numModem, encodeXml, FormListFinalizedActivity.this);
			 	                       						}
			 	                       					}
			 	                       				}
			 	                       			}).show();
					 	                    }
			                        	}
					        	   	
				          	else if (cm.getActiveNetworkInfo() == null || cm.getActiveNetworkInfo().isConnected() == false) 
							{
				          		try
								{// invio tramite sms
									if (connectionType.equalsIgnoreCase("sms")|| connectionType.equalsIgnoreCase("gprs/umts") || connectionType.equalsIgnoreCase(getString(R.string.on_request)))
									{
										CharSequence[] items = { "sms" };
										new AlertDialog.Builder(
												FormListFinalizedActivity.this)
										.setSingleChoiceItems(
												items, 0,
												null)
												.setPositiveButton(
														R.string.positive_choise,
														new DialogInterface.OnClickListener() 
														{
															public void onClick(DialogInterface dialog, int whichButton)
															{
																dialog.dismiss();
																
																if (numModem.equalsIgnoreCase("") || numModem == null) 
																{
																	Toast toast = Toast.makeText(getApplicationContext(),R.string.number_error,Toast.LENGTH_LONG);
																	toast.setGravity(Gravity.CENTER,40,40);
																	toast.show();
																}
																else
																{
																	FormListCompletedActivity formToBeSend = new FormListCompletedActivity(); 
																	formToBeSend.sendSmsNetWorkOn(numModem,encodeXml,FormListFinalizedActivity.this);
																}
															}
														}).show();
													}
												} catch (Exception e) {
													e.printStackTrace();
												}
											
							}
					           		}
					       		})
					       		.setNegativeButton(getString(R.string.negative_choise), new DialogInterface.OnClickListener() {
					       			public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					           }
					       });
					AlertDialog alert = builder.create();
					alert.show();
				}
				*/
				}
				
			});
			
				
	}
	/*
	public void selectView()
	{
		formListHandler = new FormListActivity();
		formListHandler.getFinalizedForm();
		/*
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.formlist_row, R.id.label, formNameAutoGen);
		setListAdapter(adapter);
		
	}
	*/
	/*
	private void doTheAutoRefresh() {
	    handler.postDelayed(new Runnable() {
	             @Override
	             public void run() {
	            	 selectView();
	                 doTheAutoRefresh();                
	             }
	         }, 5000);
	}
	*/	
	
	/*
	@Override
	public void setFinalizzateForm(String[] pthformFinalizzate, String[] formnameFinalizzate, String[] strpathInstanceFinalizzate, String[] formNameInstanceFinalizzate, String[] formNameAutoGenFinalizzate) {
		this.pthformFinalizzate = pthformFinalizzate;
		this.formnameFinalizzate = formnameFinalizzate;
		this.strpathInstanceFinalizzate = strpathInstanceFinalizzate;
		this.formNameInstanceFinalizzate = formNameInstanceFinalizzate;
		this.formNameAutoGenFinalizzate = formNameAutoGenFinalizzate;
	}
	*/
	
	/**
	 * 
	 * @return
	 */
	private ArrayList<FormInnerListProxy> queryFinalizedForm()
	{
		formListHandler = new FormListActivity();
		ArrayList<FormInnerListProxy> finalizzate = formListHandler.getFinalizedForm();
		
		return finalizzate;	
	}
	
	public void onResume()
    {
         super.onResume();
         adapter.notifyDataSetChanged();
    }
	@Override
	public void callbackCall()
	{
		/*
		formNameAutoGenFinalizzata = formListHandler.getFinalizedForm().get(5);
		formListHandler.catchCallBackFinalized(formNameAutoGenFinalizzate);
		listview.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		finish();
		*/
	}

	public void onDestroy()
    {
		listview.setAdapter(null);
        super.onDestroy();
    }
	@Override
	public void finishFormListCompleted() {
		// TODO Auto-generated method stub
		
	}
}
