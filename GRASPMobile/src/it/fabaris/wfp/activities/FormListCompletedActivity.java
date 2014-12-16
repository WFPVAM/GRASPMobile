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

import it.fabaris.wfp.listener.MyCallback;
import it.fabaris.wfp.provider.FormProvider.DatabaseHelper;
import it.fabaris.wfp.provider.InstanceProviderAPI;
import it.fabaris.wfp.task.HttpCheckAndSendPostTask;
import it.fabaris.wfp.task.HttpSendAllFormsTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import object.FormInnerListProxy;

import org.w3c.dom.Document;

import utils.ApplicationExt;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import content.FormCompletedAdapter;
import database.DbAdapterGrasp;

import it.fabaris.wfp.utility.FormCompletedDataDBUpdate;

/**
 * Class that defines the tab for the list of the completed forms
 * 
 */
public class FormListCompletedActivity extends Activity implements MyCallback {
	public interface FormListHandlerCompleted {
		public ArrayList<FormInnerListProxy> getCompletedForm();

		public void catchCallBackCompleted(String[] complete);
	}

	public FormListHandlerCompleted formListHandler;

	private Lock lock;
	private Condition cond;

	public static String data;
	public String nomeform;
	public String autore;
	private FormCompletedAdapter adapter;
	private int positioncomplete = 0;
	private String idFormNameInstance = null;

	private static Notification notification;
	private NotificationManager nm;
	private ListView listview;

	private SharedPreferences settings;
	private String connectionType;

	private String numClient;
	private String numModem;
	private String httpServer;
	private String encodeXml;
	private static boolean isSendAllForms = false;

	public static String idForm;
	
	public static ArrayList<String> istance;

	private ArrayList<FormInnerListProxy> complete;
	private ArrayList<FormInnerListProxy> completed;
	
	private static ArrayList<FormCompletedDataDBUpdate> listDataToUpdateDB = new ArrayList<FormCompletedDataDBUpdate>();

	ProgressDialog pd;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabcompleted);
		

		istance = new ArrayList<String>();	
		final Builder builder = new AlertDialog.Builder(this);

		/**
		 *  scrivo quante ne ho aggiunte sul tab
		 *  contaComplete - quanteComplete = aggiunte (textView)
		 *  text.setText(contaComplete -
		 *  quanteComplete);
		 */
		/**
		 *  MANDA TUTTE LE FORM
		 */
		Button buttonSendAll = (Button) findViewById(R.id.sendAll);
		/**
		 * always visible
		 */
			buttonSendAll.setVisibility(View.VISIBLE);
			buttonSendAll.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					/**
					 *  INVIO I MESSAGGI IN LISTA
					 */
					istance.clear();
					isSendAllForms = true;
					sendFormInList(complete);
				}
			});

		lock = new ReentrantLock();
		cond = lock.newCondition();

		settings = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		connectionType = settings.getString(
				PreferencesActivity.KEY_CONNECTION_TYPE,
				getString(R.string.default_connection_type));

		numClient = settings.getString(
				PreferencesActivity.KEY_CLIENT_TELEPHONE,
				getString(R.string.default_client_telephone));
		numModem = settings.getString(PreferencesActivity.KEY_SERVER_TELEPHONE,
				getString(R.string.default_server_telephone));
		httpServer = settings.getString(PreferencesActivity.KEY_SERVER_URL,
				getString(R.string.default_server_url));

		Button sendAll = (Button) findViewById(R.id.sendAll);
		if (!isNetworkConnected())
			sendAll.setVisibility(View.GONE);

		complete = new ArrayList<FormInnerListProxy>();
		complete = getIntent().getExtras().getParcelableArrayList("completed");

		completed = new ArrayList<FormInnerListProxy>();
		completed = getIntent().getExtras().getParcelableArrayList("complete");

		listview = (ListView) findViewById(R.id.listViewCompleted);
		listview.setCacheColorHint(00000000);
		listview.setClickable(true);
		adapter = new FormCompletedAdapter(this, complete, completed);
		listview.setAdapter(adapter);		
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, final int position, long id) 
			{
				Log.e("Posizione premuta nella lista delle form complete:" , String.valueOf(position));
				
				Context context = getApplicationContext();
				Intent intent = new Intent (context, FormEntryActivity.class);
				String keyIdentifer  = "ciao";
				String keyIdentifer1  = "ciao1";
				String keyIdentifer2  = "ciao2";
				String keyIdentifer3  = "ciao3";
				
				
				positioncomplete = getRightCompletedParcelableObject(completed.get(position).getFormName());//LL 12-02-14 passo l'id che identifica l'istanza della form cui fa riferimento l'elemento selezionato
																														//nella lista degli elementi completi
				Log.e("Posizione premuta nella lista delle form completeD:" , String.valueOf(positioncomplete));
				String pkgName = getPackageName();
				
				//LL eliminato perche' non prendeva l'id giusto
				/*
				intent.putExtra(pkgName+keyIdentifer, complete.get(position).getPathForm()); 			
				intent.putExtra(pkgName+keyIdentifer1, complete.get(position).getFormName());			
				intent.putExtra(pkgName+keyIdentifer2, complete.get(position).getFormNameInstance());	
				intent.putExtra(pkgName+keyIdentifer3, complete.get(position).getFormId());
				*/
				
				//LL aggiunto per prendere l'identificativo della form in base all'oggetto parcellizato GRASP scelto nella ListView delle complete
				intent.putExtra(pkgName+keyIdentifer, complete.get(positioncomplete).getPathForm()); 			
				intent.putExtra(pkgName+keyIdentifer1, complete.get(positioncomplete).getFormName());			
				intent.putExtra(pkgName+keyIdentifer2, complete.get(positioncomplete).getFormNameInstance());	
				intent.putExtra(pkgName+keyIdentifer3, complete.get(positioncomplete).getFormId());
				
				String action = getIntent().getAction();
				FormEntryActivity.fromHyera = true;
				
					intent.putExtra("submitted", true);
				
				    intent.setAction(Intent.ACTION_EDIT);
				    //String extension = MimeTypeMap.getFileExtensionFromUrl(complete.get(position).getStrPathInstance()).toLowerCase(); //LL questa posizione non e' corretta
				    String extension = MimeTypeMap.getFileExtensionFromUrl(complete.get(positioncomplete).getStrPathInstance()).toLowerCase();//LL questa e' la posizione giusta (presa in relazione veramente alla form selezionata sulla lista
				    
				    
					String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
					intent.setDataAndType(InstanceProviderAPI.InstanceColumns.CONTENT_URI, mimeType);
				    startActivity(intent);
			 }
		});
		

		listview.setOnItemLongClickListener(new OnItemLongClickListener() {                 
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {
				//idFormNameInstance = completed.get(position).getFormName();
				builder.setMessage(getString(R.string.send_form))
						.setCancelable(false)
						.setPositiveButton(R.string.positive_choise,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										
										
										ArrayList<FormInnerListProxy> mycomplete = getCompleteParceableList();//LL aggiunti perche' qui non avevo visibilita' sulle variabili d'istanza
										ArrayList<FormInnerListProxy> mycompleted = getCompletedParceableList();//LL aggiunti perche' qui non avevo visibilita' sulle variabili d'istanza
										
										idFormNameInstance = mycompleted.get(position).getFormName();
										
										int positioncomplete = getPositionCompletedToSubmit(position);
										
										// ****************************************************
										istance.clear();
										
										//encodeXml = decodeForm(complete.get(position));
										encodeXml = decodeForm(mycomplete.get(positioncomplete));//LL sostituito position con positioncomplete per prendere la form giusta
										// ****************************************************
										/**
										 *  CAMPI DI SALVATAGGIO INVIATO
										 */
										
										nomeform = mycomplete.get(positioncomplete).getFormName();
										autore = mycompleted.get(position).getFormEnumeratorId();//qui ci sta il valore dell' enumeratorID da passare quando si invia la form per poter salvare il valore nella tabella delle inviate
										
										FormCompletedDataDBUpdate myFormCompletedDataDBUpdate = new FormCompletedDataDBUpdate(idFormNameInstance, nomeform, autore);
										listDataToUpdateDB.add(myFormCompletedDataDBUpdate);//qui dentro metto i dati per fare l'update del DB grasp che verra' effettuato dalla chiamata di callback una volta che la form e' stata inviata al server
										
									
										/*LL 20-03-2014 da rimettere per form di test 
										String[] splittedFormName = nomeform.split("_");
										String endOfForm = splittedFormName[splittedFormName.length-1];//se l'ultima occorrenza e' test
										
										if(!endOfForm.equalsIgnoreCase("test")){//se il nome della form NON finisce per _test non bisogna spedire la form
										*/
										
											
											
											
											
											if (isNetworkConnected()) {
												try {
													// invio solo tramite sms
													if (connectionType
															.equalsIgnoreCase("sms only")) {
														sendSMS(numModem,
																encodeXml,
																FormListCompletedActivity.this);
													}
													// invio solo tramite 2g/3g
													else if (connectionType
															.equalsIgnoreCase("gprs/umts only")) {
														sendWithNetwork(
																FormListCompletedActivity.this,
																httpServer,
																numClient,
																encodeXml,
																FormListCompletedActivity.this);
														adapter.notifyDataSetInvalidated();
														adapter.notifyDataSetChanged();
													} else if (connectionType
															//.equalsIgnoreCase(getString(R.string.on_request))) {   LL 14-1-14
															.equalsIgnoreCase("gprs/umts preferred")) { //LL 14-1-14
														
														try {
															sendWithNetwork(
																	FormListCompletedActivity.this,
																	httpServer,
																	numClient,
																	encodeXml,
																	FormListCompletedActivity.this);
														} catch (InterruptedException e) {
															// TODO
															// Auto-generated
															// catch
															// block
															e.printStackTrace();
														}
														adapter.notifyDataSetInvalidated();
														adapter.notifyDataSetChanged();
														
														
														/* vecchia logica ora                          LL
														CharSequence[] items = {
																"gprs/umts", "sms" };
														new AlertDialog.Builder(
																FormListCompletedActivity.this)
																.setSingleChoiceItems(
																		items, 0,
																		null)
																.setPositiveButton(
																		R.string.positive_choise,
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int whichButton) {
																				dialog.dismiss();
																				int selectedPosition = ((AlertDialog) dialog)
																						.getListView()
																						.getCheckedItemPosition();
																				if (selectedPosition == 0) {
																					// -------------------------------------------------
																					pd = ProgressDialog
																							.show(FormListCompletedActivity.this,
																									FormListCompletedActivity.this
																											.getString(R.string.checking_server),
																									FormListCompletedActivity.this
																											.getString(R.string.wait));
	
																					// -------------------------------------------------
	
																					try {
																						sendWithNetwork(
																								FormListCompletedActivity.this,
																								httpServer,
																								numClient,
																								encodeXml,
																								FormListCompletedActivity.this);
																					} catch (InterruptedException e) {
																						// TODO
																						// Auto-generated
																						// catch
																						// block
																						e.printStackTrace();
																					}
																					adapter.notifyDataSetInvalidated();
																					adapter.notifyDataSetChanged();
	
	
																				} else if (selectedPosition == 1) {
																					sendSMS(numModem,
																							encodeXml,
																							FormListCompletedActivity.this);
	
																				}
																			}
																		}).show();*/ //LL
													}
												} catch (Exception e) {
													e.printStackTrace();
												}
											} else if (!isNetworkConnected()) {
												try {
													// invio solo tramite sms
													if (connectionType
															//.equalsIgnoreCase("sms") LL 14-01-14
															.equalsIgnoreCase("sms only") //LL
															//|| connectionType.equalsIgnoreCase("gprs/umts") //LL 14-01-14
															//|| connectionType.equalsIgnoreCase("gprs/umts preferred")) { //LL
														) { //LL
														
														sendSMS(numModem,
																encodeXml,
																FormListCompletedActivity.this);
														/* LL													
														CharSequence[] items = { "sms" };
														new AlertDialog.Builder(
																FormListCompletedActivity.this)
																.setSingleChoiceItems(
																		items, 0,
																		null)
																.setPositiveButton(
																		R.string.positive_choise,
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int whichButton) {
																				dialog.dismiss();
																				sendSMS(numModem,
																						encodeXml,
																						FormListCompletedActivity.this);
	
																				
																			}
																		}).show();
														
														 */	//LL											
													}else if(connectionType.equalsIgnoreCase("gprs/umts only")){ // gprs/umts only
														//it is not possible to send the form wait for the connection
														CharSequence[] items = { "There isn't a connection it isn't possible send the form" };
														new AlertDialog.Builder(
																FormListCompletedActivity.this)
																.setSingleChoiceItems(
																		items, 0,
																		null)
																.setPositiveButton(
																		R.string.positive_choise,
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int whichButton) {
																				dialog.dismiss();}})
																.show();
	
													}else if(connectionType.equalsIgnoreCase("gprs/umts preferred")){ //here there is no data connection
														//it is not possible to send the form by network do u want to send it by sms?
														AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FormListCompletedActivity.this);
														alertDialogBuilder.setTitle("Connection not present!");
														alertDialogBuilder
														.setMessage("do you want to send the form by sms?")
														.setPositiveButton(
																		R.string.positive_choise,
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int whichButton) {
																				dialog.dismiss();
																				sendSMS(numModem,
																						encodeXml,
																						FormListCompletedActivity.this);}})
																.setNegativeButton(
																		R.string.negative_choise,
																		new DialogInterface.OnClickListener() {
																			public void onClick(
																					DialogInterface dialog,
																					int whichButton) {
																				dialog.dismiss();}})
																				
																.show();
													}
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										//}	LL per form di test
									}
								})
						.setNegativeButton(getString(R.string.negative_choise),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
				finish();
				return true;
			}
		});
	}

	/**
	 *  invio una form tramite sms
	 * @param numero
	 * @param testo
	 */
	public void sendSmsNetworkOff(String numero, String testo) {
		ContentValues values = new ContentValues();
		values.put("address", numero);
		values.put("body", testo);
		getContentResolver().insert(Uri.parse("content://sms/outbox"), values);
	}

	/**
	 * aggiornamento dello stato della form a inviato
	 */
	public static void updateFormToSubmitted() {
		for (int k = 0; k < istance.size(); k++) {
			DatabaseHelper dbh = new DatabaseHelper("forms.db");
			String updatequery = "UPDATE forms SET status='submitted' WHERE displayNameInstance = '"
					+ istance.get(k) + "'";

			Log.i("FUNZIONE updateFormToSubmitted per la form: ",
					istance.get(k));

			dbh.getReadableDatabase().execSQL(updatequery);

			
			Calendar rightNow = Calendar.getInstance();
			java.text.SimpleDateFormat month = new java.text.SimpleDateFormat(
					"MM");
			// ----------------------------------------------------------------------------------------
			/**
			 *  data di importazione
			 */
			GregorianCalendar gc = new GregorianCalendar();
			String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
			
			String year = Integer.toString(gc.get(Calendar.YEAR));

			data = day + "/" + month.format(rightNow.getTime()) + "/" + year;
			// -----------------------------------------------------

			dbh.close();
		}
	}
	
	///passo alla dialog gli oggetti parcellizzati
	private ArrayList<FormInnerListProxy> getCompleteParceableList(){
		ArrayList<FormInnerListProxy> mycompleted = this.complete;
		return mycompleted;
	}
	
	///passo alla dialog gli oggetti parcellizzati
	private ArrayList<FormInnerListProxy> getCompletedParceableList(){
		ArrayList<FormInnerListProxy> mycomplete = this.completed;
		return mycomplete;
		
	}
	
	//LL aggiunto 12-02-14
	//nell' oggetto parcellizzato di Fabaris identifica la posizione dell' item che contiene i dati giusti per la form selezionata nella lista
	private int getRightCompletedParcelableObject(String idFormInFabaris){//prende l'identificativo univoco della form
		
		int posizione = 0;
		//seleziona la posizione nella lista degli oggetti parcellizzati di fabaris che contiene l'id della form collegato all'oggetto parcellizzato cliccato sulla lista 
		//delle complete
		for(int i = 0; i<complete.size(); i++){
			String prova = complete.get(i).getFormNameInstance();
			if(complete.get(i).getFormNameInstance().contains(idFormInFabaris)){
				posizione = i;
			}
		}
		return posizione;//restituisce la posizione dell'oggetto parcellizzato fabaris cui fa riferimento la form selezionata nella lista delle complete
	} 
	
	//LL aggiunto 14-02-2014
	//e' stato necessario aggiungere questa funzione per trovare la form giusta da inviare perche' gli oggetti parcellizzati non sono raggiungibili dalla dialog
	private int getPositionCompletedToSubmit(int position){//prende l'identificativo univoco della form
		int positioncomplete = getRightCompletedParcelableObject(completed.get(position).getFormName());
		return positioncomplete;//restituisce la posizione dell'oggetto parcellizzato fabaris cui fa riferimento la form selezionata nella lista delle complete
	}

	/**
	 * aggiornamento dello stato della form a finalizzato
	 */
	public static void updateFormToFinalized() {
		DatabaseHelper dbh = new DatabaseHelper("forms.db");
		String updatequery = "UPDATE forms SET status='finalized' WHERE displayNameInstance = '"
				+ istance + "' AND status='completed'";
		dbh.getReadableDatabase().execSQL(updatequery);
		dbh.close();
	}
	
	
	public void sendSmsNetWorkOn(final String numero, final String testo,
			MyCallback callback) {
		String sended = "SENDED";
		String delivered = "DELIVERED";
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> arrayTesto = sms.divideMessage(testo);
		int messageCount = arrayTesto.size();
		ArrayList<PendingIntent> sendingIntents = new ArrayList<PendingIntent>(
				messageCount);
		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(
				messageCount);
		for (int j = 0; j < messageCount; j++) {
			sendingIntents.add(PendingIntent.getBroadcast(this, 0, new Intent(
					sended), 0));
		}
		for (int j = 0; j < messageCount; j++) {
			deliveryIntents.add(PendingIntent.getBroadcast(this, 0, new Intent(
					delivered), 0));
		}

		/**
		 * SMS INVIATO
		 */
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context cont, Intent intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast toast1 = Toast.makeText(getBaseContext(),
							R.string.sending_sms, Toast.LENGTH_LONG);
					toast1.show();
					updateFormToSubmitted();
					adapter.notifyDataSetChanged();

					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast toast2 = Toast.makeText(getBaseContext(),
							R.string.generic_error, Toast.LENGTH_SHORT);
					toast2.show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast toast3 = Toast.makeText(getBaseContext(),
							R.string.error_no_service, Toast.LENGTH_SHORT);
					toast3.show();
					sendSmsNetworkOff(numero, testo);
					updateFormToFinalized();
					adapter.notifyDataSetChanged();

					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast toast4 = Toast.makeText(getBaseContext(),
							R.string.error_no_pdu, Toast.LENGTH_SHORT);
					toast4.show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast toast5 = Toast.makeText(getBaseContext(),
							R.string.error_network, Toast.LENGTH_SHORT);
					toast5.show();
					break;
				}
			}
		}, new IntentFilter(sended));

		/**
		 *  SMS CONSEGNATO
		 */
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), R.string.sms_delivered,
							Toast.LENGTH_SHORT).show();
					break;
				case Activity.RESULT_CANCELED:
					Toast.makeText(getBaseContext(),
							R.string.error_sms_not_delivered,
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(delivered));
		sms.sendMultipartTextMessage(numero, null, arrayTesto, sendingIntents,
				null);
	}

	/**
	 * decodifica sms
	 * @param testo
	 * @return
	 */
	public static String encodeSms(String testo) {
		String res = null;
		try {
			byte[] bytestesto = testo.getBytes();
			ByteArrayInputStream inStream = new ByteArrayInputStream(bytestesto);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			GZIPOutputStream zipOutput = new GZIPOutputStream(outStream);
			int i;
			byte[] buffer = new byte[1024];
			while ((i = inStream.read(buffer)) > 0) {
				zipOutput.write(buffer, 0, i);
			}
			zipOutput.finish();
			zipOutput.close();
			res = Base64.encodeToString(outStream.toByteArray(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public void callbackCall() {
		FormListActivity f = new FormListActivity();
		
		for(int i=0;i<listDataToUpdateDB.size();i++){
			FormCompletedDataDBUpdate FormDataForDBupdate = listDataToUpdateDB.get(i);
			//f.updateFormsDataToSubmitted(nomeform + "&" + autore, data, autore, idFormNameInstance); //LL aggiunto ultimo parametro per cancellare dal DBGRASP la completa che deve diventare inviata
			f.updateFormsDataToSubmitted(FormDataForDBupdate.nomeform+ "&" + FormDataForDBupdate.autore, data, FormDataForDBupdate.autore, FormDataForDBupdate.idFormNameInstance); //e' stato necessario mettere i parametri da inviare in un oggetto per poter gestire nello stesso modo l'invio di una singola form e l'invio di tutte le form insieme 
		}
		listDataToUpdateDB.clear();
		
		if(isSendAllForms){
			if(PreferencesActivity.SERVER_ONLINE == "YES"){
				Toast.makeText(this, R.string.server_on_line, Toast.LENGTH_SHORT).show();
			}
			Toast.makeText(this, R.string.forms_sent, Toast.LENGTH_LONG).show();//LL 17-04-2014 necessario per gestire la visualizzazione del toast nel caso di invio di piu' form insieme
			isSendAllForms = false;//LL 17-04-2014 necessario per gestire la visualizzazione del toast nel caso di invio di piu' form insieme
		}
		
		
		
		finish();
	}

	private final Dialog createListFile(final String[] fileList) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confronta con:");
		builder.setSingleChoiceItems(fileList, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Log.d("scelta tipo di connessione",
								"E' stato premuto il pulsante: "
										+ fileList[whichButton]);

					}
				});
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		return builder.create();
	}

	private ArrayList<FormInnerListProxy> queryCompletedForm() {
		formListHandler = new FormListActivity();
		ArrayList<FormInnerListProxy> complete = formListHandler
				.getCompletedForm();

		return complete;
	}

	/**
	 * metodo di servizio per la verifica della connessione
	 * @return
	 */
	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			/**
			 *  There are no active networks.
			 */
			return false;
		} else
			return true;
	}

	/**
	 * invio tutte le form nel tab FormListCompleted
	 * @param complete
	 */
	private void sendFormInList(final ArrayList<FormInnerListProxy> complete) {//LL 17-04-2014 aggiunto ultimo parametro per far vedere il toast dell'invio andato bene solo una volta

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which)
		        {
			        case DialogInterface.BUTTON_POSITIVE:
			        	
			        	try {
							sendAllFormsWithNetwork(FormListCompletedActivity.this, httpServer,	numClient, complete, completed);
						}
						catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}//chiamo il task asincrono per inviare tutte le form
			        	//finish(); //LL 29-04-2014 per ora lo commento 
					
					break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            //send by sms 
		        	for (int k = 0; k < complete.size(); k++) {
						String xml = null;
						xml = decodeForm(complete.get(k));
						try 
						{
							sendSMS(numModem, xml, FormListCompletedActivity.this);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        	}
		        	break;
		        }
		    }
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(FormListCompletedActivity.this);
		builder.setMessage(getString(R.string.send_form_by_network_or_sms))
		.setPositiveButton(getString(R.string.gps), dialogClickListener)
		.setNegativeButton(getString(R.string.sms), dialogClickListener).show();
	}

	/**
	 * decodifica della form
	 * @param form
	 * @return string 
	 */
	private String decodeForm(FormInnerListProxy form) {
		String xml = null;
		try {
			istance.add(form.getFormNameInstance()); // formNameInstanceComplete[position];

			InputStream fileInput = new FileInputStream(
					form.getStrPathInstance()); // path[position]);
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(fileInput);
			Transformer trans = TransformerFactory.newInstance()
					.newTransformer();
			trans.setOutputProperty(OutputKeys.METHOD, "xml");
			trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			String xmlString = trasformItem(trans, doc, form);
			xml = encodeSms(xmlString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(xml);
		return xml;
	}

	/**
	 * genero il file 
	 * @param trans
	 * @param doc
	 * @param form
	 * @return
	 * @throws TransformerException
	 */
	public String trasformItem(Transformer trans, Document doc,
			FormInnerListProxy form) throws TransformerException {
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc);
		trans.transform(source, result);
		String xmlString = sw.toString();
		String apos = "apos=\"'\"";
		xmlString = xmlString.replace(apos, "");
		/**
		 *  add unique code to data xml response
		 */
		xmlString = xmlString + "?formidentificator?"
				+ form.getFormNameAutoGen();
		/**
		 *  add autogenerated name to data xml response
		 */
		xmlString = xmlString + "?formname?" + form.getFormNameInstance();
		/**
		 *  add date and time to data xml response
		 */
		GregorianCalendar gc = new GregorianCalendar();
		String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
		String month = Integer.toString(gc.get(Calendar.MONTH));
		String year = Integer.toString(gc.get(Calendar.YEAR));
		String hour = Integer.toString(gc.get(Calendar.HOUR_OF_DAY));
		String date = day + "/" + month + "/" + year;
		return xmlString = xmlString + "?formhour?" + date + "_" + hour;
	}

	private void sendSMS(String numModem, String encodeXml, MyCallback callback) {
		if (numModem.equalsIgnoreCase("") || numModem == null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					R.string.number_error, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 40, 40);
			toast.show();
		} else {
			sendSmsNetWorkOn(numModem, encodeXml, callback);
		}
	}

	/**
	 * invio della form tramite network
	 * @param context
	 * @param url
	 * @param number
	 * @param form
	 * @param callback
	 * @throws InterruptedException
	 */
	private void sendWithNetwork(Context context, String url, String number,
			String form, MyCallback callback) throws InterruptedException {
		
		if (httpServer.equalsIgnoreCase("") || httpServer == null) {
			Toast toast = Toast.makeText(getApplicationContext(),
					getString(R.string.server_url_not_inserted),
					Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 40, 40);
			toast.show();
		} else {

			HttpCheckAndSendPostTask asyncTask = new HttpCheckAndSendPostTask(context, url, number, form, callback, isSendAllForms);
			Log.i("FUNZIONE HttpCheckAndSendPostTask", "thread: " + form);
			asyncTask.execute();
			if (asyncTask.getStatus() != HttpCheckAndSendPostTask.Status.FINISHED) {

			}
		}
	}
	
	private void sendAllFormsWithNetwork(Context context, String url, String number, ArrayList<FormInnerListProxy> listaformcompletedfirst, 
			ArrayList<FormInnerListProxy> listaformcompletedsecond) throws InterruptedException {
		
		if (httpServer.equalsIgnoreCase("") || httpServer == null) {
			Toast toast = Toast.makeText(getApplicationContext(),getString(R.string.server_url_not_inserted),Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 40, 40);
			toast.show();
		} else {
			HttpSendAllFormsTask asyncTask = new HttpSendAllFormsTask(context, url, number, complete, completed, FormListCompletedActivity.this);//chiamo l'async task con la lista di tutte le form da inviare
			//Log.i("FUNZIONE HttpSendAllFormsTask", "thread: " + form);
			//Log.i("FUNZIONE HttpSendAllFormsTask", "thread: ");
			asyncTask.execute();
		}
	}

	public static void setID(String id) {
		idForm = id;
	}

	public void onResume() {
		super.onResume();
		getFormsDataCompleted();
		runOnUiThread(new Runnable() {
			public void run() {
				FormCompletedAdapter adapter = (FormCompletedAdapter) listview
						.getAdapter();
				adapter.notifyDataSetChanged();
			}
		});
	}

	public void getFormsDataCompleted() {
		completed.clear();
		// dbAdapter.open();
		Cursor cursor = ApplicationExt.getDatabaseAdapter().open()
				.fetchAllCompleted();
		try {
			while (cursor.moveToNext()) {
				FormInnerListProxy completed = new FormInnerListProxy();
				completed.setFormId(cursor.getString(cursor
						.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_ID_KEY)));
				completed
						.setFormName(cursor.getString(cursor
								.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_NOME_FORM)));
				completed.setDataDiCompletamento(cursor.getString(cursor
						.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_DATA)));
				completed.setFormEnumeratorId(cursor.getString(cursor
						.getColumnIndex(DbAdapterGrasp.COMPLETED_FORM_BY)));
				
				
				
				this.completed.add(completed);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				ApplicationExt.getDatabaseAdapter().close();
			}
		}
	}

	public void onDestroy() {
		listview.setAdapter(null);
		super.onDestroy();
	}

	//resetto i valori delle variabili globali con i valori della form che stiamo scorrendo per l'invio dei dati corretti al server
	public void setGlobalFieldForDBUpdating(int completeposition) {
		
		int positioncompleted = getPositionCompletedToSubmit(completeposition);
		FormInnerListProxy myCompleteDShortCut = completed.get(positioncompleted);
		FormInnerListProxy myCompleteShortCut = complete.get(completeposition);
		
		
		nomeform = myCompleteShortCut.getFormName();
		autore = myCompleteDShortCut.getFormEnumeratorId();
		idFormNameInstance = myCompleteShortCut.getFormNameInstance();
		
		FormCompletedDataDBUpdate myFormCompletedDataDBUpdate =  new FormCompletedDataDBUpdate(idFormNameInstance,nomeform,autore); 
		listDataToUpdateDB.add(myFormCompletedDataDBUpdate);
		
		
	}

	public static void UpdateDBAfterSendAllForms() {
		FormListActivity f = new FormListActivity();
		Log.i("UpdateDBAfterSendAllForms","1");
		for(int i=0;i<listDataToUpdateDB.size();i++){
			FormCompletedDataDBUpdate FormDataForDBupdate = listDataToUpdateDB.get(i);
			//f.updateFormsDataToSubmitted(nomeform + "&" + autore, data, autore, idFormNameInstance); //LL aggiunto ultimo parametro per cancellare dal DBGRASP la completa che deve diventare inviata
			f.updateFormsDataToSubmitted(FormDataForDBupdate.nomeform+ "&" + FormDataForDBupdate.autore, data, FormDataForDBupdate.autore, FormDataForDBupdate.idFormNameInstance); //e' stato necessario mettere i parametri da inviare in un oggetto per poter gestire nello stesso modo l'invio di una singola form e l'invio di tutte le form insieme 
		}
		listDataToUpdateDB.clear();
		
		
	}
	
	public void finishFormListCompleted() {
		finish();
	}
	
	
	
	
}
