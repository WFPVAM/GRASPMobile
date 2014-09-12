/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 ******************************************************************************/
package it.fabaris.wfp.broadcast;

import it.fabaris.wfp.activities.MenuActivity;
import it.fabaris.wfp.activities.PreferencesActivity;
import it.fabaris.wfp.activities.R;
import it.fabaris.wfp.application.Collect;
import it.fabaris.wfp.utility.XmlParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.GZIPInputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Base64;
import android.util.Log;

/**
 * Class that defines the filter for the incoming sms and decode it	
 *
 *
 */

public class SmsReceiverBroadcast extends BroadcastReceiver {

    private String sms="";
    public static int NOTIFICATION_ID = 1;
    private  NotificationManager manager;
    private  Notification notification;
    private  PendingIntent contentIntent;
    private SharedPreferences settings;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Object messages[] = (Object[]) bundle.get("pdus");
        SmsMessage smsMessage[] = new SmsMessage[messages.length];
        XmlParser px = new XmlParser();

        for (int n = 0; n < messages.length; n++) {
            smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
        }
        /**
         * receinving response message
         */
        if (smsMessage[0].getMessageBody().toString().startsWith("H4sIAAAAAAAAA")) {
            this.abortBroadcast();
            try{
                String smsToDecode = smsMessage[0].getMessageBody().toString();
                byte[] decodedString = Base64.decode(smsToDecode, 0);
                ByteArrayInputStream inStream = new ByteArrayInputStream(decodedString);
                GZIPInputStream zipInput = new GZIPInputStream(inStream);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                int i;
                byte[] buffer = new byte[1024];
                while ((i = zipInput.read(buffer)) > 0) {
                    outStream.write(buffer, 0, i);
                }
                zipInput.close();
                inStream.close();
                String res = outStream.toString("UTF-8");
                File myfile = new File (Collect.FORMS_PATH +"/response.xml");
                myfile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myfile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut,"UTF-8");
                myOutWriter.append(res.toString());
                myOutWriter.close();
                fOut.close();
                String resp = px.getResponse(myfile);
                it.fabaris.wfp.provider.FormProvider.DatabaseHelper dbh = new it.fabaris.wfp.provider.FormProvider.DatabaseHelper("forms.db");
                String updatequery = "UPDATE forms SET status='finalized' WHERE displayNameInstance = '"+resp+"' AND status='submitted'";
                dbh.getReadableDatabase().execSQL(updatequery);
                dbh.close();
                myfile.delete();
                manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                CharSequence title = "GRASP Mobile Tool";
                CharSequence subtitle = "The form was correctly received";
                int icon = R.drawable.ic_logo;
                long when = System.currentTimeMillis();
                notification = new Notification(icon, title, when);
                Intent i1 = new Intent(context, MenuActivity.class);
                contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, i1,PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setLatestEventInfo(context, title, subtitle, contentIntent);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults |= Notification.DEFAULT_SOUND; // Suona
                notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
                notification.defaults |= Notification.DEFAULT_VIBRATE; // Vibra
                manager.notify(NOTIFICATION_ID, notification);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        /**
         * receiving new form message
         */
        if(smsMessage[0].getMessageBody().toString().startsWith("There is a new form available")) {
            this.abortBroadcast();
            for(int i= 0;i<messages.length;i++){
                sms = sms.concat(smsMessage[i].getMessageBody().toString());
            }
            try{
                settings  =  PreferenceManager.getDefaultSharedPreferences(context);
                String strSender = smsMessage[0].getOriginatingAddress();
                Log.i("telefono", strSender);
                strSender = (strSender).trim();
                final SharedPreferences.Editor editor = settings.edit();
                editor.putString(PreferencesActivity.KEY_SERVER_TELEPHONE, strSender);
                editor.commit();
                Collect.createODKDirs();
                GregorianCalendar gc = new GregorianCalendar();
                String day = Integer.toString(gc.get(Calendar.DAY_OF_MONTH));
                String month = Integer.toString(gc.get(Calendar.MONTH));
                String year = Integer.toString(gc.get(Calendar.YEAR));
                String data = day+"-"+month+"-"+year;
                String delimiter2 = "<__>";
                String[] arrSmsBody = sms.split(delimiter2);
                String smsName = ((arrSmsBody[0].toString()).substring(31)).replace(" ","");
                String xmlId = "";
                smsName = ((arrSmsBody[0].toString()).substring(31)).replace("*","(copy)");
                String smsBody = arrSmsBody[1];
                it.fabaris.wfp.provider.MessageProvider.DatabaseHelper dbh = new it.fabaris.wfp.provider.MessageProvider.DatabaseHelper("message.db");
                String query = "INSERT INTO message" +
                        "(formId," +
                        "formName," +
                        "formImported," +
                        "formEncodedText," +
                        "formText," +
                        "date)" +
                        "VALUES" +
                        "('"+xmlId+"','"+smsName+"','no','"+smsBody+"','','"+data+"')";
                dbh.getWritableDatabase().execSQL(query);
                dbh.close();
                manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                CharSequence title = "GRASP Mobile Tool";
                CharSequence subtitle = "There is a new form available";
                int icon = R.drawable.ic_logo;
                long when = System.currentTimeMillis();
                notification = new Notification(icon, title, when);
                Intent i = new Intent(context, MenuActivity.class);
                contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, i,PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setLatestEventInfo(context, title, subtitle, contentIntent);
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults |= Notification.DEFAULT_SOUND; // Suona
                notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
                notification.defaults |= Notification.DEFAULT_VIBRATE; // Vibra
                manager.notify(NOTIFICATION_ID, notification);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
